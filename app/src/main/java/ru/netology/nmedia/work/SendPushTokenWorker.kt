package ru.netology.nmedia.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.PushToken

class SendPushTokenWorker(
    application: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    application,
    workerParameters
) {
    companion object{
        const val NAME = "SendPushTokenWorker"
        const val TOKEN_KEY = "TOKEN_KEY"
    }
    override suspend fun doWork(): Result =
        try {
            val token = inputData.getString(TOKEN_KEY)
            val pushToken = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
            PostsApi.retrofitService.sendPushToken(pushToken)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
}