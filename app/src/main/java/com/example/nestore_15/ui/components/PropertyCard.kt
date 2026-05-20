package com.example.nestore_15.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.ImageShape
import java.util.Locale

@Composable
fun PropertyCard(
    listing: Listing,
    onClick: () -> Unit,
    onReserve: () -> Unit,
    onInquire: () -> Unit,
    modifier: Modifier = Modifier,
    priceLabel: (Double) -> String = { price ->
        val formatted = if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
        "P$formatted / month"
    }
) {
    val blocked = listing.isReserved
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(0.5.dp, FindAHomeColors.ImageBorder.copy(alpha = 0.15f))
    ) {
        Column {
            ListingImage(
                imageRef = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(ImageShape)
                    .padding(10.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (listing.isReserved) {
                    Surface(
                        color = FindAHomeColors.PendingOrange.copy(alpha = 0.15f),
                        shape = CardShape
                    ) {
                        Text(
                            "Reserved",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = FindAHomeColors.OrangeAccent
                        )
                    }
                }
                Text(
                    listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    priceLabel(listing.priceBwp),
                    style = MaterialTheme.typography.titleSmall,
                    color = FindAHomeColors.OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    listing.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = FindAHomeColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Available: ${listing.availabilityDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FindAHomeColors.TextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryOrangeButton(
                        text = "Reserve",
                        onClick = onReserve,
                        modifier = Modifier.weight(1f),
                        enabled = !blocked
                    )
                    SecondaryGreenButton(
                        text = "Inquire",
                        onClick = onInquire,
                        modifier = Modifier.weight(1f),
                        enabled = !blocked
                    )
                }
            }
        }
    }
}
