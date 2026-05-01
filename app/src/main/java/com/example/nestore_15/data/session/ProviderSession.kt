package com.example.nestore_15.data.session

import com.example.nestore_15.data.model.UserRole

sealed class ProviderSessionResult {
    data class Active(val userId: String) : ProviderSessionResult()
    data object RedirectStudent : ProviderSessionResult()
    data object RedirectLogin : ProviderSessionResult()
}

suspend fun SessionManager.resolveProviderSession(): ProviderSessionResult {
    val uid = getCurrentUserId() ?: return ProviderSessionResult.RedirectLogin
    val user = awaitCurrentUser() ?: return ProviderSessionResult.RedirectLogin
    if (user.id != uid) return ProviderSessionResult.RedirectLogin
    return when (user.role) {
        UserRole.PROVIDER -> ProviderSessionResult.Active(uid)
        UserRole.STUDENT -> ProviderSessionResult.RedirectStudent
    }
}
