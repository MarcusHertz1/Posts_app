package ru.netology.nmedia.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.ErrorHandler
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())
    private val prefs = application.getSharedPreferences("draft", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(FeedModelState())
    val state: StateFlow<FeedModelState> = _state.asStateFlow()

    /*val data: Flow<FeedModel> =
        repository.data.map { FeedModel(it, it.isEmpty()) }*/
    val data: LiveData<FeedModel> =
        repository.data.map {list: List<Post> -> FeedModel(list, list.isEmpty()) }
            .catch { it.printStackTrace() }
            .asLiveData(Dispatchers.Default)

    /*val data: LiveData<FeedModel> =
        repository.data.asFlow().combine(repository.isEmpty().asFlow(), ::FeedModel)
            .asLiveData()*/

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
    
    val newerCount = data.switchMap { feedModel ->
        val rawNewerCount = repository.getNewer(feedModel.posts.firstOrNull()?.id ?: 0)
        combine(rawNewerCount, _ignoreNewerUntil) { count, ignoreUntil ->
            val currentTime = System.currentTimeMillis()
            if (currentTime < ignoreUntil) {
                0
            } else {
                count
            }
        }.asLiveData(Dispatchers.Default)
    }

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

    val draft = MutableLiveData<String?>(prefs.getString("draft", null))

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
    
    fun loadNewerPosts() {
        viewModelScope.launch {
            try {
                val firstPostId = data.value?.posts?.firstOrNull()?.id ?: 0
                repository.loadNewerPosts(firstPostId)
                _ignoreNewerUntil.value = System.currentTimeMillis() + 15_000
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
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