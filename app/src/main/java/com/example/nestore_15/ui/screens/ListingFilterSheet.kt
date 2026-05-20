package com.example.nestore_15.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nestore_15.data.model.ListingFilterPreferences
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.SecondaryGreenButton
import com.example.nestore_15.ui.theme.InputShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingFilterSheet(
    current: ListingFilterPreferences,
    onDismiss: () -> Unit,
    onApply: (minPrice: Double?, maxPrice: Double?, location: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var minPrice by remember(current) { mutableStateOf(current.minPriceBwp?.toInt()?.toString().orEmpty()) }
    var maxPrice by remember(current) { mutableStateOf(current.maxPriceBwp?.toInt()?.toString().orEmpty()) }
    var location by remember(current) { mutableStateOf(current.location.orEmpty()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Filter listings")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location contains") },
                placeholder = { Text("e.g. Gaborone, Block 6") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = InputShape
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = minPrice,
                onValueChange = { minPrice = it.filter { c -> c.isDigit() } },
                label = { Text("Min price (BWP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = InputShape
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = maxPrice,
                onValueChange = { maxPrice = it.filter { c -> c.isDigit() } },
                label = { Text("Max price (BWP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = InputShape
            )
            Spacer(Modifier.height(20.dp))
            PrimaryOrangeButton(
                text = "Apply filters",
                onClick = {
                    onApply(
                        minPrice.toDoubleOrNull(),
                        maxPrice.toDoubleOrNull(),
                        location.trim().ifBlank { null }
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SecondaryGreenButton(
                text = "Clear filters",
                onClick = {
                    onApply(null, null, null)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
