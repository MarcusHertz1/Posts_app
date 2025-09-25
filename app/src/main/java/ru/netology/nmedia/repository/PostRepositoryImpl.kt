package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.toEntity

class PostRepositoryImpl(
    private val dao: PostDao
): PostRepository {
    override fun getAll(): LiveData<List<Post>> {
        return dao.getAll().map { list ->
            list.map{
                it.toDto()
            }
        }
    }

    override fun like(id: Long) {
        dao.likeById(id)
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
        dao.shareById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun save(post: Post) {
        dao.save(post.toEntity())
    }
}