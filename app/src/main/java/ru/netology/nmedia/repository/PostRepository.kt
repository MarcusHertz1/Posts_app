package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
    //fun getNewer(id:Long): Flow<Int>
    //suspend fun loadNewerPosts(id: Long)
    fun isEmpty(): Boolean
    suspend fun like(id: Long): Post
    fun formatShortNumber(value: Long): String
    fun share(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post, photo: File? = null): Post
    suspend fun getById(id: Long): Post
    //suspend fun getAllAsync()
    fun getAvatarUrl(post: Post): String?
    fun getImageUrl(post: Post): String?

}