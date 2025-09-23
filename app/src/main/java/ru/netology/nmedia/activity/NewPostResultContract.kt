package ru.netology.nmedia.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.netology.nmedia.dto.Post

class NewPostResultContract: ActivityResultContract <Post?, String?>() {
    override fun createIntent(
        context: Context,
        input: Post?
    ): Intent {
        return Intent(context, NewPostActivity::class.java).apply {
            putExtra("content", input?.content ?: "")
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): String? {
        return intent?.getStringExtra(Intent.EXTRA_TEXT)
    }
}