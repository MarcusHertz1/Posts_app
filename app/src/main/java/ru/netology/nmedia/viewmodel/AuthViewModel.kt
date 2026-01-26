package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel(
    private val appAuth: AppAuth
) : ViewModel() {
    val data = appAuth.data.asLiveData()
    val isAuth: Boolean
        get() = appAuth.data.value !=null
}