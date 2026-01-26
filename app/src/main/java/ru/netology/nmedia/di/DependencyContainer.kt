package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.ErrorHandler
import java.util.concurrent.TimeUnit

class DependencyContainer(
    private val context: Context
) {

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/api/"
        @Volatile
        private var instance: DependencyContainer? = null

        fun initApp(context: Context) {
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer = instance!!
    }

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

    val appAuth = AppAuth(context)

    private val okHttpClient = OkHttpClient
        .Builder()
        .addInterceptor { chain ->
            val request = appAuth.data.value?.let { token ->
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

    private val appBd = Room
        .databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    private val postDao = appBd.postDao()
    val apiService =  retrofit.create<PostApiService>()

    val repository = PostRepositoryImpl(
        dao = postDao,
        apiService = apiService
    )

}