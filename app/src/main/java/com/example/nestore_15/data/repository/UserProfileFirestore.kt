package com.example.nestore_15.data.repository

import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot

private fun String?.toUserRoleOrDefault(): UserRole =
    runCatching { UserRole.valueOf(this ?: "") }.getOrDefault(UserRole.STUDENT)

fun DocumentSnapshot.toUserFromDocument(fallbackUser: FirebaseUser): User {
    if (!exists()) {
        return User(
            id = fallbackUser.uid,
            email = fallbackUser.email.orEmpty(),
            role = UserRole.STUDENT,
            isVerified = false
        )
    }
    val isVerified = getBoolean("isVerified") ?: false
    val statusRaw = getString("verificationStatus")
    val verificationStatus = VerificationStatus.fromFirestore(statusRaw, isVerified)

    return User(
        id = getString("uid") ?: this.id,
        email = getString("email") ?: fallbackUser.email.orEmpty(),
        role = getString("role").toUserRoleOrDefault(),
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

fun User.toFirestoreMap(): HashMap<String, Any> {
    val map = HashMap<String, Any>()
    map["uid"] = id
    map["email"] = email
    map["role"] = role.name
    map["isVerified"] = isVerified
    map["fullName"] = fullName
    map["phone"] = phone
    map["photoUrl"] = photoUrl
    map["verificationStatus"] = verificationStatus.name
    map["verificationDocumentUrl"] = verificationDocumentUrl
    map["studentInstitution"] = studentInstitution
    map["studentId"] = studentId
    map["studentPreferredLocation"] = studentPreferredLocation
    map["providerBusinessName"] = providerBusinessName
    map["providerContactAddress"] = providerContactAddress
    map["providerOwnershipProofUrl"] = providerOwnershipProofUrl
    studentBudgetMax?.let { map["studentBudgetMax"] = it }
    providerPropertyCount?.let { map["providerPropertyCount"] = it }
    return map
}
