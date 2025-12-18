package ru.netology.nmedia.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.util.ErrorHandler
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://10.0.2.2:9999/api/"

private val errorInterceptor = Interceptor { chain ->
    val response = chain.proceed(chain.request())
    if (!response.isSuccessful) {
        ErrorHandler.handleError(response)
    }
    response
}

private val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private val okHttpClient = OkHttpClient
    .Builder()
    .addInterceptor { chain ->
        val request = AppAuth.getInstance().data.value?.let { token ->
            chain.request().newBuilder()
                .addHeader("Authorization", token.token)
                .build()
        } ?: chain.request()

        chain.proceed(request)
    }
    .addInterceptor(logging)
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(errorInterceptor)
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface PostApiService {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @POST("posts/{id}/likes")
    suspend fun like(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun unlike(@Path("id") id: Long): Post

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long)

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): List<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Media

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
    ): Token
}

object PostApi {
    val retrofitService: PostApiService by lazy {
        retrofit.create()
    }

    const val MEDIA_URL = "http://10.0.2.2:9999"
}
