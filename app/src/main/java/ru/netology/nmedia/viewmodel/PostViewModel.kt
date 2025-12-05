package ru.netology.nmedia.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    private val prefs = application.getSharedPreferences("draft", Context.MODE_PRIVATE)
    val data: LiveData<FeedModel>
        get() = _data

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.GetAllCallBack{
            override fun onSuccess(posts: List<Post>) {
                _data.value = (FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Throwable) {
                _data.value = (FeedModel(error = true))
            }
        })
    }

    val draft = MutableLiveData<String?>(prefs.getString("draft", null))

    fun like(id: Long) {
        repository.getAllAsync(object : PostRepository.GetAllCallBack{
            override fun onSuccess(posts: List<Post>) {
                repository.like(id)
                loadPosts()
            }

            override fun onError(e: Throwable) {
                // Обработка ошибки
            }
        })
    }

    fun share(id: Long) = repository.share(id)
    fun formatShortNumber(value: Long): String = repository.formatShortNumber(value)
    
    fun getAvatarUrl(post: Post): String = repository.getAvatarUrl(post)
    fun getImageUrl(post: Post): String? = repository.getImageUrl(post)

    fun removeById(id: Long) {
        repository.getAllAsync(object : PostRepository.GetAllCallBack{
            override fun onSuccess(posts: List<Post>) {
                repository.removeById(id)
                loadPosts()
            }

            override fun onError(e: Throwable) {
                // Обработка ошибки
            }
        })
    }

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val empty get() = PostViewModel.empty

    fun saveDraft(text: String) {
        draft.value = text
        prefs.edit { putString("draft", text) }
    }

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
            thread {
                repository.save(it)
                loadPosts()
                _postCreated.postValue(Unit)
            }
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