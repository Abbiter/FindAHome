package com.example.nestore_15.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.launch

sealed class VerificationUiState {
    data class Idle(
        val user: User,
        val instruction: String
    ) : VerificationUiState()

    data object Loading : VerificationUiState()
    data object Success : VerificationUiState()
}

class VerificationViewModel(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<VerificationUiState>(VerificationUiState.Loading)
    val uiState: LiveData<VerificationUiState> = _uiState

    private val _userMessage = MutableLiveData<String?>()
    val userMessage: LiveData<String?> = _userMessage

    init {
        loadCurrentUser()
    }

    fun consumeMessage() {
        _userMessage.value = null
    }

    fun uploadEnrollmentDocument(uri: Uri) {
        val idle = _uiState.value as? VerificationUiState.Idle ?: return
        _uiState.value = VerificationUiState.Loading
        viewModelScope.launch {
            runCatching {
                val url = userRepository.uploadVerificationDocument(idle.user.id, uri)
                val updated = idle.user.copy(
                    verificationDocumentUrl = url,
                    verificationStatus = VerificationStatus.PENDING_REVIEW,
                    isVerified = false
                )
                sessionManager.saveUser(updated)
            }.onSuccess {
                _userMessage.value = "Document uploaded"
                loadCurrentUser()
            }.onFailure { e ->
                _userMessage.value = e.message ?: "Upload failed"
                emitIdle(idle.user)
            }
        }
    }

    fun uploadOwnershipProof(uri: Uri) {
        val idle = _uiState.value as? VerificationUiState.Idle ?: return
        _uiState.value = VerificationUiState.Loading
        viewModelScope.launch {
            runCatching {
                val url = userRepository.uploadOwnershipProof(idle.user.id, uri)
                val updated = idle.user.copy(
                    providerOwnershipProofUrl = url,
                    verificationStatus = VerificationStatus.PENDING_REVIEW,
                    isVerified = false
                )
                sessionManager.saveUser(updated)
            }.onSuccess {
                _userMessage.value = "Document uploaded"
                loadCurrentUser()
            }.onFailure { e ->
                _userMessage.value = e.message ?: "Upload failed"
                emitIdle(idle.user)
            }
        }
    }

    fun submitForReview() {
        val idle = _uiState.value as? VerificationUiState.Idle ?: return
        val hasStudentDoc = idle.user.role == UserRole.STUDENT && idle.user.verificationDocumentUrl.isNotBlank()
        val hasProviderProof = idle.user.role == UserRole.PROVIDER && idle.user.providerOwnershipProofUrl.isNotBlank()
        if (!hasStudentDoc && !hasProviderProof) {
            _userMessage.value = "Upload the required document first"
            return
        }
        _uiState.value = VerificationUiState.Loading
        viewModelScope.launch {
            runCatching {
                sessionManager.saveUser(
                    idle.user.copy(
                        verificationStatus = VerificationStatus.PENDING_REVIEW,
                        isVerified = false
                    )
                )
            }.onSuccess {
                _uiState.value = VerificationUiState.Success
            }.onFailure { e ->
                _userMessage.value = e.message ?: "Submit failed"
                emitIdle(idle.user)
            }
        }
    }

    private fun emitIdle(user: User) {
        _uiState.value = VerificationUiState.Idle(
            user = user,
            instruction = instructionForRole(user.role)
        )
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = sessionManager.awaitCurrentUser()
            if (user == null) {
                _userMessage.value = "No active user session"
                _uiState.value = VerificationUiState.Loading
                return@launch
            }
            emitIdle(user)
        }
    }

    private fun instructionForRole(role: UserRole): String {
        return when (role) {
            UserRole.STUDENT -> "Upload proof of enrollment or student ID. Status becomes Pending until reviewed."
            UserRole.PROVIDER -> "Upload proof of ownership. Status becomes Pending until reviewed."
        }
    }

    companion object {
        fun factory(
            sessionManager: SessionManager,
            userRepository: UserRepository = UserRepository()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VerificationViewModel(sessionManager, userRepository) as T
                }
            }
    }
}
