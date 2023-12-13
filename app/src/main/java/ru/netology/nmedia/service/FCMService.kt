package ru.netology.nmedia.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.activity.AppActivity
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class FCMService: FirebaseMessagingService() {

    @Inject
    lateinit var appAuth: AppAuth

    private val channelId = "server"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {

        val id = appAuth.authStateFlow.value.id
        val pushMessage = Gson().fromJson(message.data["content"], PushMessage::class.java)
        when{
            pushMessage.recipientId == null-> {
                handlePushMessage(pushMessage)
            }
            pushMessage.recipientId == 0L && pushMessage.recipientId != id ->{
                appAuth.sendPushToken()
            }

            pushMessage.recipientId == id -> {
                handlePushMessage(pushMessage)
            }
            pushMessage.recipientId != 0L && pushMessage.recipientId != id -> {
                appAuth.sendPushToken()
            }
            else -> {
                Log.d("FSM", message.data.toString())
            }
        }

        message.data["action"]?.let { action ->
            when (Actions.values().find {it.name == action}) {
                Actions.LIKE -> handleLike(Gson().fromJson(message.data["content"], Like::class.java))
                Actions.NEW_POST -> handleNewPost(Gson().fromJson(message.data["content"], NewPost::class.java))
                else -> Log.d("notification", "mismatch value")
            }
        }
        println(Gson().toJson(message))
    }

    private fun handlePushMessage(pushMessage: PushMessage){

        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this,channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(pushMessage.content)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(
            Random.nextInt(100_00),
            notification
        )
    }
    private fun handleLike(like: Like){

        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this,channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(getString((R.string.notification_user_liked), like.userName, like.postAuthor))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(
            Random.nextInt(100_00),
            notification
        )
    }

    private fun handleNewPost(newPost: NewPost){
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this,channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(getString((R.string.notification_user_create_new_post), newPost.postAuthor))
            .setContentIntent(pi)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(newPost.contentNewPost))
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(
            Random.nextInt(100_00),
            notification
        )
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }
}

enum class Actions {
    LIKE,
    NEW_POST
}

data class Like (
    val userId: Int,
    val userName: String,
    val postId: Int,
    val postAuthor: String
)

data class NewPost (
    val postAuthor: String,
    val contentNewPost: String
)

data class PushMessage(
    val recipientId: Long?,
    val content: String
)