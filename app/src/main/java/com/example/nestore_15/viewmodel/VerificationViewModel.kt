package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class VerificationUiState {
    data class Idle(
        val user: User,
        val instruction: String,
        val isDocumentUploaded: Boolean
    ) : VerificationUiState()
    data object Loading : VerificationUiState()
    data object Success : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}

class VerificationViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableLiveData<VerificationUiState>(VerificationUiState.Loading)
    val uiState: LiveData<VerificationUiState> = _uiState

    init {
        loadCurrentUser()
    }

    fun mockUploadDocument() {
        val current = _uiState.value
        if (current is VerificationUiState.Idle) {
            _uiState.value = current.copy(isDocumentUploaded = true)
        }
    }

    fun submitVerification() {
        val current = _uiState.value
        if (current !is VerificationUiState.Idle) return

        if (!current.isDocumentUploaded) {
            _uiState.value = VerificationUiState.Error("Please upload a document first")
            _uiState.value = current
            return
        }

        _uiState.value = VerificationUiState.Loading
        viewModelScope.launch {
            val verifiedUser = current.user.copy(isVerified = true)
            runCatching {
                sessionManager.saveUser(verifiedUser)
            }.onSuccess {
                _uiState.value = VerificationUiState.Success
            }.onFailure {
                _uiState.value = VerificationUiState.Error("Verification failed. Try again.")
                _uiState.value = VerificationUiState.Idle(
                    user = current.user,
                    instruction = instructionForRole(current.user.role),
                    isDocumentUploaded = current.isDocumentUploaded
                )
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = sessionManager.getCurrentUser().firstOrNull()
            if (user == null) {
                _uiState.value = VerificationUiState.Error("No active user session")
                return@launch
            }
            _uiState.value = VerificationUiState.Idle(
                user = user,
                instruction = instructionForRole(user.role),
                isDocumentUploaded = false
            )
        }
    }

    private fun instructionForRole(role: UserRole): String {
        return when (role) {
            UserRole.STUDENT -> "Upload student ID or proof of enrollment."
            UserRole.PROVIDER -> "Upload proof of ownership."
        }
    }

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VerificationViewModel(sessionManager) as T
                }
            }
    }
}
