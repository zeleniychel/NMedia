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
    private val _toastMessage = SingleLiveEvent<String>()
    val toastMessage:LiveData<String> = _toastMessage

    init {
        _data.value = FeedModelState()
        load()

    }

    fun load() {
        _data.value = _data.value?.copy(loading = true, error = false)
        repository.getAllAsync(object : PostRepository.RepositoryCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.value = _data.value?.copy(posts = result, empty = result.isEmpty(), loading = false)
            }

            override fun onError(e: Exception) {
                _data.value = _data.value?.copy(error = true, loading = false, posts = emptyList())
                _toastMessage.value = ("${e.message}")
            }
        })

    }

/*    fun loadAll(){
        _data.value = _data.value?.copy(loading = true, error = false)
        repository.getAllPost().enqueue(object : Callback<List<Post>>{
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?: throw RuntimeException("empty body")
                    _data.value = (FeedModelState(
                        posts = responseBody,
                        empty = responseBody.isEmpty()))

                } else {
                    _toastMessage.value = ("Неудалось загрузить ленту. \n error code:${response.code()} ")
                }

            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                _data.value = _data.value?.copy(error = true)
                _toastMessage.value = ("${t.message}")
            }

        })
    }
*/

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
                            _postCreated.value = (Unit)
                        }

                        override fun onError(e: Exception) {
                            if (it.id == 0L){
                                _toastMessage.value = ("Неудалось создать пост \n ${e.message} ")
                            } else
                                _toastMessage.value = ("Неудалось изменить пост \n ${e.message} ")

                        }
                    })
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
                _toastMessage.value = ("${e.message}")
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
        val updatedData = posts?.let { _data.value?.copy(posts = it) }
        _data.value = (updatedData)
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.value = (
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )
        repository.removeByIdAsync(id, object : PostRepository.RepositoryCallback<Unit> {
            override fun onSuccess(result: Unit) {
                _data.value?.let { if (it.posts.isEmpty()) _data.value = it.copy(empty = true) }

            }

            override fun onError(e: Exception) {
                _data.value = (FeedModelState(posts = old))
                _toastMessage.value = ("${e.message}")
            }
        })
    }
}