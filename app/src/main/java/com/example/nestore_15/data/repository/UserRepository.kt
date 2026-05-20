package com.example.nestore_15.data.repository

import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getUser(userId: String): User? {
        if (userId.isBlank()) return null
        return runCatching {
            firestore.collection("users").document(userId).get().await().toUserOrNull()
        }.getOrNull()
    }
}

fun DocumentSnapshot.toUserOrNull(): User? {
    if (!exists()) return null
    val isVerified = getBoolean("isVerified") ?: false
    val verificationStatus = VerificationStatus.fromFirestore(getString("verificationStatus"), isVerified)
    return User(
        id = getString("uid") ?: id,
        email = getString("email").orEmpty(),
        role = runCatching { UserRole.valueOf(getString("role").orEmpty()) }.getOrDefault(UserRole.STUDENT),
        isVerified = isVerified,
        fullName = getString("fullName").orEmpty(),
        phone = getString("phone").orEmpty(),
        photoUrl = getString("photoUrl").orEmpty(),
        verificationStatus = verificationStatus,
        verificationDocumentUrl = getString("verificationDocumentUrl").orEmpty(),
        studentInstitution = getString("studentInstitution").orEmpty(),
        studentId = getString("studentId").orEmpty(),
        studentPreferredLocation = getString("studentPreferredLocation").orEmpty(),
        studentBudgetMax = getDouble("studentBudgetMax"),
        providerBusinessName = getString("providerBusinessName").orEmpty(),
        providerContactAddress = getString("providerContactAddress").orEmpty(),
        providerOwnershipProofUrl = getString("providerOwnershipProofUrl").orEmpty(),
        providerPropertyCount = getLong("providerPropertyCount")?.toInt()
    )
}
