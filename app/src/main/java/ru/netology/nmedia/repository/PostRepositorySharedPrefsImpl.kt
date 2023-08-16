package ru.netology.nmedia.repository

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositorySharedPrefsImpl(
    context: Context,
) : PostRepository {
    private val prefs = context.getSharedPreferences("posts", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var nextId = 1L
    private var posts = emptyList<Post>()
    private val postsKey = "posts"
    private val nextIdKey = "nextId"
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    private val data = MutableLiveData(posts)

    init{
        posts = prefs.getString(postsKey, null)?.let {
            gson.fromJson<List<Post>>(it, type)
        }
            .orEmpty()

        prefs.getLong(nextIdKey,nextId)

        data.value = posts
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likeByMe = !it.likeByMe,
                likesCount = if (it.likeByMe) it.likesCount - 1 else it.likesCount + 1
            )
        }
        data.value = posts
        sync()
    }

    override fun share(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(sharesCount = it.sharesCount + 1)
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun save(post: Post) {
        if (post.id == 0L) {
            posts = listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    likeByMe = false,
                    published = "now"
                )
            ) + posts
            data.value = posts
            sync()
            return
        }

        posts = posts.map {
            if (it.id != post.id) it else it.copy(content = post.content)
        }
        data.value = posts
        sync()
    }

    private fun sync() {
        prefs.edit {
            putString(postsKey,gson.toJson(posts))
            putLong(nextIdKey, nextId)
        }
    }
}