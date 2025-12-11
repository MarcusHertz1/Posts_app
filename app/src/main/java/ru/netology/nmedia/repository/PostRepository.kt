package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    fun isEmpty(): LiveData<Boolean>
    suspend fun like(id: Long): Post
    fun formatShortNumber(value: Long): String
    fun share(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post
    suspend fun getById(id: Long): Post
    suspend fun getAllAsync()
    fun getAvatarUrl(post: Post): String
    fun getImageUrl(post: Post): String?
}