package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositoryFileImpl(
    private val context: Context,
) : PostRepository {


    private val gson = Gson()
    private var nextId = 1L
    private val postsFileName = "posts.json"
    private val idFileName = "nextId.json"
    private var posts = emptyList<Post>()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val data = MutableLiveData(posts)

    init{
        val postsFile = context.filesDir.resolve(postsFileName)
        if (postsFile.exists()) {
            context.openFileInput(postsFileName).bufferedReader().use {
                posts = gson.fromJson(it, type)
                data.value = posts
            }
        } else {
            sync()
        }

        val idFile = context.filesDir.resolve(idFileName)
        if (idFile.exists()){
            context.openFileInput(idFileName).bufferedReader().use {
                nextId = gson.fromJson(it,Long::class.java)
                data.value = posts
            }
        } else {
            sync()
        }
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
        context.openFileOutput(postsFileName, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
        context.openFileOutput(idFileName, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(nextId))
        }
    }
}