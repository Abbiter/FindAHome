package com.example.nestore_15.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.preferences.SavedListingsStore
import com.example.nestore_15.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MyListingsUiState(
    val saved: List<Listing> = emptyList(),
    val reserved: List<Listing> = emptyList(),
    val isLoading: Boolean = true
)

class MyListingsViewModel(
    private val userId: String,
    private val listingRepository: ListingRepository,
    private val savedListingsStore: SavedListingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyListingsUiState())
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                listingRepository.getAllListings(),
                listingRepository.observeReservedByUser(userId),
                savedListingsStore.savedIdsFlow(userId)
            ) { all, reserved, savedIds ->
                val saved = all.filter {
                    savedIds.contains(it.id) && (it.reservedBy.isBlank() || it.reservedBy == userId)
                }
                MyListingsUiState(
                    saved = saved,
                    reserved = reserved,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    companion object {
        fun factory(
            userId: String,
            savedListingsStore: SavedListingsStore
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MyListingsViewModel(
                        userId = userId,
                        listingRepository = ListingRepository(),
                        savedListingsStore = savedListingsStore
                    ) as T
                }
            }
    }
}
