package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.repository.PostRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _authentication: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val authentication: StateFlow<Boolean> = _authentication



    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            val user = repository.registerUser(login, password, name)
            user.token?.let { appAuth.setAuth(user.id, it) }
            _authentication.value = appAuth.authStateFlow.value.id != 0L
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