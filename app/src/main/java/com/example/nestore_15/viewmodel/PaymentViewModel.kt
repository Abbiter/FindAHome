package com.example.nestore_15.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.repository.toListing
import com.example.nestore_15.ui.screens.PaymentSummaryUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class PaymentUiState {
    data object Loading : PaymentUiState()
    data class Form(val summary: PaymentSummaryUi, val listing: Listing) : PaymentUiState()
    data class Success(val reservationRef: String, val summary: PaymentSummaryUi) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

class PaymentViewModel(
    private val listingId: String,
    private val propertyRepository: PropertyRepository,
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Loading)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private var cachedListing: Listing? = null

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            val result = runCatching {
                val property = propertyRepository.getProperty(listingId)
                val listing = if (property != null) {
                    property.toListing()
                } else {
                    listingRepository.getListingById(listingId)
                        ?: throw IllegalStateException("Listing not found")
                }
                cachedListing = listing
                PaymentSummaryUi(
                    listingId = listing.id,
                    title = listing.title,
                    location = listing.location,
                    priceText = formatPrice(listing.priceBwp),
                    imageUrl = listing.imageUrl
                ) to listing
            }
            _uiState.value = result.fold(
                onSuccess = { (summary, listing) -> PaymentUiState.Form(summary, listing) },
                onFailure = { PaymentUiState.Error(it.message ?: "Could not load listing") }
            )
        }
    }

    fun confirmPayment(currentUserId: String) {
        val listing = cachedListing ?: return
        viewModelScope.launch {
            val current = _uiState.value
            if (current !is PaymentUiState.Form) return@launch
            _uiState.value = PaymentUiState.Loading
            val result = runCatching {
                if (listing.isPropertyListing) {
                    propertyRepository.reservePropertyAsRented(listing.id, currentUserId)
                } else {
                    listingRepository.reserveListing(listing.id, currentUserId)
                }
            }
            _uiState.value = result.fold(
                onSuccess = { ref ->
                    PaymentUiState.Success(ref, current.summary)
                },
                onFailure = {
                    PaymentUiState.Error(it.message ?: "Payment could not be completed")
                }
            )
        }
    }

    private fun formatPrice(price: Double): String {
        val formatted = if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
        return "P$formatted / month"
    }

    companion object {
        fun factory(listingId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PaymentViewModel(
                        listingId = listingId,
                        propertyRepository = PropertyRepository(),
                        listingRepository = ListingRepository()
                    ) as T
                }
            }
    }
}
