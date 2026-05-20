package com.example.nestore_15.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.ListingFilterPreferences
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.ui.screens.HomeScreen
import com.example.nestore_15.ui.screens.ListingDetailsScreen
import com.example.nestore_15.ui.screens.ListingDetailsUi
import com.example.nestore_15.ui.screens.PaymentScreen
import com.example.nestore_15.viewmodel.HomeUiState
import com.example.nestore_15.viewmodel.ListingDetailViewModel
import com.example.nestore_15.viewmodel.PaymentViewModel

@Composable
fun StudentNavHost(
    navController: NavHostController,
    homeUiState: HomeUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterPreferences: ListingFilterPreferences,
    onApplyFilters: (minPrice: Double?, maxPrice: Double?, location: String?) -> Unit,
    verificationDotColor: Color,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    onMapFab: () -> Unit,
    currentUserId: String?,
    onContactProvider: (ListingDetailsUi) -> Unit,
    onToggleFavorite: (Listing) -> Unit,
    savedListingIds: Set<String>,
    notificationStore: AppNotificationStore,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = StudentNavRoutes.HOME,
        modifier = modifier.fillMaxSize()
    ) {
        composable(StudentNavRoutes.HOME) {
            HomeScreen(
                uiState = homeUiState,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                filterPreferences = filterPreferences,
                onApplyFilters = onApplyFilters,
                verificationDotColor = verificationDotColor,
                onNotifications = onNotifications,
                onProfile = onProfile,
                onListingClick = { listing ->
                    navController.navigate(StudentNavRoutes.listingDetails(listing.id))
                },
                onToggleFavorite = onToggleFavorite,
                savedListingIds = savedListingIds,
                onMapFab = onMapFab
            )
        }

        composable(
            route = StudentNavRoutes.LISTING_DETAILS,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { entry ->
            val listingId = entry.arguments?.getString("listingId").orEmpty()
            val detailViewModel: ListingDetailViewModel = viewModel(
                factory = ListingDetailViewModel.factory(listingId, currentUserId)
            )
            val detailState by detailViewModel.uiState.collectAsState()

            ListingDetailsScreen(
                uiState = detailState,
                onBack = { navController.popBackStack() },
                onReserveProperty = { detail ->
                    if (detail.isReserved || detail.reservedByCurrentUser) {
                        Toast.makeText(context, "This property is already reserved", Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate(StudentNavRoutes.payment(listingId))
                    }
                },
                onContactProvider = { detail ->
                    if (currentUserId.isNullOrBlank()) {
                        Toast.makeText(context, "Please log in to continue", Toast.LENGTH_SHORT).show()
                    } else if (currentUserId == detail.ownerId) {
                        Toast.makeText(context, "You cannot chat about your own listing", Toast.LENGTH_SHORT).show()
                    } else {
                        onContactProvider(detail)
                    }
                }
            )
        }

        composable(
            route = StudentNavRoutes.PAYMENT,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { entry ->
            val listingId = entry.arguments?.getString("listingId").orEmpty()
            val paymentViewModel: PaymentViewModel = viewModel(
                factory = PaymentViewModel.factory(listingId, notificationStore)
            )
            val paymentState by paymentViewModel.uiState.collectAsState()

            PaymentScreen(
                uiState = paymentState,
                onBack = { navController.popBackStack() },
                onConfirmPayment = {
                    val uid = currentUserId
                    if (uid.isNullOrBlank()) {
                        Toast.makeText(context, "Please log in to continue", Toast.LENGTH_SHORT).show()
                    } else {
                        paymentViewModel.confirmPayment(uid)
                    }
                },
                onBackToHome = {
                    navController.popBackStack(StudentNavRoutes.HOME, inclusive = false)
                }
            )
        }
    }
}
