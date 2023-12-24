package ru.netology.nmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    override val id: Long = 0,
    val authorId: Long = 0,
    val author: String = "",
    val authorAvatar: String = "",
    val content: String = "",
    val published: String = "",
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val attachment: Attachment? = null,
    val ownedByMe:Boolean = false
) :FeedItem, Parcelable