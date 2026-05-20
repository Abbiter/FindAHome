package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data object Error : ProfileUiState()
}

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ProfileUiState>(ProfileUiState.Loading)
    val uiState: LiveData<ProfileUiState> = _uiState

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collectLatest { cached ->
                if (cached == null) {
                    _uiState.value = ProfileUiState.Error
                    return@collectLatest
                }
                val fresh = userRepository.getUser(cached.id)
                val user = fresh?.let { mergeProfile(cached, it) } ?: cached
                _uiState.value = ProfileUiState.Success(user)
            }
        }
    }

    private fun mergeProfile(local: User, remote: User): User = remote.copy(
        email = remote.email.ifBlank { local.email },
        fullName = remote.fullName.ifBlank { local.fullName },
        phone = remote.phone.ifBlank { local.phone },
        photoUrl = remote.photoUrl.ifBlank { local.photoUrl },
        studentInstitution = remote.studentInstitution.ifBlank { local.studentInstitution },
        studentId = remote.studentId.ifBlank { local.studentId },
        studentPreferredLocation = remote.studentPreferredLocation.ifBlank { local.studentPreferredLocation },
        studentBudgetMax = remote.studentBudgetMax ?: local.studentBudgetMax,
        providerBusinessName = remote.providerBusinessName.ifBlank { local.providerBusinessName },
        providerContactAddress = remote.providerContactAddress.ifBlank { local.providerContactAddress },
        providerOwnershipProofUrl = remote.providerOwnershipProofUrl.ifBlank { local.providerOwnershipProofUrl },
        providerPropertyCount = remote.providerPropertyCount ?: local.providerPropertyCount,
        verificationDocumentUrl = remote.verificationDocumentUrl.ifBlank { local.verificationDocumentUrl }
    )

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(sessionManager, UserRepository()) as T
                }
            }
    }
}
