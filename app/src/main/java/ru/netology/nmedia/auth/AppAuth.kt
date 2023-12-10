package ru.netology.nmedia.auth

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val keyId = "id"
    private val keyToken = "token"

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(keyId, 0)
        val token = prefs.getString(keyToken, null)

        if (id ==0L || token == null){
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()){
                clear()
                apply()
            }
        } else{
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }
        sendPushToken()
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Long,token: String){
        _authStateFlow.value = AuthState(id,token)
        with(prefs.edit()){
            putLong(keyId, id)
            putString(keyToken,token)
            commit()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth(){
        _authStateFlow.value = AuthState()
        with(prefs.edit()){
            clear()
            commit()
        }
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getPostsApiService():PostsApiService
    }
    fun sendPushToken(token: String? = null){
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token?: FirebaseMessaging.getInstance().token.await())
                val entry = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entry.getPostsApiService().sendPushToken(pushToken)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)