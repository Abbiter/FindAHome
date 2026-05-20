package com.example.nestore_15.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = FindAHomeColors.ImageBorder.copy(alpha = 0.5f)
        )
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = FindAHomeColors.PrimaryText,
            modifier = Modifier.padding(top = 16.dp),
            textAlign = TextAlign.Center
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = FindAHomeColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        action()
    }
}

@Composable
fun FavoritesEmptyState(
    modifier: Modifier = Modifier,
    title: String = "No saved homes yet",
    message: String = "Tap the heart on listings you love. Your favorites will appear here."
) {
    EmptyState(
        icon = Icons.Outlined.FavoriteBorder,
        title = title,
        message = message,
        modifier = modifier
    )
}

@Composable
fun ListingsEmptyState(
    modifier: Modifier = Modifier,
    databaseEmpty: Boolean = true
) {
    EmptyState(
        icon = Icons.Outlined.SearchOff,
        title = if (databaseEmpty) "No homes in the database yet" else "No listings match your search",
        message = if (databaseEmpty) {
            "Listings are loaded from Firestore. Run scripts/seed.js against project findahome-50b4d, " +
                "publish firestore.rules, then sign in and reopen the app."
        } else {
            "Try adjusting your location filter or check back soon for new homes."
        },
        modifier = modifier
    )
}

@Composable
fun WelcomeEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Home,
        title = "Discover your next home",
        message = "Browse verified campus housing from trusted providers.",
        modifier = modifier
    )
}
