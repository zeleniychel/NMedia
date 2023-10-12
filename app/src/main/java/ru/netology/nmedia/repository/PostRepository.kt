package ru.netology.nmedia.repository

import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState

interface PostRepository {
    fun getAllAsync(callback:RepositoryCallback<List<Post>>)
    fun likeByIdAsync(id: Long, callback:RepositoryCallback<Post>)
    fun unlikeByIdAsync(id: Long, callback:RepositoryCallback<Post>)
    fun removeByIdAsync(id: Long, callback: RepositoryCallback<Unit>)
    fun saveAsync(post:Post, callback:RepositoryCallback<Unit>)

    interface RepositoryCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception) {
            MutableLiveData(FeedModelState()).postValue(FeedModelState(error = true))
        }
    }

}

