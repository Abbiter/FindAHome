package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.example.nestore_15.ui.components.FavoritesEmptyState
import com.example.nestore_15.ui.components.FindAHomeTopAppBar
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeTopAppBar(title = "Saved Listings")
        Text(
            "Your favorite homes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FindAHomeColors.PrimaryDarkBlue,
            modifier = Modifier.padding(16.dp)
        )
        FavoritesEmptyState(modifier = Modifier.fillMaxSize())
    }
}
