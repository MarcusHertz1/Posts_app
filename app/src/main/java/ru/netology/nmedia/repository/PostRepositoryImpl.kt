package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class PostRepositoryImpl: PostRepository {
    private val okHttpClient = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()

    private val gson = Gson()

    private companion object{
        const val BASE_URL = "http://10.0.2.2:9999"
        val jsonType = "application/json".toMediaType()
        val postsType: Type = object : TypeToken<List<Post>>() {}.type
    }

    override fun getAll(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()
        val stringResponse = response.body.string()
        return gson.fromJson(stringResponse, postsType)
    }

    override fun like(id: Long) {
        val post = getById(id)

        val request = if (post.likedByMe) {
            Request.Builder()
                .url("${BASE_URL}/api/slow/posts/$id/likes")
                .delete()
                .build()
        } else {
            Request.Builder()
                .url("${BASE_URL}/api/slow/posts/$id/likes")
                .post("".toRequestBody(null))
                .build()
        }

        val call = okHttpClient.newCall(request)
        call.execute()
    }

    private fun getById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()
        val stringResponse = response.body.string()
        return gson.fromJson(stringResponse, Post::class.java)
    }

    override fun formatShortNumber(value: Long): String {
        return when {
            value < 1_000 -> value.toString()
            value < 10_000 -> {
                val thousands = value / 1000
                val hundreds = (value % 1000) / 100
                "$thousands.${hundreds}K"
            }

            value < 1_000_000 -> {
                val thousands = value / 1000
                "${thousands}K"
            }

            else -> {
                val millions = value / 1_000_000
                val hundredThousands = (value % 1_000_000) / 100_000
                "$millions.${hundredThousands}M"
            }
        }
    }

    override fun share(id: Long) {
        //dao.shareById(id)
    }

    override fun removeById(id: Long) {
        //dao.removeById(id)
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()
        val stringResponse = response.body.string()
        return gson.fromJson(stringResponse, Post::class.java)
    }
}