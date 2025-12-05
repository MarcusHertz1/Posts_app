package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun like(id: Long)
    fun formatShortNumber(value: Long): String
    fun share(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)

    fun getAllAsync(callback: GetAllCallBack)
    
    fun getAvatarUrl(post: Post): String
    fun getImageUrl(post: Post): String?

    interface GetAllCallBack {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Throwable)
    }
}