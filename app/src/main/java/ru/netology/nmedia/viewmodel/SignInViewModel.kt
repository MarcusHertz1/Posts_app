package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.util.SingleLiveEvent

class SignInViewModel(
    private val apiService: PostApiService,
    private val appAuth: AppAuth,
) : ViewModel() {

    private val _authSuccess = SingleLiveEvent<Unit>()
    val authSuccess: LiveData<Unit> get() = _authSuccess

    private val _authError = SingleLiveEvent<String>()
    val authError: LiveData<String> get() = _authError

    fun authenticate(login: String, pass: String) {
        viewModelScope.launch {
            try {
                val token = apiService.updateUser(login, pass)
                appAuth.setAuth(token.id, token.token)
                _authSuccess.postValue(Unit)
            } catch (e: Exception) {
                _authError.postValue(e.message ?: "Ошибка аутентификации")
            }
        }
    }
}
