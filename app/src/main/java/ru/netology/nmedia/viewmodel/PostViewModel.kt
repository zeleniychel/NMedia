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
    var draft: String? = null
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    init {
        load()
    }

    fun load() {
        _data.postValue(FeedModelState(loading = true))
        repository.getAllAsync(object : PostRepository.RepositoryCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModelState(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModelState(error = true))
            }
        })

    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clearEdit() {
        edited.value = empty
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (edited.value?.content != text) {
                repository.saveAsync(it.copy(content = text),
                    object : PostRepository.RepositoryCallback<Unit> {
                        override fun onSuccess(result: Unit) {
                        }
                        override fun onError(e: Exception) {
                            _data.postValue(FeedModelState(error = true))
                        }
                    })
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
    }

    fun likeById(id: Long) {
        val likeCallback = object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(result: Post) {
                updatedDataLike(result)
            }
            override fun onError(e: Exception) {
                _data.postValue(FeedModelState(error = true))
            }
        }

        _data.value?.posts?.map { post ->
            if (post.id == id) {
                if (post.likedByMe) {
                    repository.unlikeByIdAsync(id, likeCallback)
                } else {
                    repository.likeByIdAsync(id, likeCallback)
                }
            } else {
                post
            }
        }
    }

    fun updatedDataLike(updatedPost: Post) {
        val posts = _data.value?.posts?.map { post ->
            if (post.id == updatedPost.id) {
                updatedPost
            } else {
                post
            }
        }
        val updatedData = posts?.let { _data.value?.copy(posts = it, empty = it.isEmpty()) }
        _data.postValue(updatedData)
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )
        repository.removeByIdAsync(id, object : PostRepository.RepositoryCallback<Unit> {
            override fun onSuccess(result: Unit) {
                _data.postValue(_data.value?.posts?.isEmpty()?.let { FeedModelState(empty = it) })
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModelState(posts = old, error = true))
            }
        })
    }
}