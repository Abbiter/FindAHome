package com.example.nestore_15.data.repository

import com.example.nestore_15.data.model.Inquiry
import com.example.nestore_15.data.model.InquiryThreadStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private fun DocumentSnapshot.toInquiryOrNull(): Inquiry? {
    val propertyId = getString("propertyId") ?: return null
    val propertyTitle = getString("propertyTitle").orEmpty()
    val providerId = getString("providerId") ?: return null
    val studentId = getString("studentId") ?: return null
    val studentName = getString("studentName").orEmpty()
    val message = getString("message").orEmpty()
    val createdAt = getLong("createdAt") ?: return null
    val status = InquiryThreadStatus.fromFirestore(getString("inquiryStatus"))
    return Inquiry(
        id = id,
        propertyId = propertyId,
        propertyTitle = propertyTitle,
        providerId = providerId,
        studentId = studentId,
        studentName = studentName,
        message = message,
        createdAt = createdAt,
        inquiryStatus = status
    )
}

class InquiryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observeInquiriesForProvider(providerId: String): Flow<List<Inquiry>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("inquiries")
            .whereEqualTo("providerId", providerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toInquiryOrNull() }
                    .sortedByDescending { it.createdAt }
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    suspend fun createInquiry(
        propertyId: String,
        propertyTitle: String,
        providerId: String,
        studentId: String,
        studentName: String,
        message: String
    ): String {
        val docId = firestore.collection("inquiries").document().id
        val now = System.currentTimeMillis()
        val payload = hashMapOf(
            "propertyId" to propertyId,
            "propertyTitle" to propertyTitle,
            "providerId" to providerId,
            "studentId" to studentId,
            "studentName" to studentName,
            "message" to message,
            "createdAt" to now,
            "inquiryStatus" to InquiryThreadStatus.PENDING.name
        )
        firestore.collection("inquiries").document(docId).set(payload).await()
        return docId
    }

    suspend fun updateInquiryStatus(inquiryId: String, status: InquiryThreadStatus) {
        firestore.collection("inquiries").document(inquiryId)
            .update(
                mapOf(
                    "inquiryStatus" to status.name
                )
            )
            .await()
    }
}
