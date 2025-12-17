package ru.netology.nmedia.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

class PostRepositoryImpl(
    private val dao: PostDao
) : PostRepository {
    override val data = dao.getAll().map {
        it.map(PostEntity::toDto)
    }/*.flowOn(Dispatchers.Default)*/

    override fun isEmpty() = dao.isEmpty()

    override suspend fun like(id: Long): Post {
        val local = dao.getById(id) ?: throw IllegalStateException("Post not found in DB: $id")

        dao.likeById(id)

        return try {
            val result = if (local.likedByMe) {
                PostApi.retrofitService.unlike(id)
            } else {
                PostApi.retrofitService.like(id)
            }
            dao.insert(PostEntity.fromDto(result))
            result
        } catch (e: Exception) {
            dao.likeById(id)
            throw e
        }
    }

    override suspend fun getAllAsync() {
        val posts = PostApi.retrofitService.getAll()
        dao.insert(posts.map(PostEntity::fromDto))
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            val posts = PostApi.retrofitService.getNewer(id)
            emit(posts.size)
        }
    }/*.catch { e -> throw AppError.from(e) }*/

    override suspend fun loadNewerPosts(id: Long) {
        val posts = PostApi.retrofitService.getNewer(id)
        dao.insert(posts.map(PostEntity::fromDto))
    }

    override suspend fun save(post: Post): Post {
        val postFromServer = PostApi.retrofitService.save(post)
        dao.insert(PostEntity.fromDto(postFromServer))
        return postFromServer
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

    override suspend fun removeById(id: Long) {
        val existing = dao.getById(id)
        dao.removeById(id)

        try {
            PostApi.retrofitService.removeById(id)
        } catch (e: Exception) {
            if (existing != null) {
                dao.insert(existing)
            }
            throw e
        }
    }

    override suspend fun getById(id: Long): Post {
        val post = PostApi.retrofitService.getById(id)
        dao.insert(PostEntity.fromDto(post))
        return post
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
