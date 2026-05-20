package com.example.nestore_15.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.components.FindAHomeCenterTopBar
import com.example.nestore_15.ui.components.ProviderInfoCard
import com.example.nestore_15.ui.screens.ProviderProfileUi
import com.example.nestore_15.ui.components.ListingImage
import com.example.nestore_15.ui.components.OverlayLoading
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.SecondaryGreenButton
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.ImageShape
import com.example.nestore_15.viewmodel.ListingDetailUiState

data class ListingDetailsUi(
    val id: String,
    val title: String,
    val location: String,
    val priceText: String,
    val priceBwp: Double,
    val roomCount: Int,
    val description: String,
    val availabilityStatus: String,
    val isReserved: Boolean,
    val imageUrls: List<String>,
    val ownerId: String,
    val reservedByCurrentUser: Boolean = false,
    val reservationRef: String = "",
    val provider: ProviderProfileUi? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListingDetailsScreen(
    uiState: ListingDetailUiState,
    onBack: () -> Unit,
    onReserveProperty: (ListingDetailsUi) -> Unit,
    onContactProvider: (ListingDetailsUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeCenterTopBar(title = "Listing Details", onBack = onBack)
        when (uiState) {
            ListingDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                OverlayLoading(visible = true)
            }
            is ListingDetailUiState.Error -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.message, color = FindAHomeColors.TextSecondary)
            }
            is ListingDetailUiState.Ready -> {
                val detail = uiState.detail
                Column(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    GallerySection(detail.imageUrls, detail.title)
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        AvailabilityBadge(detail.availabilityStatus, detail.isReserved)
                        if (detail.reservedByCurrentUser) {
                            Text(
                                "You reserved this property${if (detail.reservationRef.isNotBlank()) " · Ref ${detail.reservationRef}" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FindAHomeColors.GreenAccent,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            detail.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = FindAHomeColors.PrimaryDarkBlue
                        )
                        Text(
                            detail.priceText,
                            style = MaterialTheme.typography.titleLarge,
                            color = FindAHomeColors.OrangeAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        DetailInfoRow(Icons.Default.LocationOn, detail.location)
                        DetailInfoRow(Icons.Default.Bed, "${detail.roomCount} room(s)")
                        detail.provider?.let { provider ->
                            Spacer(Modifier.height(16.dp))
                            ProviderInfoCard(provider)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "About this home",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FindAHomeColors.PrimaryDarkBlue
                        )
                        Text(
                            detail.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FindAHomeColors.TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Column(Modifier.padding(20.dp)) {
                    PrimaryOrangeButton(
                        text = if (detail.reservedByCurrentUser) "Already reserved" else "Reserve Property",
                        onClick = { onReserveProperty(detail) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !detail.isReserved && !detail.reservedByCurrentUser
                    )
                    Spacer(Modifier.height(10.dp))
                    SecondaryGreenButton(
                        text = "Contact Provider",
                        onClick = { onContactProvider(detail) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GallerySection(imageUrls: List<String>, title: String) {
    val images = imageUrls.filter { it.isNotBlank() }.ifEmpty { listOf("") }
    val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })
    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            ListingImage(
                imageRef = images[page.coerceIn(images.indices)],
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
                contentScale = ContentScale.Crop
            )
        }
        if (images.size > 1) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(images.size) { index ->
                    Box(
                        Modifier
                            .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) FindAHomeColors.OrangeAccent
                                else FindAHomeColors.TextOnPrimary.copy(alpha = 0.6f)
                            )
                    )
                }
            }
        }
    }
    if (images.size > 1) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(images) { index, url ->
                Card(
                    shape = ImageShape,
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.size(72.dp)
                ) {
                    ListingImage(
                        imageRef = url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailabilityBadge(status: String, isReserved: Boolean) {
    val bg = if (isReserved) FindAHomeColors.PendingOrange.copy(alpha = 0.15f)
    else FindAHomeColors.VerifiedGreen.copy(alpha = 0.15f)
    val fg = if (isReserved) FindAHomeColors.OrangeAccent else FindAHomeColors.GreenAccent
    Surface(color = bg, shape = CardShape) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = FindAHomeColors.ImageBorder)
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = FindAHomeColors.PrimaryText,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
