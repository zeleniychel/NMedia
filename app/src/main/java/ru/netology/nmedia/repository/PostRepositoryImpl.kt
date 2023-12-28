package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostsApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(apiService, dao, postRemoteKeyDao, appDb)
    ).flow
        .map {
            it.map(PostEntity::toDto)
        }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
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
            val response = apiService.getById(id)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNewerCount(): Flow<Long> = dao.max()
        .flatMapLatest {
            if (it != null) {
                flow {
                    while (true) {
                        delay(10_000L)
                        val response = apiService.getNewerCount(it)
                        if (!response.isSuccessful) {
                            throw ApiError(response.code(), response.message())
                        }
                        val body =
                            response.body() ?: throw ApiError(response.code(), response.message())
                        emit(body.count)
                    }
                }
            } else {
                emptyFlow()
            }
        }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

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
                apiService.unlikeById(post.id)
            } else {
                apiService.likeById(post.id)
            }
            if (!response.isSuccessful) {
                dao.likeById(post.id)
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
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
            val response = apiService.removeById(id)
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
            val maxId = dao.max().firstOrNull() ?: 0L
            post.copy(id = maxId + 1)
        } else {
            post.copy(id = post.id)
        }
        dao.insert(PostEntity.fromDto(newPost).copy(isSaved = false))
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
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
            val maxId = dao.max().firstOrNull() ?: 0L
            post.copy(id = maxId + 1)
        } else {
            post.copy(id = post.id)
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

            val response = apiService.save(
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
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
            dao.removeById(post.id)
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveMedia(file: File): Response<Media> {
        val part =
            MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return apiService.saveMedia(part)
    }

    override suspend fun updateUser(login: String, password: String): AuthState {
        try {
            val response = apiService.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUser(
        login: String,
        password: String,
        name: String
    ): AuthState {
        try {
            val response = apiService.registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}