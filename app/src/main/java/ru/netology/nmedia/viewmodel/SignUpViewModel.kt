package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.io.IOException

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _authentication: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val authentication: StateFlow<Boolean> = _authentication

    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            val user = repository.registerUser(login, password, name)
            user.token?.let { AppAuth.getInstance().setAuth(user.id, it) }
            _authentication.value = AppAuth.getInstance().authState.value.id != 0L
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    _errorMessage.value = "Network error"
                }

                is ApiError -> {
                    _errorMessage.value = "Api error"
                }

                else -> {
                    _errorMessage.value = "Unknown error"
                }
            }
        }
    }
}