package ru.netology.nmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Post(
    val id: Long = 0,
    val author: String = "",
    val content: String = "",
    val published: String = "",
    val likeByMe: Boolean = false,
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Int = 0,
    val videoUrl: String? = null
) : Parcelable