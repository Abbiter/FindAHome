package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.repository.ListingRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val listings: List<Listing>) : HomeUiState()
    data object Error : HomeUiState()
}

class HomeViewModel(
    private val listingRepository: ListingRepository
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
        listingId: String,
        currentUserId: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val result = runCatching {
                listingRepository.reserveListing(listingId, currentUserId)
            }
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
        fun factory(listingRepository: ListingRepository = ListingRepository()): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(listingRepository) as T
                }
            }
    }
}
