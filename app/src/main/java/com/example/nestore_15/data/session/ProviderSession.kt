package com.example.nestore_15.data.session

import com.example.nestore_15.data.model.UserRole
import kotlinx.coroutines.flow.first

sealed class ProviderSessionResult {
    data class Active(val userId: String) : ProviderSessionResult()
    data object RedirectStudent : ProviderSessionResult()
    data object RedirectLogin : ProviderSessionResult()
}

suspend fun SessionManager.resolveProviderSession(): ProviderSessionResult {
    val role = userRole.first()
    val uid = getCurrentUserId()
    return when {
        role == UserRole.PROVIDER && !uid.isNullOrBlank() -> ProviderSessionResult.Active(uid)
        role == UserRole.STUDENT -> ProviderSessionResult.RedirectStudent
        else -> ProviderSessionResult.RedirectLogin
    }
}
