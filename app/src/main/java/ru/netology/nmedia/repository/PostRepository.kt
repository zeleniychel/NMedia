package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun getById(id:Long)
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun likeById(post:Post)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
}

