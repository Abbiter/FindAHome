package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestore_15.data.model.ConversationSummary
import com.example.nestore_15.ui.components.FindAHomeCenterTopBar
import com.example.nestore_15.ui.components.ListingImage
import com.example.nestore_15.ui.components.OverlayLoading
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.ImageShape
import com.example.nestore_15.viewmodel.ConversationsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsScreen(
    uiState: ConversationsUiState,
    currentUserId: String,
    onBack: () -> Unit,
    onConversationClick: (ConversationSummary) -> Unit,
    onBrowseListings: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeCenterTopBar(title = "Messages", onBack = onBack)
        when (uiState) {
            ConversationsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                OverlayLoading(visible = true)
            }
            is ConversationsUiState.Error -> ConversationsEmptyContent(
                showError = true,
                errorMessage = uiState.message,
                onBrowseListings = onBrowseListings,
                onRefresh = onRefresh
            )
            is ConversationsUiState.Ready -> {
                if (uiState.conversations.isEmpty()) {
                    ConversationsEmptyContent(
                        showError = false,
                        onBrowseListings = onBrowseListings,
                        onRefresh = onRefresh
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.conversations, key = { it.id }) { conversation ->
                            ConversationRow(
                                conversation = conversation,
                                currentUserId = currentUserId,
                                onClick = { onConversationClick(conversation) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: ConversationSummary,
    currentUserId: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ListingImage(
                imageRef = conversation.propertyImageUrl,
                contentDescription = conversation.propertyTitle,
                modifier = Modifier
                    .size(56.dp)
                    .background(FindAHomeColors.BackgroundSoft, ImageShape),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    conversation.otherParticipantName(currentUserId).ifBlank { "Host" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    conversation.propertyTitle.ifBlank { "Property" },
                    style = MaterialTheme.typography.bodySmall,
                    color = FindAHomeColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    conversation.lastMessage.ifBlank { "No messages yet" },
                    style = MaterialTheme.typography.bodySmall,
                    color = FindAHomeColors.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conversation.lastUpdated)),
                style = MaterialTheme.typography.labelSmall,
                color = FindAHomeColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ConversationsEmptyContent(
    showError: Boolean,
    errorMessage: String = "",
    onBrowseListings: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = FindAHomeColors.ImageBorder.copy(alpha = 0.5f)
        )
        Text(
            if (showError) "Could not load messages" else "No conversations yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = FindAHomeColors.PrimaryDarkBlue,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            if (showError) errorMessage
            else "Messages with property owners will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = FindAHomeColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        PrimaryOrangeButton(
            text = "Browse Listings",
            onClick = onBrowseListings,
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.material3.TextButton(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Refresh", color = FindAHomeColors.PrimaryDarkBlue)
        }
    }
}
