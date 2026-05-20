package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.ui.components.ListingImage
import com.example.nestore_15.ui.components.FindAHomeTopAppBar
import com.example.nestore_15.ui.components.OverlayLoading
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.SecondaryGreenButton
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.ImageShape
import com.example.nestore_15.viewmodel.ProviderDashboardUiState

@Composable
fun ProviderHomeScreen(
    uiState: ProviderDashboardUiState,
    onAddProperty: () -> Unit,
    onManageListings: () -> Unit,
    onMessages: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    onListingClick: (Listing) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProperty,
                containerColor = FindAHomeColors.OrangeAccent,
                contentColor = FindAHomeColors.TextOnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add property")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(FindAHomeColors.BackgroundSoft)
        ) {
            FindAHomeTopAppBar(
                title = "Provider Dashboard",
                showNotifications = true,
                onNotifications = onNotifications,
                actions = {
                    androidx.compose.material3.TextButton(onClick = onProfile) {
                        Text("Profile", color = FindAHomeColors.TextOnPrimary)
                    }
                }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip("Total", uiState.stats.totalListings.toString(), Modifier.weight(1f))
                StatChip("Active", uiState.stats.activeListings.toString(), Modifier.weight(1f))
                StatChip("Reserved", uiState.stats.reservedListings.toString(), Modifier.weight(1f))
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryGreenButton("Manage listings", onManageListings, Modifier.weight(1f))
                PrimaryOrangeButton("Messages", onMessages, Modifier.weight(1f))
            }

            Text(
                "Your listings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FindAHomeColors.PrimaryDarkBlue,
                modifier = Modifier.padding(16.dp)
            )

            Box(Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    OverlayLoading(true)
                } else if (uiState.listingsPreview.isEmpty()) {
                    Text(
                        "No listings yet — tap + to add your first property.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = FindAHomeColors.TextSecondary
                    )
                    PrimaryOrangeButton(
                        "Add Property",
                        onAddProperty,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                            .fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.listingsPreview, key = { it.id }) { listing ->
                            ProviderListingRow(listing) { onListingClick(listing) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = FindAHomeColors.CardSurface,
        shadowElevation = 4.dp
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FindAHomeColors.OrangeAccent)
            Text(label, style = MaterialTheme.typography.labelSmall, color = FindAHomeColors.TextSecondary)
        }
    }
}

@Composable
private fun ProviderListingRow(listing: Listing, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListingImage(
                imageRef = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .width(88.dp)
                    .height(72.dp)
                    .clip(ImageShape),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(listing.location, style = MaterialTheme.typography.bodySmall, color = FindAHomeColors.TextSecondary)
            }
            if (listing.isReserved) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FindAHomeColors.PrimaryDarkBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        "RENTED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FindAHomeColors.PrimaryDarkBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
