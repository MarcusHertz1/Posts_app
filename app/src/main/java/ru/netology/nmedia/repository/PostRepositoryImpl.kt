package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.MEDIA_URL
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService
) : PostRepository {
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { PostPagingSource(apiService) }
    ).flow

    override fun isEmpty() = dao.isEmpty()

    override suspend fun like(id: Long): Post {
        val local = dao.getById(id) ?: throw IllegalStateException("Post not found in DB: $id")

        dao.likeById(id)

        return try {
            val result = if (local.likedByMe) {
               apiService.unlike(id)
            } else {
               apiService.like(id)
            }
            dao.insert(PostEntity.fromDto(result))
            result
        } catch (e: Exception) {
            dao.likeById(id)
            throw e
        }
    }

    override suspend fun getAllAsync() {
        val posts =apiService.getAll()
        dao.insert(posts.map(PostEntity::fromDto))
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            val posts =apiService.getNewer(id)
            emit(posts.size)
        }
    }/*.catch { e -> throw AppError.from(e) }*/

    override suspend fun loadNewerPosts(id: Long) {
        val posts =apiService.getNewer(id)
        dao.insert(posts.map(PostEntity::fromDto))
    }

    override suspend fun save(post: Post, photo: File?): Post {
        val media = photo?.let {
            upload(it)
        }
        val postWithAttachment = post.copy(
            attachment = media?.let {
                Attachment(url = it.id, type = AttachmentType.IMAGE)
            }
        )
        val postFromServer =apiService.save(postWithAttachment)
        dao.insert(PostEntity.fromDto(postFromServer))
        return postFromServer
    }

    private suspend fun upload(file: File): Media =
       apiService.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
        )


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
           apiService.removeById(id)
        } catch (e: Exception) {
            if (existing != null) {
                dao.insert(existing)
            }
            throw e
        }
    }

    override suspend fun getById(id: Long): Post {
        val post =apiService.getById(id)
        dao.insert(PostEntity.fromDto(post))
        return post
    }

    override fun getAvatarUrl(post: Post): String? {
        val avatar = post.authorAvatar ?: return null
        val filename = if (avatar.contains(".")) avatar else "$avatar.jpg"

        return "${MEDIA_URL}/avatars/$filename"
    }

    /*override fun getAvatarUrl(post: Post): String {
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
    }*/

    override fun getImageUrl(post: Post): String? {
        val imageUrl = post.attachment?.takeIf { it.type == AttachmentType.IMAGE }?.url
        return imageUrl?.let {
            if (it.startsWith("http")) {
                it
            } else {
                val filename = if (it.contains(".")) it else "$it.jpg"
                "${MEDIA_URL}/media/$filename"
            }
        }
    }
}
