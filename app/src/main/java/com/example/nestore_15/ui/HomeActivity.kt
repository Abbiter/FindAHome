package com.example.nestore_15.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.data.preferences.ListingFilterPreferencesStore
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.notifications.ListingMatchNotifier
import com.example.nestore_15.ui.screens.MainScreen
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.HomeUiState
import com.example.nestore_15.viewmodel.HomeViewModel
import com.example.nestore_15.viewmodel.ProfileUiState
import com.example.nestore_15.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.nestore_15.R

class HomeActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, "You'll get alerts when new listings match your filters.", Toast.LENGTH_SHORT).show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Notifications off")
                .setMessage("You can enable them anytime in Settings.")
                .setPositiveButton("Open settings") { _, _ -> openAppNotificationSettings() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.factory() }
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(sessionManager)
    }
    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val listingRepository by lazy { ListingRepository() }
    private val filterStore by lazy { ListingFilterPreferencesStore(applicationContext) }
    private val listingMatchNotifier by lazy {
        ListingMatchNotifier(
            context = applicationContext,
            sessionManager = sessionManager,
            listingRepository = listingRepository,
            filterStore = filterStore
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
                UserRole.PROVIDER -> {
                    startActivity(
                        Intent(this@HomeActivity, ProviderHomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                    )
                    finish()
                    return@launch
                }
                UserRole.STUDENT -> Unit
                null -> {
                    if (sessionManager.getCurrentUserId() != null) {
                        initializeStudentUi()
                        return@launch
                    }
                    startActivity(
                        Intent(this@HomeActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                    return@launch
                }
            }
            initializeStudentUi()
        }
    }

    private fun initializeStudentUi() {
        setContent {
            val homeState by viewModel.uiState.observeAsState(HomeUiState.Loading)
            val profileState by profileViewModel.uiState.observeAsState(ProfileUiState.Loading)
            val currentUser by sessionManager.getCurrentUser().collectAsStateWithLifecycle(initialValue = null)
            var searchQuery by remember { mutableStateOf("") }

            val verificationColor = when (currentUser?.effectiveVerificationStatus()) {
                VerificationStatus.VERIFIED -> FindAHomeColors.VerifiedGreen
                VerificationStatus.PENDING_REVIEW -> FindAHomeColors.PendingOrange
                VerificationStatus.NOT_SUBMITTED, null -> FindAHomeColors.NeutralDot
            }

            FindAHomeTheme {
                MainScreen(
                    homeUiState = homeState ?: HomeUiState.Loading,
                    profileUiState = profileState ?: ProfileUiState.Loading,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onSearchDone = {
                        lifecycleScope.launch {
                            filterStore.saveLocationFilter(searchQuery)
                            Toast.makeText(this@HomeActivity, "Filter saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    verificationDotColor = verificationColor,
                    onNotifications = { setupNotificationEntry() },
                    onProfileHeader = {
                        startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                    },
                    onListingClick = ::openListingDetail,
                    onReserve = ::onReserveRequested,
                    onInquire = ::onInquireRequested,
                    onMapFab = { handleDepositAction() },
                    onOpenMessages = {
                        startActivity(Intent(this@HomeActivity, ConversationsActivity::class.java))
                    },
                    onEditProfile = {
                        startActivity(Intent(this@HomeActivity, EditProfileActivity::class.java))
                    },
                    onVerify = {
                        startActivity(Intent(this@HomeActivity, VerificationActivity::class.java))
                    },
                    onLogout = { confirmLogout() },
                    onChangePassword = { sendPasswordReset(profileState) }
                )
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishAffinity()
                }
            }
        )

        listingMatchNotifier.start(lifecycleScope)
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Log out")
            .setMessage("Sign out of this account?")
            .setPositiveButton("Log out") { _, _ ->
                lifecycleScope.launch {
                    sessionManager.clearSession()
                    startActivity(
                        Intent(this@HomeActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun sendPasswordReset(profileState: ProfileUiState?) {
        val email = (profileState as? ProfileUiState.Success)?.user?.email?.takeIf { it.isNotBlank() }
            ?: FirebaseAuth.getInstance().currentUser?.email
        if (email.isNullOrBlank()) {
            Toast.makeText(this, "No email on file", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Could not send email", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun handleDepositAction() {
        guardRestrictedFeatureAccess {
            Toast.makeText(this, "Deposit feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardRestrictedFeatureAccess(onAccessGranted: () -> Unit) {
        lifecycleScope.launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()
            if (!isLoggedIn) {
                Toast.makeText(this@HomeActivity, "Please log in to continue", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val isVerified = sessionManager.isVerified.first()
            if (!isVerified) {
                Toast.makeText(this@HomeActivity, "Please verify your account to continue", Toast.LENGTH_SHORT).show()
                return@launch
            }
            onAccessGranted()
        }
    }

    private fun onReserveRequested(listing: Listing) {
        if (listing.isReserved) {
            Toast.makeText(this, "Listing is already reserved", Toast.LENGTH_SHORT).show()
            return
        }
        guardRestrictedFeatureAccess {
            val currentUserId = sessionManager.getCurrentUserId()
            if (currentUserId == null) {
                Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
                return@guardRestrictedFeatureAccess
            }
            viewModel.reserveListing(listing, currentUserId) { result ->
                result.onSuccess { reservationRef ->
                    startActivity(
                        Intent(this, PaymentSuccessActivity::class.java).apply {
                            putExtra(PaymentSuccessActivity.EXTRA_RESERVATION_REF, reservationRef)
                        }
                    )
                }.onFailure {
                    Toast.makeText(this, "Listing already reserved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onInquireRequested(listing: Listing) {
        guardRestrictedFeatureAccess {
            val input = EditText(this)
            input.hint = "Your message to the host"
            MaterialAlertDialogBuilder(this)
                .setTitle("Inquire about this home")
                .setView(input)
                .setPositiveButton("Send") { _, _ ->
                    val message = input.text.toString().trim()
                    if (message.isEmpty()) {
                        Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val studentId = sessionManager.getCurrentUserId()
                    if (studentId.isNullOrBlank()) {
                        Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val authUser = FirebaseAuth.getInstance().currentUser
                    val rawName = authUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: authUser?.email?.substringBefore("@").orEmpty()
                    val studentName = if (rawName.isBlank()) "Student" else rawName
                    viewModel.submitInquiry(listing, message, studentId, studentName) { result ->
                        result.onSuccess {
                            Toast.makeText(this, "Inquiry sent", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(this, it.message ?: "Could not send inquiry", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun openListingDetail(listing: Listing) {
        startActivity(
            Intent(this, StudentListingDetailActivity::class.java).apply {
                putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_ID, listing.id)
                putExtra(StudentListingDetailActivity.EXTRA_PROVIDER_ID, listing.ownerId)
                putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_TITLE, listing.title)
                putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_IMAGE_URL, listing.imageUrl)
                putExtra(
                    StudentListingDetailActivity.EXTRA_PROPERTY_PRICE,
                    getString(R.string.listing_price_monthly, formatPrice(listing.priceBwp))
                )
                putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_LOCATION, listing.location)
                putExtra(
                    StudentListingDetailActivity.EXTRA_PROPERTY_AVAILABILITY,
                    getString(R.string.listing_available_on, listing.availabilityDate)
                )
            }
        )
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(java.util.Locale.getDefault(), "%.2f", price)
    }

    private fun setupNotificationEntry() {
        if (Build.VERSION.SDK_INT >= 33) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED -> openAppNotificationSettings()
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS) -> {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.notification_rationale)
                        .setPositiveButton(R.string.notification_allow) { _, _ ->
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton(R.string.not_now, null)
                        .show()
                }
                else -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            openAppNotificationSettings()
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
