package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.repository.ListingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProviderDashboardStats(
    val totalListings: Int = 0,
    val activeListings: Int = 0,
    val inquiriesCount: Int = 0
)

data class ProviderDashboardUiState(
    val isLoading: Boolean = true,
    val stats: ProviderDashboardStats = ProviderDashboardStats(),
    val listingsPreview: List<Listing> = emptyList()
)

class ProviderDashboardViewModel(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ProviderDashboardUiState())
    val uiState: LiveData<ProviderDashboardUiState> = _uiState

    private var listingsJob: Job? = null
    private var currentOwnerId: String? = null

    fun loadDashboard(ownerId: String) {
        if (currentOwnerId == ownerId && listingsJob != null) return
        currentOwnerId = ownerId
        listingsJob?.cancel()
        _uiState.value = ProviderDashboardUiState(isLoading = true)

        listingsJob = viewModelScope.launch {
            listingRepository.getAllListings().collectLatest { allListings ->
                val ownedListings = allListings
                    .filter { it.ownerId == ownerId }
                    .sortedByDescending { it.availabilityDate }

                val stats = buildStats(ownedListings)
                val preview = ownedListings.take(6)

                _uiState.value = ProviderDashboardUiState(
                    isLoading = false,
                    stats = stats,
                    listingsPreview = preview
                )
            }
        }
    }

    private fun buildStats(listings: List<Listing>): ProviderDashboardStats {
        val today = todayString()
        val active = listings.count { listing ->
            val isPending = listing.availabilityDate.isNotBlank() && listing.availabilityDate > today
            !listing.isReserved && !isPending
        }
        val inquiries = listings.count { it.isReserved }

        return ProviderDashboardStats(
            totalListings = listings.size,
            activeListings = active,
            inquiriesCount = inquiries
        )
    }

    private fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    companion object {
        fun factory(
            listingRepository: ListingRepository = ListingRepository()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProviderDashboardViewModel(listingRepository) as T
                }
            }
    }
}
