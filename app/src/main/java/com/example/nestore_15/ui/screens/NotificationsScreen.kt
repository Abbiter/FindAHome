package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nestore_15.data.model.AppNotification
import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.ui.components.FindAHomeCenterTopBar
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeCenterTopBar(title = "Notifications", onBack = onBack)
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = FindAHomeColors.ImageBorder.copy(alpha = 0.5f)
                    )
                    Text(
                        "No notifications yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        "Alerts for matching listings and your reservations appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FindAHomeColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCard(item)
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification) {
    val accent = when (notification.type) {
        NotificationType.LISTING_MATCH -> FindAHomeColors.OrangeAccent
        NotificationType.RESERVATION -> FindAHomeColors.GreenAccent
        NotificationType.GENERAL -> FindAHomeColors.PrimaryDarkBlue
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Text(
                notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = FindAHomeColors.PrimaryText,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(Date(notification.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = FindAHomeColors.TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
