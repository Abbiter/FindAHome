package com.example.nestore_15.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.ListingFilterPreferences
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.data.preferences.SavedListingsStore
import com.example.nestore_15.ui.navigation.FloatingBottomNavBar
import com.example.nestore_15.ui.navigation.StudentNavHost
import com.example.nestore_15.ui.navigation.StudentTab
import com.example.nestore_15.viewmodel.HomeUiState
import com.example.nestore_15.viewmodel.MyListingsViewModel
import com.example.nestore_15.viewmodel.ProfileUiState
import kotlinx.coroutines.flow.flowOf

@Composable
fun MainScreen(
    navController: NavHostController,
    homeUiState: HomeUiState,
    profileUiState: ProfileUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterPreferences: ListingFilterPreferences,
    onApplyFilters: (minPrice: Double?, maxPrice: Double?, location: String?) -> Unit,
    verificationDotColor: Color,
    onNotifications: () -> Unit,
    onProfileHeader: () -> Unit,
    onMapFab: () -> Unit,
    onOpenMessages: () -> Unit,
    onEditProfile: () -> Unit,
    onVerify: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    currentUserId: String?,
    onContactProvider: (ListingDetailsUi) -> Unit,
    onToggleFavorite: (Listing) -> Unit,
    savedListingsStore: SavedListingsStore,
    notificationStore: AppNotificationStore,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(StudentTab.HOME) }
    val savedIdsFlow = if (currentUserId.isNullOrBlank()) {
        flowOf(emptySet())
    } else {
        savedListingsStore.savedIdsFlow(currentUserId)
    }
    val savedIds by savedIdsFlow.collectAsState(initial = emptySet())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            FloatingBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == StudentTab.MESSAGES) {
                        onOpenMessages()
                    } else {
                        selectedTab = tab
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                StudentTab.HOME -> StudentNavHost(
                    navController = navController,
                    homeUiState = homeUiState,
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    filterPreferences = filterPreferences,
                    onApplyFilters = onApplyFilters,
                    verificationDotColor = verificationDotColor,
                    onNotifications = onNotifications,
                    onProfile = onProfileHeader,
                    onMapFab = onMapFab,
                    currentUserId = currentUserId,
                    onContactProvider = onContactProvider,
                    onToggleFavorite = onToggleFavorite,
                    savedListingIds = savedIds,
                    notificationStore = notificationStore
                )
                StudentTab.FAVORITES -> {
                    if (!currentUserId.isNullOrBlank()) {
                        val myVm: MyListingsViewModel = viewModel(
                            factory = MyListingsViewModel.factory(currentUserId, savedListingsStore)
                        )
                        val myState by myVm.uiState.collectAsState()
                        MyListingsScreen(
                            uiState = myState,
                            savedIds = savedIds,
                            onListingClick = { listing ->
                                selectedTab = StudentTab.HOME
                                navController.navigate(
                                    com.example.nestore_15.ui.navigation.StudentNavRoutes.listingDetails(listing.id)
                                )
                            },
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
                StudentTab.PROFILE -> ProfileScreen(
                    uiState = profileUiState,
                    onEditProfile = onEditProfile,
                    onVerify = onVerify,
                    onLogout = onLogout,
                    onChangePassword = onChangePassword,
                    showBack = false
                )
                StudentTab.MESSAGES -> Unit
            }
        }
    }
}
