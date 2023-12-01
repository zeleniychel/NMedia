package ru.netology.nmedia.repository

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostWorkDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostWorkEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import java.io.IOException

class PostRepositoryImpl(private val dao: PostDao, private val postWorkDao: PostWorkDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)
    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long) {
        try {
            val response = PostsApi.retrofitService.getById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostsApi.retrofitService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity(isHidden = true))
            emit(dao.getIsHiddenCount())
        }

    }

    override suspend fun changeIsHiddenFlag() {
        dao.changeIsHiddenFlag()
    }


    override suspend fun likeById(post: Post) {
        if (dao.getIsSavedById(post.id)?.equals(false) == true) {
            return
        }
        dao.likeById(post.id)
        try {
            val response = if (post.likedByMe) {
                PostsApi.retrofitService.unlikeById(post.id)
            } else {
                PostsApi.retrofitService.likeById(post.id)
            }
            if (!response.isSuccessful) {
                dao.likeById(post.id)
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            dao.likeById(post.id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(post.id)
            throw UnknownError
        }

    }

    override suspend fun removeById(id: Long) {
        try {
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        val newPost = if (post.id == 0L) {
            post.copy(id = data.firstOrNull()?.first()?.id?.plus(1) ?: 1)
        } else {
            post.copy(id = data.firstOrNull()?.first()?.id ?: 1)
        }
        dao.insert(PostEntity.fromDto(newPost).copy(isSaved = false))
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.removeById(post.id)
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel) {
        val newPost = if (post.id == 0L) {
            post.copy(id = data.firstOrNull()?.first()?.id?.plus(1) ?: 1)
        } else {
            post.copy(id = data.firstOrNull()?.first()?.id ?: 1)
        }
        dao.insert(PostEntity.fromDto(newPost).copy(isSaved = false))
        try {
            val mediaResponse = saveMedia(photoModel.file!!)
            if (!mediaResponse.isSuccessful) {
                throw ApiError(mediaResponse.code(), mediaResponse.message())
            }
            val media = mediaResponse.body() ?: throw ApiError(
                mediaResponse.code(),
                mediaResponse.message()
            )

            val response = PostsApi.retrofitService.save(
                post.copy(
                    attachment = Attachment(
                        media.id,
                        AttachmentType.IMAGE
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.removeById(post.id)
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveMedia(file: File): Response<Media> {
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return PostsApi.retrofitService.saveMedia(part)
    }

    override suspend fun updateUser(login: String, password: String): AuthState {
        try {
            val response = PostsApi.retrofitService.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUser(login: String, password: String, name: String): AuthState {
        try {
            val response = PostsApi.retrofitService.registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWork(post: Post, upload: MediaUpload?): Long {
        try {
            val entity = PostWorkEntity.fromDto(post).copy().apply {
                if (upload != null) {
                    this.attachment.url = upload.file.toUri().toString()
                }
            }
            return postWorkDao.insert(entity)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun processWork(id: Long) {
        try {
            // TODO: handle this in homework
            val entity = postWorkDao.getById(id)
            if (entity.attachment?.url != null) {
                val upload = MediaUpload(Uri.parse(entity.attachment.url).toFile())
            }
            println(entity.id)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}