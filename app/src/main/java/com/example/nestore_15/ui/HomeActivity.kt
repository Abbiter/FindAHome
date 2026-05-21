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
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.ListingFilterPreferences
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.data.preferences.ListingFilterPreferencesStore
import com.example.nestore_15.data.preferences.ListingSeenStore
import com.example.nestore_15.data.preferences.SavedListingsStore
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.notifications.ListingMatchNotifier
import com.example.nestore_15.ui.components.FullScreenLoading
import com.example.nestore_15.ui.screens.ListingDetailsUi
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
    ) { }

    private val filterStore by lazy { ListingFilterPreferencesStore(applicationContext) }
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.factory(filterStore = filterStore)
    }
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(sessionManager)
    }
    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val chatRepository by lazy { ChatRepository() }
    private val listingRepository by lazy { ListingRepository() }
    private val savedListingsStore by lazy { SavedListingsStore(applicationContext) }
    private val notificationStore by lazy { AppNotificationStore(applicationContext) }
    private val listingSeenStore by lazy { ListingSeenStore(applicationContext) }
    private val listingMatchNotifier by lazy {
        ListingMatchNotifier(
            context = applicationContext,
            sessionManager = sessionManager,
            listingRepository = listingRepository,
            filterStore = filterStore,
            notificationStore = notificationStore,
            listingSeenStore = listingSeenStore
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val showStudentUi = mutableStateOf(false)

        setContent {
            if (!showStudentUi.value) {
                FindAHomeTheme {
                    FullScreenLoading("Loading…")
                }
            } else {
                StudentHomeContent()
            }
        }

        lifecycleScope.launch {
            val uid = sessionManager.getCurrentUserId()
            if (uid.isNullOrBlank()) {
                goToLogin()
                return@launch
            }

            val roleFromIntent = intent.getStringExtra(RegisterActivity.EXTRA_ROLE_OVERRIDE)
                ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
            val user = sessionManager.awaitCurrentUser()
            val role = user?.role ?: roleFromIntent

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
                UserRole.STUDENT, null -> {
                    showStudentUi.value = true
                    onStudentUiReady(uid)
                }
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
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }

    private fun onStudentUiReady(userId: String) {
        lifecycleScope.launch {
            runCatching {
                if (notificationStore.cleanupListingNotificationFlood(userId)) {
                    val browsable = listingRepository.getBrowsableListings().first()
                    listingSeenStore.markSeededWithIds(userId, browsable.map { it.id })
                }
            }
        }
        listingMatchNotifier.start(lifecycleScope)
    }

    @Composable
    private fun StudentHomeContent() {
        val navController = rememberNavController()
        val homeState by viewModel.uiState.observeAsState(HomeUiState.Loading)
        val profileState by profileViewModel.uiState.observeAsState(ProfileUiState.Loading)
        val filterPrefs by filterStore.preferencesFlow.collectAsStateWithLifecycle(
            initialValue = ListingFilterPreferences()
        )
        val currentUser by sessionManager.getCurrentUser().collectAsStateWithLifecycle(initialValue = null)
        var searchQuery by remember { mutableStateOf(filterPrefs.location.orEmpty()) }

        val verificationColor = when (currentUser?.effectiveVerificationStatus()) {
            VerificationStatus.VERIFIED -> FindAHomeColors.VerifiedGreen
            VerificationStatus.PENDING_REVIEW -> FindAHomeColors.PendingOrange
            VerificationStatus.NOT_SUBMITTED, null -> FindAHomeColors.NeutralDot
        }

        FindAHomeTheme {
            MainScreen(
                navController = navController,
                homeUiState = homeState ?: HomeUiState.Loading,
                profileUiState = profileState ?: ProfileUiState.Loading,
                searchQuery = searchQuery,
                onSearchChange = { query ->
                    searchQuery = query
                    viewModel.setSearchQuery(query)
                },
                filterPreferences = filterPrefs,
                onApplyFilters = { min, max, location ->
                    lifecycleScope.launch {
                        filterStore.saveFilters(min, max, location)
                        searchQuery = location.orEmpty()
                        viewModel.setSearchQuery(location.orEmpty())
                    }
                },
                verificationDotColor = verificationColor,
                onNotifications = { openNotifications() },
                onProfileHeader = {
                    startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                },
                onMapFab = {
                    Toast.makeText(this@HomeActivity, "Map view coming soon", Toast.LENGTH_SHORT).show()
                },
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
                onChangePassword = { sendPasswordReset(profileState) },
                currentUserId = sessionManager.getCurrentUserId(),
                onContactProvider = ::contactProvider,
                onToggleFavorite = ::toggleFavorite,
                savedListingsStore = savedListingsStore,
                notificationStore = notificationStore
            )
        }
    }

    private fun openNotifications() {
        maybeRequestNotificationPermission()
        startActivity(Intent(this, NotificationsActivity::class.java))
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun toggleFavorite(listing: Listing) {
        lifecycleScope.launch {
            val saved = savedListingsStore.toggleSaved(listing.id)
            Toast.makeText(
                this@HomeActivity,
                if (saved) "Saved to My Homes" else "Removed from saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun contactProvider(detail: ListingDetailsUi) {
        val studentId = sessionManager.getCurrentUserId()
        if (studentId.isNullOrBlank()) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            runCatching {
                chatRepository.getOrCreateConversation(
                    propertyId = detail.id,
                    propertyTitle = detail.title,
                    propertyImageUrl = detail.imageUrls.firstOrNull().orEmpty(),
                    studentId = studentId,
                    providerId = detail.ownerId
                )
            }.onSuccess { conversationId ->
                startActivity(
                    Intent(this@HomeActivity, ChatActivity::class.java).apply {
                        putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversationId)
                        putExtra(ChatActivity.EXTRA_PROPERTY_ID, detail.id)
                        putExtra(ChatActivity.EXTRA_PROPERTY_TITLE, detail.title)
                        putExtra(ChatActivity.EXTRA_PROPERTY_IMAGE_URL, detail.imageUrls.firstOrNull().orEmpty())
                        putExtra(ChatActivity.EXTRA_PROPERTY_LOCATION, detail.location)
                        putExtra(ChatActivity.EXTRA_OWNER_ID, detail.ownerId)
                        putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, studentId)
                        putExtra(ChatActivity.EXTRA_RETURN_TO_PROPERTY, false)
                    }
                )
            }.onFailure {
                Toast.makeText(this@HomeActivity, it.message ?: "Could not start chat", Toast.LENGTH_LONG).show()
            }
        }
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
}
