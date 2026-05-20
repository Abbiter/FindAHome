package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.components.FindAHomeCenterTopBar
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeCenterTopBar(title = "Settings", onBack = onBack)
        Text(
            "Preferences",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(20.dp),
            color = FindAHomeColors.PrimaryDarkBlue
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface)
        ) {
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Manage alerts for new listings") },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                modifier = Modifier.padding(4.dp),
                overlineContent = null
            )
            androidx.compose.material3.HorizontalDivider()
            ListItem(
                headlineContent = { Text("Privacy & Security") },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) }
            )
        }
    }
}
