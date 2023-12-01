package ru.netology.nmedia.auth

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.work.SendPushTokenWorker

class AppAuth private constructor(private val context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(KEY_ID, 0)
        val token = prefs.getString(KEY_TOKEN, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
            commit()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        val request = OneTimeWorkRequestBuilder<SendPushTokenWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putString(SendPushTokenWorker.TOKEN_KEY, token)
                    .build()
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SendPushTokenWorker.NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    companion object {

        private const val KEY_ID = "id"
        private const val KEY_TOKEN = "token"

        @Volatile
        private var instance: AppAuth? = null

        fun getInstance() = synchronized(this) {
            instance ?: throw IllegalAccessError("getInstance should be called only after initApp")
        }

        fun initAuth(context: Context) = instance ?: synchronized(this) {
            instance ?: AppAuth(context).also { instance = it }
        }
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)