package com.example.nestore_15.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.Property
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.data.repository.toListing
import com.example.nestore_15.data.util.LocalListingImages
import com.example.nestore_15.ui.screens.ProviderProfileUi
import com.example.nestore_15.ui.screens.toProviderProfileUi
import com.example.nestore_15.ui.screens.ListingDetailsUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class ListingDetailUiState {
    data object Loading : ListingDetailUiState()
    data class Ready(val detail: ListingDetailsUi, val listing: Listing) : ListingDetailUiState()
    data class Error(val message: String) : ListingDetailUiState()
}

class ListingDetailViewModel(
    private val listingId: String,
    private val propertyRepository: PropertyRepository,
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val currentUserId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingDetailUiState>(ListingDetailUiState.Loading)
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ListingDetailUiState.Loading
            val result = runCatching {
                val property = propertyRepository.getProperty(listingId)
                val listing: Listing
                val detail: ListingDetailsUi
                if (property != null) {
                    listing = property.toListing()
                    detail = property.toDetailsUi(loadProvider(property.ownerId))
                } else {
                    val legacy = listingRepository.getListingById(listingId)
                        ?: throw IllegalStateException("Listing not found")
                    listing = legacy
                    detail = legacy.toDetailsUi(loadProvider(legacy.ownerId))
                }
                listing to detail
            }
            _uiState.value = result.fold(
                onSuccess = { (listing, detail) ->
                    ListingDetailUiState.Ready(detail, listing)
                },
                onFailure = {
                    ListingDetailUiState.Error(it.message ?: "Could not load property")
                }
            )
        }
    }

    private suspend fun loadProvider(ownerId: String): ProviderProfileUi? =
        userRepository.getUser(ownerId)?.toProviderProfileUi()

    private fun Property.toDetailsUi(provider: ProviderProfileUi?): ListingDetailsUi {
        val statusLabel = when (availabilityStatus) {
            PropertyStatus.AVAILABLE -> "AVAILABLE"
            PropertyStatus.PENDING -> "PENDING"
            PropertyStatus.RENTED -> "RENTED"
        }
        val priceText = formatPrice(priceBwp)
        return ListingDetailsUi(
            id = id,
            title = title,
            location = location,
            priceText = priceText,
            priceBwp = priceBwp,
            roomCount = roomCount,
            description = description.ifBlank { "No description provided." },
            availabilityStatus = statusLabel,
            isReserved = availabilityStatus == PropertyStatus.RENTED,
            imageUrls = imageUrls.ifEmpty { listOf(LocalListingImages.keyForListingId(id)) },
            ownerId = ownerId,
            reservedByCurrentUser = currentUserId != null && reservedBy == currentUserId,
            reservationRef = reservationRef,
            provider = provider
        )
    }

    private fun Listing.toDetailsUi(provider: ProviderProfileUi?): ListingDetailsUi {
        val statusLabel = if (isReserved) "RENTED" else "AVAILABLE"
        return ListingDetailsUi(
            id = id,
            title = title,
            location = location,
            priceText = formatPrice(priceBwp),
            priceBwp = priceBwp,
            roomCount = 1,
            description = "Student housing near campus. Contact the provider for more details.",
            availabilityStatus = statusLabel,
            isReserved = isReserved,
            imageUrls = imageUrls.ifEmpty { listOf(imageUrl).filter { it.isNotBlank() } },
            ownerId = ownerId,
            reservedByCurrentUser = currentUserId != null && isReservedBy(currentUserId),
            reservationRef = reservationRef,
            provider = provider
        )
    }

    private fun formatPrice(price: Double): String {
        val formatted = if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
        return "P$formatted / month"
    }

    companion object {
        fun factory(listingId: String, currentUserId: String? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ListingDetailViewModel(
                        listingId = listingId,
                        propertyRepository = PropertyRepository(),
                        listingRepository = ListingRepository(),
                        userRepository = UserRepository(),
                        currentUserId = currentUserId
                    ) as T
                }
            }
    }
}
