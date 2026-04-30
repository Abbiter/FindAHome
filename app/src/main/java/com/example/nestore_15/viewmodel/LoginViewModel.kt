package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.repository.AuthRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val reason: LoginError) : LoginUiState()
}

enum class LoginError {
    EMPTY_CREDENTIALS,
    INVALID_CREDENTIALS
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun submitLogin(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            _uiState.value = LoginUiState.Error(LoginError.EMPTY_CREDENTIALS)
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository.mockLogin(trimmedEmail, trimmedPassword).fold(
                onSuccess = { user ->
                    sessionManager.saveUser(user)
                    _uiState.value = LoginUiState.Success
                },
                onFailure = {
                    _uiState.value = LoginUiState.Error(LoginError.INVALID_CREDENTIALS)
                }
            )
        }
    }

    fun acknowledgeState() {
        _uiState.value = LoginUiState.Idle
    }

    companion object {
        fun factory(
            authRepository: AuthRepository = AuthRepository(),
            sessionManager: SessionManager
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(authRepository, sessionManager) as T
                }
            }
    }
}
