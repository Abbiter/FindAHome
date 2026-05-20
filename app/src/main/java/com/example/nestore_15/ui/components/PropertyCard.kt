package com.example.nestore_15.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.util.ListingImageResolver
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.ImageShape
import java.util.Locale

@Composable
fun PropertyCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    priceLabel: (Double) -> String = { price ->
        val formatted = if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
        "P$formatted"
    }
) {
    val imageRef = ListingImageResolver.displayRefForListing(listing)
    val statusLabel = if (listing.isReserved) "RENTED" else "AVAILABLE"
    val statusBg = if (listing.isReserved) {
        FindAHomeColors.PendingOrange.copy(alpha = 0.92f)
    } else {
        FindAHomeColors.GreenAccent.copy(alpha = 0.92f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, FindAHomeColors.ImageBorder.copy(alpha = 0.12f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                ListingImage(
                    imageRef = imageRef,
                    listingId = listing.id,
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(ImageShape),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = statusBg,
                    shape = CardShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FindAHomeColors.TextOnPrimary
                    )
                }
                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save listing",
                            tint = if (isFavorite) FindAHomeColors.OrangeAccent else FindAHomeColors.TextOnPrimary
                        )
                    }
                }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                Text(
                    listing.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FindAHomeColors.PrimaryDarkBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.titleSmall.lineHeight
                )
                Text(
                    "${priceLabel(listing.priceBwp)} / month",
                    style = MaterialTheme.typography.titleMedium,
                    color = FindAHomeColors.OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    listing.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = FindAHomeColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
