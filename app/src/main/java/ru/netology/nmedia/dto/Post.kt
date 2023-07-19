package ru.netology.nmedia.dto

data class Post (
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    var likeByMe: Boolean,
    var likesCount: Int,
    var sharesCount: Int,
    var viewsCount: Int
)