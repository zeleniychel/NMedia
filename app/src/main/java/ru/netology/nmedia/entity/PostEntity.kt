package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    @TypeConverters(ru.netology.nmedia.db.Converters::class)
    val attachment: Attachment? = null,
    val isSaved:Boolean = true,
    val isHidden: Boolean = false
) {
    fun toDto() = Post(id, author, authorAvatar, content, published, likedByMe, likes, attachment)

    companion object {
        fun fromDto(dto: Post, isSaved: Boolean = true, isHidden: Boolean = false) =
            PostEntity(
                dto.id,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                dto.attachment,
                isSaved = isSaved,
                isHidden = isHidden
            )

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(isSaved:Boolean = true, isHidden: Boolean = false): List<PostEntity> = map { PostEntity.fromDto(it,isSaved,isHidden) }