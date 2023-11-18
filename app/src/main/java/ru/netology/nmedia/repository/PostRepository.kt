package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun getById(id: Long)
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun changeIsHiddenFlag()
    suspend fun likeById(post: Post)
    suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
}

