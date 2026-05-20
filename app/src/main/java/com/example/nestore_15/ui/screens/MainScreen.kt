package com.example.nestore_15.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.ui.navigation.FloatingBottomNavBar
import com.example.nestore_15.ui.navigation.StudentTab
import com.example.nestore_15.viewmodel.HomeUiState
import com.example.nestore_15.viewmodel.ProfileUiState

@Composable
fun MainScreen(
    homeUiState: HomeUiState,
    profileUiState: ProfileUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchDone: () -> Unit,
    verificationDotColor: Color,
    onNotifications: () -> Unit,
    onProfileHeader: () -> Unit,
    onListingClick: (Listing) -> Unit,
    onReserve: (Listing) -> Unit,
    onInquire: (Listing) -> Unit,
    onMapFab: () -> Unit,
    onOpenMessages: () -> Unit,
    onEditProfile: () -> Unit,
    onVerify: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(StudentTab.HOME) }

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
                StudentTab.HOME -> HomeScreen(
                    uiState = homeUiState,
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    onSearchDone = onSearchDone,
                    verificationDotColor = verificationDotColor,
                    onNotifications = onNotifications,
                    onProfile = onProfileHeader,
                    onListingClick = onListingClick,
                    onReserve = onReserve,
                    onInquire = onInquire,
                    onMapFab = onMapFab
                )
                StudentTab.FAVORITES -> FavoritesScreen()
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
