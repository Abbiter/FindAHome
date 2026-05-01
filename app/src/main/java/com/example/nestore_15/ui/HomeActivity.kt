package com.example.nestore_15.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.preferences.ListingFilterPreferencesStore
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.notifications.ListingMatchNotifier
import com.example.nestore_15.viewmodel.HomeUiState
import com.example.nestore_15.viewmodel.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.factory() }
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var listingAdapter: ListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContentView(R.layout.home)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        observeVerificationStatus()
        setupSearchFilter()

        recyclerView = findViewById(R.id.propertyRecyclerView)
        loadingProgress = findViewById(R.id.homeLoadingProgress)
        emptyStateText = findViewById(R.id.tvEmptyState)
        setupPropertyRecyclerView(recyclerView)
        listingAdapter = ListingAdapter(::onReserveRequested, ::onInquireRequested)
        recyclerView.adapter = listingAdapter

        viewModel.uiState.observe(this) { state ->
            renderHomeState(state)
        }

        findViewById<FloatingActionButton>(R.id.fabMap).setOnClickListener {
            handleDepositAction()
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewModel.loadListings()
                }
                R.id.nav_chats -> {
                    Toast.makeText(this, "Long-press a listing to chat with owner", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_saved -> {
                    // TODO: Navigate to saved listings when favorites persistence is implemented
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_logout -> {
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
            }
            drawerLayout.closeDrawers()
            true
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        listingMatchNotifier.start(lifecycleScope)
    }

    private fun setupPropertyRecyclerView(recyclerView: RecyclerView) {
        val columnCount = 2
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_list_spacing)

        recyclerView.layoutManager = GridLayoutManager(this, columnCount)
        if (recyclerView.itemDecorationCount == 0) {
            recyclerView.addItemDecoration(
                GridSpacingItemDecoration(columnCount, spacingPx, includeEdge = true)
            )
        }
    }

    private fun renderHomeState(state: HomeUiState) {
        when (state) {
            HomeUiState.Loading -> {
                recyclerView.alpha = 0.5f
                recyclerView.visibility = View.VISIBLE
                loadingProgress.visibility = View.VISIBLE
                emptyStateText.visibility = View.GONE
            }
            is HomeUiState.Success -> {
                recyclerView.alpha = 1f
                loadingProgress.visibility = View.GONE
                listingAdapter.submitList(state.listings)
                emptyStateText.visibility =
                    if (state.listings.isEmpty()) View.VISIBLE else View.GONE
            }
            HomeUiState.Error -> {
                recyclerView.alpha = 1f
                loadingProgress.visibility = View.GONE
                emptyStateText.visibility = View.GONE
                Toast.makeText(this, "Failed to load listings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleDepositAction() {
        guardRestrictedFeatureAccess(
            onAccessGranted = {
                Toast.makeText(this, "Deposit feature coming soon", Toast.LENGTH_SHORT).show()
            }
        )
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

    private fun onReserveRequested(listing: com.example.nestore_15.data.model.Listing) {
        if (listing.isReserved) {
            Toast.makeText(this, "Listing is already reserved", Toast.LENGTH_SHORT).show()
            return
        }

        guardRestrictedFeatureAccess(
            onAccessGranted = {
                val currentUserId = sessionManager.getCurrentUserId()
                if (currentUserId == null) {
                    Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
                    return@guardRestrictedFeatureAccess
                }

                viewModel.reserveListing(
                    listing = listing,
                    currentUserId = currentUserId
                ) { result ->
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
        )
    }

    private fun onInquireRequested(listing: com.example.nestore_15.data.model.Listing) {
        guardRestrictedFeatureAccess(
            onAccessGranted = {
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

                        viewModel.submitInquiry(
                            listing = listing,
                            message = message,
                            studentId = studentId,
                            studentName = studentName
                        ) { result ->
                            result.onSuccess {
                                Toast.makeText(this, "Inquiry sent", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(
                                    this,
                                    it.message ?: "Could not send inquiry",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        )
    }

    private fun onChatRequested(listing: com.example.nestore_15.data.model.Listing) {
        lifecycleScope.launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()
            if (!isLoggedIn) {
                Toast.makeText(this@HomeActivity, "Please log in to continue", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val currentUserId = sessionManager.getCurrentUserId()
            if (currentUserId.isNullOrBlank()) {
                Toast.makeText(this@HomeActivity, "Please log in to continue", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (listing.ownerId == currentUserId) {
                Toast.makeText(this@HomeActivity, "You cannot chat with yourself", Toast.LENGTH_SHORT).show()
                return@launch
            }

            startActivity(
                Intent(this@HomeActivity, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_LISTING_ID, listing.id)
                    putExtra(ChatActivity.EXTRA_OWNER_ID, listing.ownerId)
                    putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, currentUserId)
                }
            )
        }
    }

    private fun setupSearchFilter() {
        val searchInput = findViewById<EditText?>(R.id.searchInput) ?: return
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                lifecycleScope.launch {
                    filterStore.saveLocationFilter(searchInput.text.toString())
                    Toast.makeText(this@HomeActivity, "Filter saved", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun observeVerificationStatus() {
        val statusText = findViewById<TextView>(R.id.tvVerificationStatus)
        val statusDot = findViewById<View>(R.id.viewVerificationDot)

        lifecycleScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                val (label, colorRes) = when {
                    user == null -> "Not Verified" to R.color.status_not_verified_red
                    user.isVerified -> "Verified" to R.color.available_green
                    else -> "Pending" to R.color.status_pending_orange
                }
                statusText.text = label
                val color = ContextCompat.getColor(this@HomeActivity, colorRes)
                statusText.setTextColor(color)
                statusDot.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }
}
