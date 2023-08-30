package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likeByMe: Boolean,
    val likesCount: Int,
    val sharesCount: Int,
    val viewsCount: Int,
    val videoUrl: String?
) {
    fun toDto() = Post(
        id,
        author,
        content,
        published,
        likeByMe,
        likesCount,
        sharesCount,
        viewsCount,
        videoUrl
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.content,
                dto.published,
                dto.likeByMe,
                dto.likesCount,
                dto.sharesCount,
                dto.viewsCount,
                dto.videoUrl
            )

    }
}