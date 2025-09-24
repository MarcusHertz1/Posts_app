package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositorySQLiteImpl

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositorySQLiteImpl(
        AppDb.getInstance(application).postDao
    )
    val data = repository.getAll()
    fun like(id: Long) = repository.like(id)
    fun share(id: Long) = repository.share(id)
    fun formatShortNumber(value: Long): String = repository.formatShortNumber(value)
    fun removeById(id: Long) = repository.removeById(id)
    val edited = MutableLiveData(empty)

    fun changeContent(content: String) {
        val text = content.trim()
        edited.value?.let {
            if (text == it.content) {
                return@let
            }
            edited.value = it.copy(content = text)
        }
    }

    fun save() {
        edited.value?.let {
            repository.save(it)
        }
        edited.value = empty
    }

    fun edit(post: Post) {
            edited.value = post
    }

    companion object {
        val empty = Post(
            id = 0,
            author = "",
            content = "",
            published = "",
            likes = 0,
            likedByMe = false,
            shares = 0,
            views = 0,
            video = null
        )
    }
}