package com.example.nestore_15.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.RegistrationRole
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.repository.AuthRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.launch

data class RegisterFieldErrors(
    val fullNameRequired: Boolean,
    val fullNameTooShort: Boolean,
    val phoneInvalid: Boolean,
    val emailRequired: Boolean,
    val emailInvalid: Boolean,
    val passwordRequired: Boolean,
    val passwordTooShort: Boolean
)

sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data class InvalidInput(val errors: RegisterFieldErrors) : RegisterUiState()
    data class Success(val role: UserRole) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
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

    fun submitRegistration(fullName: String, phone: String, email: String, password: String) {
        val trimmedName = fullName.trim()
        val trimmedPhone = phone.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        val fieldErrors = RegisterFieldErrors(
            fullNameRequired = trimmedName.isEmpty(),
            fullNameTooShort = trimmedName.isNotEmpty() && trimmedName.length < 2,
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
            authRepository.mockRegister(trimmedName, trimmedPhone, trimmedEmail, trimmedPassword, role).fold(
                onSuccess = { user ->
                    sessionManager.saveUser(user)
                    _uiState.value = RegisterUiState.Success(user.role)
                },
                onFailure = { error ->
                    _uiState.value = RegisterUiState.Error(error.message ?: "Registration failed")
                }
            )
        }
    }

    fun submitDebugVerifiedRegistration(role: RegistrationRole) {
        val ts = System.currentTimeMillis()
        val fullName = if (role == RegistrationRole.STUDENT) "Debug Student" else "Debug Provider"
        val phone = "71234567"
        val emailPrefix = if (role == RegistrationRole.STUDENT) "debug.student" else "debug.provider"
        val email = "$emailPrefix.$ts@findahome.test"
        val password = "Password123!"

        viewModelScope.launch {
            authRepository.mockRegister(
                fullName = fullName,
                phone = phone,
                email = email,
                password = password,
                role = role,
                forceVerified = true
            ).fold(
                onSuccess = { user ->
                    sessionManager.saveUser(user)
                    _uiState.value = RegisterUiState.Success(user.role)
                },
                onFailure = { error ->
                    _uiState.value = RegisterUiState.Error(error.message ?: "Debug registration failed")
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
        return errors.fullNameRequired ||
            errors.fullNameTooShort ||
            errors.phoneInvalid ||
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
