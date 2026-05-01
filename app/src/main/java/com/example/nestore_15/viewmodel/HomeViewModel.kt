package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.repository.InquiryRepository
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.repository.PropertyRepository
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
    private val inquiryRepository: InquiryRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        observeListings()
    }

    fun loadListings() {
        _uiState.value = HomeUiState.Loading
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

    fun submitInquiry(
        listing: Listing,
        message: String,
        studentId: String,
        studentName: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = runCatching {
                inquiryRepository.createInquiry(
                    propertyId = listing.id,
                    propertyTitle = listing.title,
                    providerId = listing.ownerId,
                    studentId = studentId,
                    studentName = studentName,
                    message = message
                )
            }.map { }
            onResult(result)
        }
    }

    private fun observeListings() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            runCatching {
                listingRepository.getAllListings().collectLatest { listings ->
                    _uiState.value = HomeUiState.Success(listings)
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
            inquiryRepository: InquiryRepository = InquiryRepository()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(listingRepository, propertyRepository, inquiryRepository) as T
                }
            }
    }
}
