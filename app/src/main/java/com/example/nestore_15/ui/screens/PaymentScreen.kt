package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.components.FindAHomeCenterTopBar
import com.example.nestore_15.ui.components.ListingImage
import com.example.nestore_15.ui.components.OverlayLoading
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.findAHomeTextFieldColors
import com.example.nestore_15.ui.components.SecondaryGreenButton
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.ImageShape
import com.example.nestore_15.ui.theme.InputShape
import com.example.nestore_15.viewmodel.PaymentUiState

data class PaymentSummaryUi(
    val listingId: String,
    val title: String,
    val location: String,
    val priceText: String,
    val imageUrl: String
)

@Composable
fun PaymentScreen(
    uiState: PaymentUiState,
    onBack: () -> Unit,
    onConfirmPayment: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        FindAHomeCenterTopBar(
            title = when (uiState) {
                is PaymentUiState.Success -> "Payment complete"
                else -> "Checkout"
            },
            onBack = onBack
        )
        when (uiState) {
            PaymentUiState.Loading -> OverlayLoading(visible = true)
            is PaymentUiState.Error -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.message, color = FindAHomeColors.TextSecondary)
                Spacer(Modifier.height(16.dp))
                SecondaryGreenButton("Go back", onBack, Modifier.fillMaxWidth())
            }
            is PaymentUiState.Form -> PaymentFormContent(
                summary = uiState.summary,
                onConfirmPayment = onConfirmPayment
            )
            is PaymentUiState.Success -> PaymentSuccessContent(
                reservationRef = uiState.reservationRef,
                summary = uiState.summary,
                onBackToHome = onBackToHome
            )
        }
    }
}

@Composable
private fun PaymentFormContent(
    summary: PaymentSummaryUi,
    onConfirmPayment: () -> Unit
) {
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Simulation only — no real charges",
            style = MaterialTheme.typography.labelMedium,
            color = FindAHomeColors.OrangeAccent
        )
        Spacer(Modifier.height(16.dp))
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(14.dp)) {
                ListingImage(
                    imageRef = summary.imageUrl,
                    contentDescription = summary.title,
                    modifier = Modifier
                        .size(88.dp)
                        .background(FindAHomeColors.BackgroundSoft, ImageShape),
                    contentScale = ContentScale.Crop
                )
                Column(Modifier.padding(start = 14.dp)) {
                    Text(
                        summary.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FindAHomeColors.PrimaryDarkBlue
                    )
                    Text(
                        summary.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = FindAHomeColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        summary.priceText,
                        style = MaterialTheme.typography.titleSmall,
                        color = FindAHomeColors.OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Card details (demo)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FindAHomeColors.PrimaryDarkBlue
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it.filter { c -> c.isDigit() }.take(16) },
            label = { Text("Card number") },
            placeholder = { Text("4242 4242 4242 4242") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = InputShape,
            colors = paymentFieldColors()
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it.take(5) },
                label = { Text("Expiry") },
                placeholder = { Text("MM/YY") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = InputShape,
                colors = paymentFieldColors()
            )
            Spacer(Modifier.size(12.dp))
            OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = InputShape,
                colors = paymentFieldColors()
            )
        }
        Spacer(Modifier.height(28.dp))
        PrimaryOrangeButton(
            text = "Confirm Payment",
            onClick = onConfirmPayment,
            modifier = Modifier.fillMaxWidth(),
            enabled = cardNumber.length >= 12 && expiry.isNotBlank() && cvv.length >= 3
        )
    }
}

@Composable
private fun PaymentSuccessContent(
    reservationRef: String,
    summary: PaymentSummaryUi,
    onBackToHome: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = FindAHomeColors.GreenAccent,
            modifier = Modifier.size(72.dp)
        )
        Text(
            "Reservation confirmed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = FindAHomeColors.PrimaryDarkBlue,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            summary.title,
            style = MaterialTheme.typography.bodyLarge,
            color = FindAHomeColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            "Reference: $reservationRef",
            style = MaterialTheme.typography.bodyMedium,
            color = FindAHomeColors.PrimaryDarkBlue,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            "This property is now marked as reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = FindAHomeColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        PrimaryOrangeButton(
            text = "Back to Home",
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun paymentFieldColors() = findAHomeTextFieldColors()
