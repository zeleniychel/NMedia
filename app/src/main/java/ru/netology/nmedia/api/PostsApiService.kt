package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken

interface PostsApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("post/latest")
    suspend fun getLatest(@Query("count") count:Int): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count:Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count:Int): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @Multipart
    @POST("media")
    suspend fun saveMedia(@Part part: MultipartBody.Part): Response<Media>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<AuthState>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<AuthState>

    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken): Response<Unit>
}


