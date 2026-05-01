package com.example.nestore_15.data.model

enum class VerificationStatus {
    NOT_SUBMITTED,
    PENDING_REVIEW,
    VERIFIED;

    companion object {
        fun fromFirestore(status: String?, isVerifiedFlag: Boolean): VerificationStatus {
            if (isVerifiedFlag) return VERIFIED
            return when (status?.trim()?.uppercase()) {
                "PENDING_REVIEW", "PENDING" -> PENDING_REVIEW
                "VERIFIED" -> VERIFIED
                "NOT_SUBMITTED" -> NOT_SUBMITTED
                else -> NOT_SUBMITTED
            }
        }
    }
}

data class User(
    val id: String,
    val email: String,
    val role: UserRole,
    val isVerified: Boolean,
    val fullName: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_SUBMITTED,
    val verificationDocumentUrl: String = "",
    val studentInstitution: String = "",
    val studentId: String = "",
    val studentPreferredLocation: String = "",
    val studentBudgetMax: Double? = null,
    val providerBusinessName: String = "",
    val providerContactAddress: String = "",
    val providerOwnershipProofUrl: String = "",
    val providerPropertyCount: Int? = null
) {
    /** UI label: admin-approved flag wins; else explicit status and document presence. */
    fun effectiveVerificationStatus(): VerificationStatus {
        if (isVerified) return VerificationStatus.VERIFIED
        if (verificationStatus == VerificationStatus.PENDING_REVIEW) return VerificationStatus.PENDING_REVIEW
        if (verificationDocumentUrl.isNotBlank()) return VerificationStatus.PENDING_REVIEW
        if (providerOwnershipProofUrl.isNotBlank()) return VerificationStatus.PENDING_REVIEW
        return VerificationStatus.NOT_SUBMITTED
    }

    fun displayNameOrEmail(): String = fullName.ifBlank { email.ifBlank { "User" } }

    /** First token of [fullName] for “Welcome, …”; else email local-part; else neutral fallback. */
    fun greetingName(): String {
        val first = fullName.trim().split(Regex("\\s+")).firstOrNull { it.isNotEmpty() }
        if (first != null) return first
        val local = email.substringBefore('@').trim()
        if (local.isNotEmpty()) return local
        return "there"
    }
}

enum class UserRole {
    STUDENT,
    PROVIDER
}
