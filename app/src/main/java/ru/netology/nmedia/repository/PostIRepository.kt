package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostIRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun share(id: Long)
}