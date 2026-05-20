package com.example.nestore_15.ui.screens

import com.example.nestore_15.data.model.User

data class ProviderProfileUi(
    val userId: String,
    val displayName: String,
    val businessName: String,
    val email: String,
    val phone: String,
    val contactAddress: String,
    val isVerified: Boolean
)

fun User.toProviderProfileUi(): ProviderProfileUi = ProviderProfileUi(
    userId = id,
    displayName = providerBusinessName.ifBlank { fullName }.ifBlank { email.substringBefore("@") },
    businessName = providerBusinessName.ifBlank { fullName },
    email = email,
    phone = phone,
    contactAddress = providerContactAddress,
    isVerified = isVerified || effectiveVerificationStatus() == com.example.nestore_15.data.model.VerificationStatus.VERIFIED
)

data class StudentProfileUi(
    val userId: String,
    val displayName: String,
    val email: String,
    val phone: String,
    val institution: String
)

fun User.toStudentProfileUi(): StudentProfileUi = StudentProfileUi(
    userId = id,
    displayName = fullName.ifBlank { email.substringBefore("@") },
    email = email,
    phone = phone,
    institution = studentInstitution
)
