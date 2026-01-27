package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.ErrorHandler
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedModelState())
    val state: StateFlow<FeedModelState> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = appAuth.data.flatMapLatest { token ->
            repository.data
                .map { posts ->
                    posts.map { it.copy(ownedByMe = it.authorId == token?.id) }
                }
                .catch { it.printStackTrace() }
        }
        .flowOn(Dispatchers.Default)

    private val edited = MutableStateFlow(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _shouldScrollToTop = SingleLiveEvent<Unit>()
    val shouldScrollToTop: LiveData<Unit>
        get() = _shouldScrollToTop

    private val _ignoreNewerUntil = MutableStateFlow(0L)

    private val _photo = MutableLiveData<PhotoModel?>()
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        loadPosts()
    }

    fun updatePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getAllAsync()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun like(id: Long) {
        viewModelScope.launch {
            try {
                repository.like(id)
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
                ErrorHandler.handleError()
            }
        }
    }

    fun share(id: Long) = repository.share(id)
    fun formatShortNumber(value: Long): String = repository.formatShortNumber(value)

    fun getAvatarUrl(post: Post): String? = repository.getAvatarUrl(post)
    fun getImageUrl(post: Post): String? = repository.getImageUrl(post)

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
                ErrorHandler.handleError()
            }
        }
    }

    val empty get() = PostViewModel.empty

    fun changeContent(content: String) {
        val text = content.trim()
        edited.value.let {
            if (text == it.content) {
                return@let
            }
            edited.value = it.copy(content = text)
        }
    }

    fun save() {
        viewModelScope.launch {
            edited.value.let {
                repository.save(it, _photo.value?.file)

                _postCreated.postValue(Unit)
                _shouldScrollToTop.postValue(Unit)
                _ignoreNewerUntil.value = System.currentTimeMillis() + 15_000
            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun removePhoto() {
        _photo.value = null
    }

    companion object {
        val empty = Post(
            id = 0,
            author = "",
            authorId = 0,
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