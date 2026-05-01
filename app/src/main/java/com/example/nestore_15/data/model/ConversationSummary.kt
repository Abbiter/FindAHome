package com.example.nestore_15.data.model

data class ConversationSummary(
    val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val propertyImageUrl: String,
    val participants: List<String>,
    val studentId: String,
    val providerId: String,
    val studentName: String,
    val providerName: String,
    val lastMessage: String,
    val lastUpdated: Long
) {
    fun otherParticipantName(currentUserId: String): String {
        return if (currentUserId == studentId) providerName else studentName
    }
}
