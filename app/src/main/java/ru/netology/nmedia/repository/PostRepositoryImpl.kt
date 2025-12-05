package ru.netology.nmedia.repository


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
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

    override fun like(id: Long) {
        /*val post = getById(id)

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

        okHttpClient.newCall(request).execute()*/TODO()
    }

    private fun getById(id: Long): Post {
        /*val request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        return gson.fromJson(
            okHttpClient.newCall(request).execute().body.string(),
            Post::class.java
        )*/TODO()
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
        PostApi.service.removeById(id).execute()
    }

    override fun save(post: Post){
        PostApi.service.save(post).execute()
    }

    override fun getAvatarUrl(post: Post): String {
        /*return post.authorAvatar?.let {
            val filename = if (it.contains(".")) it else "$it.jpg"
            "$BASE_URL/avatars/$filename"
        } ?: run {
            val authorKey = post.author.lowercase()
                .replace(" ", "")
                .replace(".", "")
                .replace(",", "")
                .take(20)
            "$BASE_URL/avatars/$authorKey.jpg"
        }*/TODO()
    }

    override fun getImageUrl(post: Post): String? {
        /*val imageUrl = post.attachment?.takeIf { it.type == AttachmentType.IMAGE }?.url
        return imageUrl?.let {
            if (it.startsWith("http")) {
                it
            } else {
                val filename = if (it.contains(".")) it else "$it.jpg"
                "$BASE_URL/images/$filename"
            }
        }*/
        TODO("Provide the return value")
    }
}