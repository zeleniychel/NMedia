package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.work.SavePostWorker
import java.io.File

private val empty = Post(
    0,
    0,
    "",
    "",
    "",
    "",
    false,
    0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(
            AppDb.getInstance(application).postDao(),
            AppDb.getInstance(application).postWorkDao()
        )

    private val workManager: WorkManager =
        WorkManager.getInstance(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<FeedModel> = AppAuth.getInstance()
        .authStateFlow
        .flatMapLatest { auth ->
            repository.data.map { posts ->
                FeedModel(
                    posts.map { it.copy(ownedByMe = auth.id == it.authorId) },
                    posts.isEmpty()
                )
            }
        }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)


    val newerCount = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0)
            .catch { _dataState.postValue(FeedModelState(error = true)) }
            .asLiveData(Dispatchers.Default, 100)
    }

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?> = _photo

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState> = _dataState

    private val edited = MutableLiveData(empty)
    var draft: String? = null

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    init {
        load()

    }

    fun setPhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun load() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true, error = false)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true, loading = false)
        }

    }

    fun likeById(post: Post) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(error = false)
            repository.likeById(post)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }


    }

    fun changeIsHiddenFlag() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(error = false)
            repository.changeIsHiddenFlag()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true, error = false)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true, refreshing = false)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changeContentAndSave(content: String) {
        val text = content.trim()
        edited.value?.let {
            if (it.content == text) {
                return
            }
            edited.value = it.copy(content = text)
        }
        _postCreated.value = Unit
        viewModelScope.launch {
            try {
                val id = edited.value?.let { post ->
                    repository.saveWork(post, _photo.value?.file.let { MediaUpload(it.toFile) })
                }
                val data = workDataOf(SavePostWorker.postKey to id)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val request = OneTimeWorkRequestBuilder<SavePostWorker>()
                    .setInputData(data)
                    .setConstraints(constraints)
                    .build()
                workManager.enqueue(request)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        edited.value = empty
    }

//                val photoModel = _photo.value
//                if (photoModel == null) {
//                    edited.value?.let { repository.save(it) }
//                } else
//                    edited.value?.let { repository.saveWithAttachment(it, photoModel) }
//                _dataState.value = FeedModelState()
//            } catch (e: Exception) {
//                _dataState.value = FeedModelState(error = true)
//            }
//        }
//        edited.value = empty
//            }



            fun edit(post: Post) {
                edited.value = post
            }

            fun clearEdit() {
                edited.value = empty
            }
        }