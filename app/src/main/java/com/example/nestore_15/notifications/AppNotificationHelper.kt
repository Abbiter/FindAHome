package com.example.nestore_15.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.nestore_15.R
import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.preferences.AppNotificationStore

class AppNotificationHelper(private val context: Context) {

    private val store = AppNotificationStore(context)
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    suspend fun notifyUser(
        userId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        subtitle: String = ""
    ) {
        if (userId.isBlank()) return
        store.add(userId, title, message, type, subtitle)
        postSystemNotification(
            notificationId = (userId + title + message).hashCode(),
            channelId = channelFor(type),
            title = title,
            body = if (subtitle.isNotBlank()) "$message\n$subtitle" else message
        )
    }

    private fun postSystemNotification(
        notificationId: Int,
        channelId: String,
        title: String,
        body: String
    ) {
        if (!canPostNotifications()) return
        ensureChannel(channelId)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun channelFor(type: NotificationType): String = when (type) {
        NotificationType.LISTING_MATCH -> CHANNEL_LISTINGS
        NotificationType.RESERVATION,
        NotificationType.RESERVATION_RECEIVED -> CHANNEL_RESERVATIONS
        NotificationType.CHAT -> CHANNEL_MESSAGES
        NotificationType.GENERAL -> CHANNEL_GENERAL
    }

    private fun ensureChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (notificationManager.getNotificationChannel(channelId) != null) return
        val (name, description) = when (channelId) {
            CHANNEL_RESERVATIONS -> "Reservations" to "Alerts when properties are reserved"
            CHANNEL_MESSAGES -> "Messages" to "New chat messages"
            CHANNEL_LISTINGS -> "Listings" to "New listings matching your filters"
            else -> "General" to "App updates"
        }
        notificationManager.createNotificationChannel(
            NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                this.description = description
            }
        )
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    companion object {
        const val CHANNEL_GENERAL = "findahome_general"
        const val CHANNEL_RESERVATIONS = "findahome_reservations"
        const val CHANNEL_MESSAGES = "findahome_messages"
        const val CHANNEL_LISTINGS = "findahome_listings"
    }
}
