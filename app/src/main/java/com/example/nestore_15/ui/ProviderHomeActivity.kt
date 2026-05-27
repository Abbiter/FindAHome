package com.example.nestore_15.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.ProviderHomeScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.notifications.AppNotificationHelper
import com.example.nestore_15.notifications.ChatInboxNotifier
import com.example.nestore_15.notifications.ProviderReservationNotifier
import com.example.nestore_15.viewmodel.ProviderDashboardUiState
import com.example.nestore_15.viewmodel.ProviderDashboardViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProviderHomeActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, "You'll get alerts for reservations and messages.", Toast.LENGTH_SHORT).show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Notifications off")
                .setMessage("You can enable them anytime in Settings.")
                .setPositiveButton("Open settings") { _, _ -> openAppNotificationSettings() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: ProviderDashboardViewModel by viewModels {
        ProviderDashboardViewModel.factory()
    }
    private val reservationNotifier by lazy {
        ProviderReservationNotifier(
            PropertyRepository(),
            UserRepository(),
            AppNotificationHelper(applicationContext)
        )
    }
    private val chatInboxNotifier by lazy {
        ChatInboxNotifier(
            context = applicationContext,
            sessionManager = sessionManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val roleFromIntent = intent.getStringExtra(RegisterActivity.EXTRA_ROLE_OVERRIDE)
                ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
            val role = sessionManager.userRole.first() ?: roleFromIntent
            when (role) {
                UserRole.PROVIDER -> initializeDashboard()
                UserRole.STUDENT -> {
                    startActivity(Intent(this@ProviderHomeActivity, HomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                    finish()
                }
                null -> {
                    if (sessionManager.getCurrentUserId() != null) {
                        initializeDashboard()
                        return@launch
                    }
                    startActivity(
                        Intent(this@ProviderHomeActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
            }
        }
    }

    private fun initializeDashboard() {
        val ownerId = sessionManager.getCurrentUserId()
        if (!ownerId.isNullOrBlank()) {
            viewModel.loadDashboard(ownerId)
            reservationNotifier.start(lifecycleScope, ownerId)
            chatInboxNotifier.start(lifecycleScope)
        }
        maybeRequestNotificationPermission()

        setContent {
            val state by viewModel.uiState.observeAsState(ProviderDashboardUiState())
            FindAHomeTheme {
                ProviderHomeScreen(
                    uiState = state ?: ProviderDashboardUiState(),
                    onAddProperty = {
                        startActivity(Intent(this, ProviderAddPropertyActivity::class.java))
                    },
                    onManageListings = {
                        startActivity(Intent(this, ProviderManageListingsActivity::class.java))
                    },
                    onMessages = {
                        startActivity(Intent(this, ConversationsActivity::class.java))
                    },
                    onNotifications = {
                        maybeRequestNotificationPermission()
                        startActivity(Intent(this, NotificationsActivity::class.java))
                    },
                    onProfile = {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    },
                    onListingClick = ::openListingDetails
                )
            }
        }
    }

    private fun openListingDetails(listing: Listing) {
        startActivity(
            Intent(this, ProviderListingDetailActivity::class.java).apply {
                putExtra(ProviderListingDetailActivity.EXTRA_TITLE, listing.title)
                putExtra(ProviderListingDetailActivity.EXTRA_LOCATION, listing.location)
                putExtra(
                    ProviderListingDetailActivity.EXTRA_PRICE,
                    getString(R.string.listing_price_monthly, formatPrice(listing.priceBwp))
                )
                putExtra(ProviderListingDetailActivity.EXTRA_STATUS, resolveListingStatusLabel(listing))
                putExtra(ProviderListingDetailActivity.EXTRA_IMAGE_URL, listing.imageUrl)
                putExtra(ProviderListingDetailActivity.EXTRA_PROPERTY_ID, listing.id)
                putExtra(ProviderListingDetailActivity.EXTRA_IS_PROPERTY_LISTING, listing.isPropertyListing)
            }
        )
    }

    private fun resolveListingStatusLabel(listing: Listing): String {
        if (listing.isReserved) return "Rented"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val pending = listing.availabilityDate.isNotBlank() && listing.availabilityDate > today
        return if (pending) "Pending" else "Available"
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openAppNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        }
        runCatching { startActivity(intent) }
    }
}
