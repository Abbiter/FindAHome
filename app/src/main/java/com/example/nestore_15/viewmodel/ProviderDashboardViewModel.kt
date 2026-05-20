package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.InquiryRepository
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.repository.toListing
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProviderDashboardStats(
    val totalListings: Int = 0,
    val activeListings: Int = 0,
    val reservedListings: Int = 0,
    val inquiriesCount: Int = 0
)

data class ProviderDashboardUiState(
    val isLoading: Boolean = true,
    val stats: ProviderDashboardStats = ProviderDashboardStats(),
    val listingsPreview: List<Listing> = emptyList()
)

class ProviderDashboardViewModel(
    private val propertyRepository: PropertyRepository,
    private val inquiryRepository: InquiryRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ProviderDashboardUiState())
    val uiState: LiveData<ProviderDashboardUiState> = _uiState

    private var dashboardJob: Job? = null
    private var currentOwnerId: String? = null

    fun loadDashboard(ownerId: String) {
        if (currentOwnerId == ownerId && dashboardJob != null) return
        currentOwnerId = ownerId
        dashboardJob?.cancel()
        _uiState.value = ProviderDashboardUiState(isLoading = true)

        dashboardJob = viewModelScope.launch {
            combine(
                propertyRepository.observePropertiesByOwner(ownerId),
                inquiryRepository.observeInquiriesForProvider(ownerId)
            ) { properties, inquiries ->
                val preview = properties
                    .sortedByDescending { it.updatedAt ?: it.createdAt ?: 0L }
                    .take(6)
                    .map { it.toListing() }

                val stats = ProviderDashboardStats(
                    totalListings = properties.size,
                    activeListings = properties.count { it.availabilityStatus == PropertyStatus.AVAILABLE },
                    reservedListings = properties.count { it.availabilityStatus == PropertyStatus.RENTED },
                    inquiriesCount = inquiries.size
                )

                ProviderDashboardUiState(
                    isLoading = false,
                    stats = stats,
                    listingsPreview = preview
                )
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    companion object {
        fun factory(
            propertyRepository: PropertyRepository = PropertyRepository(),
            inquiryRepository: InquiryRepository = InquiryRepository()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProviderDashboardViewModel(propertyRepository, inquiryRepository) as T
                }
            }
    }
}
