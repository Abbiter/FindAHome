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
import androidx.navigation.NavHostController
import com.example.nestore_15.ui.navigation.FloatingBottomNavBar
import com.example.nestore_15.ui.navigation.StudentNavHost
import com.example.nestore_15.ui.navigation.StudentTab
import com.example.nestore_15.viewmodel.HomeUiState
import com.example.nestore_15.viewmodel.ProfileUiState

@Composable
fun MainScreen(
    navController: NavHostController,
    homeUiState: HomeUiState,
    profileUiState: ProfileUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchDone: () -> Unit,
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
                StudentTab.HOME -> StudentNavHost(
                    navController = navController,
                    homeUiState = homeUiState,
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    onSearchDone = onSearchDone,
                    verificationDotColor = verificationDotColor,
                    onNotifications = onNotifications,
                    onProfile = onProfileHeader,
                    onMapFab = onMapFab,
                    currentUserId = currentUserId,
                    onContactProvider = onContactProvider
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
