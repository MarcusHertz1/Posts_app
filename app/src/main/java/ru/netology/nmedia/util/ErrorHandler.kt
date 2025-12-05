package ru.netology.nmedia.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.Response as OkHttpResponse
import retrofit2.Response
import ru.netology.nmedia.App

object ErrorHandler {
    private val handler = Handler(Looper.getMainLooper())
    
    fun handleError(response: Response<*>?) {
        showToast(response?.let { if (!it.isSuccessful) "Ой! Что-то пошло не так. ${it.code()}" else null })
    }
    
    fun handleError(response: OkHttpResponse?) {
        showToast(response?.let { if (!it.isSuccessful) "Ой! Что-то пошло не так. ${it.code}" else null })
    }
    
    fun handleError() {
        showToast(null)
    }
    
    private fun showToast(message: String?) {
        try {
            val app = App.getInstance()
            handler.post {
                Toast.makeText(app, message ?: "Ой! Что-то пошло не так.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IllegalStateException) {

        }
    }
}

