package com.example.nestore_15.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.RegistrationRole
import com.example.nestore_15.data.repository.AuthRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.launch

data class RegisterFieldErrors(
    val phoneInvalid: Boolean,
    val emailRequired: Boolean,
    val emailInvalid: Boolean,
    val passwordRequired: Boolean,
    val passwordTooShort: Boolean
)

sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data class InvalidInput(val errors: RegisterFieldErrors) : RegisterUiState()
    data class Success(val role: com.example.nestore_15.data.model.UserRole) : RegisterUiState()
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _selectedRole = MutableLiveData(RegistrationRole.STUDENT)
    val selectedRole: LiveData<RegistrationRole> = _selectedRole

    private val _uiState = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val uiState: LiveData<RegisterUiState> = _uiState

    fun selectRole(role: RegistrationRole) {
        _selectedRole.value = role
    }

    fun submitRegistration(phone: String, email: String, password: String) {
        val trimmedPhone = phone.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        val fieldErrors = RegisterFieldErrors(
            phoneInvalid = !isValidPhone(trimmedPhone),
            emailRequired = trimmedEmail.isEmpty(),
            emailInvalid = trimmedEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches(),
            passwordRequired = trimmedPassword.isEmpty(),
            passwordTooShort = trimmedPassword.isNotEmpty() && trimmedPassword.length < 8
        )

        if (hasValidationErrors(fieldErrors)) {
            _uiState.value = RegisterUiState.InvalidInput(fieldErrors)
            return
        }

        val role = _selectedRole.value ?: RegistrationRole.STUDENT
        viewModelScope.launch {
            authRepository.mockRegister(trimmedPhone, trimmedEmail, trimmedPassword, role).fold(
                onSuccess = { user ->
                    sessionManager.saveUser(user)
                    _uiState.value = RegisterUiState.Success(user.role)
                },
                onFailure = {
                    _uiState.value = RegisterUiState.Idle
                }
            )
        }
    }

    fun acknowledgeState() {
        _uiState.value = RegisterUiState.Idle
    }

    private fun isValidPhone(raw: String): Boolean {
        if (raw.isBlank()) return false
        val digitsOnly = raw.filter { it.isDigit() }
        return digitsOnly.length in 8..15
    }

    private fun hasValidationErrors(errors: RegisterFieldErrors): Boolean {
        return errors.phoneInvalid ||
            errors.emailRequired ||
            errors.emailInvalid ||
            errors.passwordRequired ||
            errors.passwordTooShort
    }

    companion object {
        fun factory(
            authRepository: AuthRepository = AuthRepository(),
            sessionManager: SessionManager
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RegisterViewModel(authRepository, sessionManager) as T
                }
            }
    }
}
