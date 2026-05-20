package com.example.nestore_15.data.model

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType = NotificationType.GENERAL
)

enum class NotificationType {
    LISTING_MATCH,
    RESERVATION,
    GENERAL
}
