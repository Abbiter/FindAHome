package com.example.nestore_15.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.screens.ProviderProfileUi
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun ProviderInfoCard(
    provider: ProviderProfileUi,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Property host",
                style = MaterialTheme.typography.labelMedium,
                color = FindAHomeColors.TextSecondary
            )
            Text(
                provider.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FindAHomeColors.PrimaryDarkBlue,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (provider.businessName.isNotBlank()) {
                ProviderInfoRow(Icons.Default.Business, provider.businessName)
            }
            if (provider.phone.isNotBlank()) {
                ProviderInfoRow(Icons.Default.Phone, provider.phone)
            }
            if (provider.email.isNotBlank()) {
                ProviderInfoRow(Icons.Default.Email, provider.email)
            }
            if (provider.contactAddress.isNotBlank()) {
                ProviderInfoRow(Icons.Default.Business, provider.contactAddress)
            }
            if (provider.isVerified) {
                Row(
                    Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = FindAHomeColors.GreenAccent)
                    Text(
                        "Verified provider",
                        style = MaterialTheme.typography.labelMedium,
                        color = FindAHomeColors.GreenAccent,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FindAHomeColors.ImageBorder)
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = FindAHomeColors.PrimaryText,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
