package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.preferences.ListingFilterPreferencesStore
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val listings: List<Listing>) : HomeUiState()
    data object Error : HomeUiState()
}

class HomeViewModel(
    private val listingRepository: ListingRepository,
    private val propertyRepository: PropertyRepository,
    private val filterStore: ListingFilterPreferencesStore
) : ViewModel() {

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val uiState: LiveData<HomeUiState> = _uiState

    private val searchQuery = MutableStateFlow("")

    init {
        observeListings()
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun reserveListing(
        listing: Listing,
        currentUserId: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val result = runCatching {
                if (listing.isPropertyListing) {
                    propertyRepository.reservePropertyAsRented(listing.id, currentUserId)
                } else {
                    listingRepository.reserveListing(listing.id, currentUserId)
                }
            }
            onResult(result)
        }
    }

    private fun observeListings() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            runCatching {
                combine(
                    listingRepository.getBrowsableListings(),
                    filterStore.preferencesFlow,
                    searchQuery
                ) { listings, prefs, search ->
                    val query = search.trim().ifBlank { prefs.location.orEmpty() }
                    listingRepository.applyBrowseFilters(
                        listings = listings,
                        minPriceBwp = prefs.minPriceBwp,
                        maxPriceBwp = prefs.maxPriceBwp,
                        locationQuery = query
                    )
                }.collectLatest { filtered ->
                    _uiState.value = HomeUiState.Success(filtered)
                }
            }.onFailure {
                _uiState.value = HomeUiState.Error
            }
        }
    }

    companion object {
        fun factory(
            listingRepository: ListingRepository = ListingRepository(),
            propertyRepository: PropertyRepository = PropertyRepository(),
            filterStore: ListingFilterPreferencesStore
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        listingRepository,
                        propertyRepository,
                        filterStore
                    ) as T
                }
            }
    }
}
