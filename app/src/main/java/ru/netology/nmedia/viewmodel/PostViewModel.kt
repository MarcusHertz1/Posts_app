package ru.netology.nmedia.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.ErrorHandler
import ru.netology.nmedia.util.SingleLiveEvent

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())
    private val prefs = application.getSharedPreferences("draft", Context.MODE_PRIVATE)
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    val data: LiveData<FeedModel> =
        repository.data.asFlow().combine(repository.isEmpty().asFlow(), ::FeedModel)
            .asLiveData()

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                repository.getAllAsync()
                _state.postValue(FeedModelState())
            } catch (_: Exception) {
                _state.postValue(FeedModelState(error = true))
            }
        }
    }

    val draft = MutableLiveData<String?>(prefs.getString("draft", null))

    fun like(id: Long) {
        viewModelScope.launch {
            try {
                repository.like(id)
            } catch (_: Exception) {
                _state.postValue(FeedModelState(error = true))
                ErrorHandler.handleError()
            }
        }
    }

    fun share(id: Long) = repository.share(id)
    fun formatShortNumber(value: Long): String = repository.formatShortNumber(value)

    fun getAvatarUrl(post: Post): String = repository.getAvatarUrl(post)
    fun getImageUrl(post: Post): String? = repository.getImageUrl(post)

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (_: Exception) {
                _state.postValue(FeedModelState(error = true))
                ErrorHandler.handleError()
            }
        }
    }

    val empty get() = PostViewModel.empty

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
        viewModelScope.launch {
            edited.value?.let {
                repository.save(it)

                _postCreated.postValue(Unit)
            }
            edited.value = empty
        }
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