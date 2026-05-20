package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.ui.animation.listItemAnimation
import com.example.nestore_15.ui.components.FindAHomeTopAppBar
import com.example.nestore_15.ui.components.HeaderActionRow
import com.example.nestore_15.ui.components.ListingsEmptyState
import com.example.nestore_15.ui.components.OverlayLoading
import com.example.nestore_15.ui.components.PropertyCard
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.InputShape
import com.example.nestore_15.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchDone: () -> Unit,
    verificationDotColor: Color,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    onListingClick: (Listing) -> Unit,
    onReserve: (Listing) -> Unit,
    onInquire: (Listing) -> Unit,
    onMapFab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeTopAppBar(
            title = "Find A Home",
            showNotifications = false,
            actions = {
                HeaderActionRow(
                    onNotifications = onNotifications,
                    onProfile = onProfile,
                    verificationColor = verificationDotColor
                )
            }
        )

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Discover campus housing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FindAHomeColors.PrimaryDarkBlue,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by location…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = InputShape,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FindAHomeColors.CardSurface,
                    unfocusedContainerColor = FindAHomeColors.CardSurface,
                    focusedBorderColor = FindAHomeColors.OrangeAccent
                )
            )
        }

        Box(Modifier.fillMaxSize()) {
            when (uiState) {
                HomeUiState.Loading -> OverlayLoading(visible = true)
                HomeUiState.Error -> {
                    ListingsEmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        databaseEmpty = true
                    )
                }
                is HomeUiState.Success -> {
                    if (uiState.listings.isEmpty()) {
                        ListingsEmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(uiState.listings, key = { _, it -> it.id }) { index, listing ->
                                PropertyCard(
                                    listing = listing,
                                    onClick = { onListingClick(listing) },
                                    onReserve = { onReserve(listing) },
                                    onInquire = { onInquire(listing) },
                                    modifier = Modifier.listItemAnimation(index)
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onMapFab,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = FindAHomeColors.GreenAccent,
                contentColor = FindAHomeColors.TextOnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = "Map")
            }
        }
    }
}
