package com.example.nestore_15.data.model

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType = NotificationType.GENERAL,
    val subtitle: String = ""
)

enum class NotificationType {
    LISTING_MATCH,
    RESERVATION,
    RESERVATION_RECEIVED,
    CHAT,
    GENERAL;

    fun displayLabel(): String = when (this) {
        LISTING_MATCH -> "New listing"
        RESERVATION -> "Your reservation"
        RESERVATION_RECEIVED -> "Property reserved"
        CHAT -> "Message"
        GENERAL -> "Update"
    }
}
