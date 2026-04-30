package com.example.nestore_15.data.model

data class User(
    val id: String,
    val email: String,
    val role: UserRole,
    val isVerified: Boolean
)

enum class UserRole {
    STUDENT,
    PROVIDER
}
