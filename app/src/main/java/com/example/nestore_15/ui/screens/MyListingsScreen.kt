package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.ui.components.FavoritesEmptyState
import com.example.nestore_15.ui.components.FindAHomeTopAppBar
import com.example.nestore_15.ui.components.OverlayLoading
import com.example.nestore_15.ui.components.PropertyCard
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.viewmodel.MyListingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    uiState: MyListingsUiState,
    savedIds: Set<String>,
    onListingClick: (Listing) -> Unit,
    onToggleFavorite: (Listing) -> Unit,
    modifier: Modifier = Modifier
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Saved", "Reserved")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeTopAppBar(title = "My Homes")
        PrimaryTabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(label) }
                )
            }
        }
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                OverlayLoading(visible = true)
            }
        } else when (tabIndex) {
            0 -> ListingListSection(
                listings = uiState.saved,
                emptyTitle = "No saved homes yet",
                emptyMessage = "Tap the heart on any listing to save it here.",
                savedIds = savedIds,
                onListingClick = onListingClick,
                onToggleFavorite = onToggleFavorite
            )
            1 -> ListingListSection(
                listings = uiState.reserved,
                emptyTitle = "No reservations yet",
                emptyMessage = "Properties you reserve will appear here with confirmation details.",
                savedIds = savedIds,
                onListingClick = onListingClick,
                onToggleFavorite = onToggleFavorite,
                showReservedHint = true
            )
        }
    }
}

@Composable
private fun ListingListSection(
    listings: List<Listing>,
    emptyTitle: String,
    emptyMessage: String,
    savedIds: Set<String>,
    onListingClick: (Listing) -> Unit,
    onToggleFavorite: (Listing) -> Unit,
    showReservedHint: Boolean = false
) {
    if (listings.isEmpty()) {
        FavoritesEmptyState(
            modifier = Modifier.fillMaxSize(),
            title = emptyTitle,
            message = emptyMessage
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(listings, key = { it.id }) { listing ->
                Column {
                    PropertyCard(
                        listing = listing,
                        onClick = { onListingClick(listing) },
                        isFavorite = savedIds.contains(listing.id),
                        onToggleFavorite = { onToggleFavorite(listing) }
                    )
                    if (showReservedHint && listing.reservationRef.isNotBlank()) {
                        Text(
                            "Ref: ${listing.reservationRef}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FindAHomeColors.GreenAccent,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
