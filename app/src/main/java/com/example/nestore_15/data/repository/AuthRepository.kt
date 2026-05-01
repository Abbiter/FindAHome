package com.example.nestore_15.data.repository

import com.example.nestore_15.data.model.RegistrationRole
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun login(email: String, password: String): Result<User> {
        return runCatching {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("User not found")
            fetchUserProfile(firebaseUser)
        }.recoverCatching { throwable ->
            throw mapAuthFailure(throwable)
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: RegistrationRole,
        phone: String,
        forceVerified: Boolean = false
    ): Result<User> {
        return runCatching {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("User not found")

            val roleValue = role.toUserRole()
            val userProfile = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                role = roleValue,
                isVerified = forceVerified,
                fullName = fullName.trim(),
                phone = phone.trim(),
                verificationStatus = if (forceVerified) VerificationStatus.VERIFIED else VerificationStatus.NOT_SUBMITTED
            )

            firestore.collection("users").document(userProfile.id)
                .set(userProfile.toFirestoreMap(), SetOptions.merge())
                .await()
            userProfile
        }.recoverCatching { throwable ->
            throw mapAuthFailure(throwable)
        }
    }

    suspend fun mockLogin(email: String, password: String): Result<User> = login(email, password)

    suspend fun mockRegister(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        role: RegistrationRole,
        forceVerified: Boolean = false
    ): Result<User> = register(fullName, email, password, role, phone, forceVerified)

    private suspend fun fetchUserProfile(firebaseUser: FirebaseUser): User {
        val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
        return snapshot.toUserFromDocument(firebaseUser)
    }

    private fun RegistrationRole.toUserRole(): UserRole {
        return when (this) {
            RegistrationRole.STUDENT -> UserRole.STUDENT
            RegistrationRole.HOME_PROVIDER -> UserRole.PROVIDER
        }
    }

    private fun mapAuthFailure(throwable: Throwable): Throwable {
        val message = throwable.message.orEmpty()
        return when {
            message.contains("password is invalid", ignoreCase = true) ||
                message.contains("no user record", ignoreCase = true) ||
                message.contains("invalid credential", ignoreCase = true) ->
                IllegalArgumentException("Invalid login credentials")
            message.contains("network error", ignoreCase = true) ->
                IllegalStateException("Network error. Please try again.")
            message.contains("email address is already in use", ignoreCase = true) ->
                IllegalStateException("Email already in use")
            else -> throwable
        }
    }
}
