package com.example.nestore_15.data.model

enum class InquiryThreadStatus {
    PENDING,
    RESPONDED;

    companion object {
        fun fromFirestore(value: String?): InquiryThreadStatus =
            runCatching { valueOf(value ?: "") }.getOrDefault(PENDING)
    }
}

data class Inquiry(
    val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val providerId: String,
    val studentId: String,
    val studentName: String,
    val message: String,
    val createdAt: Long,
    val inquiryStatus: InquiryThreadStatus
)
