package ru.netology.nmedia

import android.app.Application
import ru.netology.nmedia.auth.AppAuth

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppAuth.init(this)
        instance = this
    }

    companion object {
        @Volatile
        private var instance: App? = null

        fun getInstance(): App {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}

