package ru.netology.nmedia.application

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.work.RefreshPostsWorker
import java.util.concurrent.TimeUnit

class NMediaApplication: Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)
    override fun onCreate() {
        super.onCreate()
        AppAuth.initAuth(this)
        setupAuth()
        setupWork()
    }
    private fun setupAuth() {
        appScope.launch {
            AppAuth.initAuth(this@NMediaApplication)
        }
    }

    private fun setupWork() {
        appScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<RefreshPostsWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(this@NMediaApplication).enqueueUniquePeriodicWork(
                RefreshPostsWorker.name,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}