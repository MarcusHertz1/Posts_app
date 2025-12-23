package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.jvm.java
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.data[action]?.let { actionValue ->
            try {
                when (Action.valueOf(actionValue)) {
                    Action.LIKE -> {
                        val like = gson.fromJson(message.data[content], Like::class.java)
                        if (shouldShowNotification(like.recipientId)) {
                            handleLike(like)
                        }
                    }
                    Action.POST -> {
                        val newPost = gson.fromJson(message.data[content], NewPost::class.java)
                        if (shouldShowNotification(newPost.recipientId)) {
                            handleNewPost(newPost)
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                Log.w("FCMService", "Unknown action: $actionValue", e)
            }
        }
    }

    private fun shouldShowNotification(recipientId: Long?): Boolean {
        val currentUserId = AppAuth.getInstance().data.value?.id

        return when {
            recipientId == null -> true
            recipientId == currentUserId -> true
            recipientId == 0L && recipientId != currentUserId -> {
                AppAuth.getInstance().sendPushToken()
                false
            }
            recipientId != 0L && recipientId != currentUserId -> {
                AppAuth.getInstance().sendPushToken()
                false
            }
            else -> false
        }
    }

    override fun onNewToken(token: String) {
        Log.i("fcm", token)
        println(token)
        AppAuth.getInstance().sendPushToken(token)
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun handleNewPost(content: NewPost) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_new_post, content.userName))
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.postContent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }
}

enum class Action {
    LIKE,
    POST,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
    val recipientId: Long? = null,
)

data class NewPost(
    val userName: String,
    val postContent: String,
    val recipientId: Long? = null,
)