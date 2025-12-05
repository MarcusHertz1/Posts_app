package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {
    override fun getAll(): List<Post> {
        return PostApi.service.getAll().execute()
            .let { it.body() ?: throw RuntimeException("body is null") }
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallBack) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody().toString()))
                        return
                    }

                    val posts = response.body()
                    if (posts == null){
                        callback.onError(RuntimeException("Body is null"))
                        return
                    }

                    callback.onSuccess(posts)
                }

                override fun onFailure(
                    call: Call<List<Post>>,
                    t: Throwable
                ) {
                    callback.onError(t)
                }
            })
    }

    override fun like(id: Long, callback: PostRepository.LikeCallback) {
        getById(id, object : PostRepository.GetByIdCallback {
            override fun onSuccess(post: Post) {
                val call = if (post.likedByMe) {
                    PostApi.service.unlike(id)
                } else {
                    PostApi.service.like(id)
                }
                
                call.enqueue(object : Callback<Post> {
                    override fun onResponse(
                        call: Call<Post>,
                        response: Response<Post>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(response, null)
                            return
                        }

                        val updatedPost = response.body()
                        if (updatedPost == null) {
                            callback.onError(response, RuntimeException("Body is null"))
                            return
                        }

                        callback.onSuccess(updatedPost)
                    }

                    override fun onFailure(
                        call: Call<Post>,
                        t: Throwable
                    ) {
                        callback.onError(null, t)
                    }
                })
            }

            override fun onError(response: Response<*>?, throwable: Throwable?) {
                callback.onError(response, throwable)
            }
        })
    }

    override fun getById(id: Long, callback: PostRepository.GetByIdCallback) {
        PostApi.service.getById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(
                    call: Call<Post>,
                    response: Response<Post>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(response, null)
                        return
                    }

                    val post = response.body()
                    if (post == null) {
                        callback.onError(response, RuntimeException("Body is null"))
                        return
                    }

                    callback.onSuccess(post)
                }

                override fun onFailure(
                    call: Call<Post>,
                    t: Throwable
                ) {
                    callback.onError(null, t)
                }
            })
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

    override fun removeById(id: Long, callback: PostRepository.RemoveCallback) {
        PostApi.service.removeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(response, null)
                        return
                    }

                    callback.onSuccess()
                }

                override fun onFailure(
                    call: Call<Unit>,
                    t: Throwable
                ) {
                    callback.onError(null, t)
                }
            })
    }

    override fun save(post: Post, callback: PostRepository.SaveCallback) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(
                    call: Call<Post>,
                    response: Response<Post>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(response, null)
                        return
                    }

                    val savedPost = response.body()
                    if (savedPost == null) {
                        callback.onError(response, RuntimeException("Body is null"))
                        return
                    }

                    callback.onSuccess(savedPost)
                }

                override fun onFailure(
                    call: Call<Post>,
                    t: Throwable
                ) {
                    callback.onError(null, t)
                }
            })
    }

    override fun getAvatarUrl(post: Post): String {
        return post.authorAvatar?.let {
            val filename = if (it.contains(".")) it else "$it.jpg"
            "${PostApi.BASE_URL}/avatars/$filename"
        } ?: run {
            val authorKey = post.author.lowercase()
                .replace(" ", "")
                .replace(".", "")
                .replace(",", "")
                .take(20)
            "${PostApi.BASE_URL}/avatars/$authorKey.jpg"
        }
    }

    override fun getImageUrl(post: Post): String? {
        val imageUrl = post.attachment?.takeIf { it.type == AttachmentType.IMAGE }?.url
        return imageUrl?.let {
            if (it.startsWith("http")) {
                it
            } else {
                val filename = if (it.contains(".")) it else "$it.jpg"
                "${PostApi.BASE_URL}/images/$filename"
            }
        }
    }
}
