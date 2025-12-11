package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id:Long): Flow<Int>
    fun isEmpty(): Boolean
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