package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likeByMe: Boolean,
    val likesCount: Int,
    val sharesCount: Int,
    val viewsCount: Int,
    val videoUrl: String?
)