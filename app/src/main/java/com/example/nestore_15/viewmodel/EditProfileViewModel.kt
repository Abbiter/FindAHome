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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class EditProfileUiState {
    data object Loading : EditProfileUiState()
    data class Ready(val user: User) : EditProfileUiState()
    data object Saving : EditProfileUiState()
    data object SaveSuccess : EditProfileUiState()
}

class EditProfileViewModel(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: LiveData<EditProfileUiState> = _uiState

    private val _saveError = MutableLiveData<String?>()
    val saveError: LiveData<String?> = _saveError

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collectLatest { user ->
                if (_uiState.value is EditProfileUiState.Saving) return@collectLatest
                if (_uiState.value is EditProfileUiState.SaveSuccess) return@collectLatest
                if (user == null) {
                    _uiState.value = EditProfileUiState.Loading
                } else {
                    _uiState.value = EditProfileUiState.Ready(user)
                }
            }
        }
    }

    fun save(
        draft: User,
        profilePhotoUri: Uri?,
        verificationOrEnrollmentUri: Uri?,
        ownershipProofUri: Uri?
    ) {
        val snapshot = (_uiState.value as? EditProfileUiState.Ready)?.user ?: return
        _uiState.value = EditProfileUiState.Saving
        viewModelScope.launch {
            runCatching {
                var next = draft.copy(
                    id = snapshot.id,
                    email = snapshot.email,
                    role = snapshot.role
                )
                if (profilePhotoUri != null) {
                    val url = userRepository.uploadProfilePhoto(snapshot.id, profilePhotoUri)
                    next = next.copy(photoUrl = url)
                }
                if (verificationOrEnrollmentUri != null) {
                    val url = userRepository.uploadVerificationDocument(snapshot.id, verificationOrEnrollmentUri)
                    next = next.copy(
                        verificationDocumentUrl = url,
                        verificationStatus = VerificationStatus.PENDING_REVIEW,
                        isVerified = false
                    )
                }
                if (ownershipProofUri != null && snapshot.role == UserRole.PROVIDER) {
                    val url = userRepository.uploadOwnershipProof(snapshot.id, ownershipProofUri)
                    next = next.copy(
                        providerOwnershipProofUrl = url,
                        verificationStatus = VerificationStatus.PENDING_REVIEW,
                        isVerified = false
                    )
                }
                sessionManager.saveUser(next)
            }.onSuccess {
                _uiState.value = EditProfileUiState.SaveSuccess
            }.onFailure { e ->
                _saveError.value = e.message ?: "Save failed"
                _uiState.value = EditProfileUiState.Ready(snapshot)
            }
        }
    }

    fun consumeSaveError() {
        _saveError.value = null
    }

    companion object {
        fun factory(
            sessionManager: SessionManager,
            userRepository: UserRepository = UserRepository()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditProfileViewModel(sessionManager, userRepository) as T
                }
            }
    }
}
