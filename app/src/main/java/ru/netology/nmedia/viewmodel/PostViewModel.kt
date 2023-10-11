package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    0,
    "",
    "",
    "",
    false,
    0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModelState())
    val data: LiveData<FeedModelState> = _data
    private val edited = MutableLiveData(empty)
    var draft:String? = null
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    init {
        load()
    }
    fun load() {

        thread {
            _data.postValue(FeedModelState(loading = true))
            try {
                val posts = repository.getAll()
                FeedModelState(posts = posts, empty = posts.isEmpty())
            } catch (e: Exception) {
                FeedModelState(error = true)
            }.also(_data::postValue)
        }

    }
    fun edit(post: Post) {
        edited.value = post
    }

    fun clearEdit() {
        edited.value = empty
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            thread {
                val text = content.trim()
                if (edited.value?.content != text) {
                    repository.save(it.copy(content = text))
                    _postCreated.postValue(Unit)
                }
            }
        }
        edited.value = empty
    }

    fun likeById(id: Long) {
        thread {

            val updatedPosts = _data.value?.posts?.map { post ->
                if (post.id == id) {
                    if (post.likedByMe) {
                        repository.unlikeById(id)
                    } else {
                        repository.likeById(id)
                    }

                } else {
                    post
                }
            }
            val updatedData = updatedPosts?.let { _data.value?.copy(posts = it) }
            _data.postValue(updatedData)
        }
    }

    fun removeById(id: Long) {
        thread {
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }
}