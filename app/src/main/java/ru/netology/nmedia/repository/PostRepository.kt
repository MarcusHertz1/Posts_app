package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun like(id: Long, callback: LikeCallback)
    fun formatShortNumber(value: Long): String
    fun share(id: Long)
    fun removeById(id: Long, callback: RemoveCallback)
    fun save(post: Post, callback: SaveCallback)
    fun getById(id: Long, callback: GetByIdCallback)

    fun getAllAsync(callback: GetAllCallBack)
    
    fun getAvatarUrl(post: Post): String
    fun getImageUrl(post: Post): String?

    interface GetAllCallBack {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Throwable)
    }
    
    interface LikeCallback {
        fun onSuccess(post: Post)
        fun onError(response: retrofit2.Response<*>?, throwable: Throwable?)
    }
    
    interface RemoveCallback {
        fun onSuccess()
        fun onError(response: retrofit2.Response<*>?, throwable: Throwable?)
    }
    
    interface SaveCallback {
        fun onSuccess(post: Post)
        fun onError(response: retrofit2.Response<*>?, throwable: Throwable?)
    }
    
    interface GetByIdCallback {
        fun onSuccess(post: Post)
        fun onError(response: retrofit2.Response<*>?, throwable: Throwable?)
    }
}