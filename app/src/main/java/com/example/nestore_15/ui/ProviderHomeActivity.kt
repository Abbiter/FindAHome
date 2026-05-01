package com.example.nestore_15.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.ProviderDashboardUiState
import com.example.nestore_15.viewmodel.ProviderDashboardViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProviderHomeActivity : AppCompatActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, "You’ll get alerts when new listings match your filters.", Toast.LENGTH_SHORT).show()
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
    private lateinit var listingsAdapter: ProviderListingPreviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val roleFromIntent = intent.getStringExtra(RegisterActivity.EXTRA_ROLE_OVERRIDE)
                ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
            val role = sessionManager.userRole.first() ?: roleFromIntent
            when (role) {
                UserRole.PROVIDER -> {
                    setContentView(R.layout.provider_home)
                    initializeDashboard()
                }
                UserRole.STUDENT -> {
                    startActivity(
                        Intent(this@ProviderHomeActivity, HomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                    )
                    finish()
                }
                null -> {
                    if (sessionManager.getCurrentUserId() != null) {
                        setContentView(R.layout.provider_home)
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
        listingsAdapter = ProviderListingPreviewAdapter(::openListingDetails)
        val drawerLayout = findViewById<DrawerLayout>(R.id.providerDrawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.providerNavView)
        navigationView.bindPersonalizedDrawerHeader(lifecycleScope, sessionManager)
        val recyclerView = findViewById<RecyclerView>(R.id.rvProviderListingsPreview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = listingsAdapter

        observeVerificationStatus()

        findViewById<ImageView>(R.id.btnProviderMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        findViewById<ImageView>(R.id.btnProviderProfileIcon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        setupProviderNotificationEntry()
        findViewById<View>(R.id.actionManageListings).setOnClickListener {
            startActivity(Intent(this, ProviderManageListingsActivity::class.java))
        }
        findViewById<View>(R.id.actionAddProperty).setOnClickListener {
            startActivity(Intent(this, ProviderAddPropertyActivity::class.java))
        }
        findViewById<View>(R.id.actionViewInquiries).setOnClickListener {
            startActivity(Intent(this, ProviderInquiriesActivity::class.java))
        }

        findViewById<View>(R.id.btnEmptyStateAddProperty).setOnClickListener {
            startActivity(Intent(this, ProviderAddPropertyActivity::class.java))
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> Unit
                R.id.nav_saved -> startActivity(Intent(this, ProviderManageListingsActivity::class.java))
                R.id.nav_chats -> startActivity(Intent(this, ConversationsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_logout -> {
                    lifecycleScope.launch {
                        sessionManager.clearSession()
                        startActivity(
                            Intent(this@ProviderHomeActivity, LoginActivity::class.java).apply {
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

        val ownerId = sessionManager.getCurrentUserId()
        if (!ownerId.isNullOrBlank()) {
            viewModel.loadDashboard(ownerId)
        }

        viewModel.uiState.observe(this) { state ->
            renderDashboard(state)
        }
    }

    private fun renderDashboard(state: ProviderDashboardUiState) {
        findViewById<ProgressBar>(R.id.providerDashboardLoading).visibility =
            if (state.isLoading) View.VISIBLE else View.GONE

        findViewById<Chip>(R.id.chipTotalListings).text = "Total: ${state.stats.totalListings}"
        findViewById<Chip>(R.id.chipActiveListings).text = "Active: ${state.stats.activeListings}"
        findViewById<Chip>(R.id.chipInquiries).text = "Inquiries: ${state.stats.inquiriesCount}"

        val hasListings = state.listingsPreview.isNotEmpty()
        findViewById<RecyclerView>(R.id.rvProviderListingsPreview).visibility =
            if (hasListings) View.VISIBLE else View.GONE
        findViewById<View>(R.id.providerEmptyState).visibility =
            if (hasListings) View.GONE else View.VISIBLE

        listingsAdapter.submitList(state.listingsPreview)
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
        return if (price % 1.0 == 0.0) {
            price.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", price)
        }
    }

    private fun observeVerificationStatus() {
        val statusDot = findViewById<View>(R.id.viewProviderVerificationDot)
        val a11yBase = getString(R.string.cd_verification_status)

        lifecycleScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                val (label, colorRes) = when {
                    user == null ->
                        "Signed out" to R.color.verification_dot_neutral
                    user.effectiveVerificationStatus() == VerificationStatus.VERIFIED ->
                        getString(R.string.verification_label_verified) to R.color.available_green
                    user.effectiveVerificationStatus() == VerificationStatus.PENDING_REVIEW ->
                        getString(R.string.verification_label_pending) to R.color.status_pending_orange
                    else ->
                        getString(R.string.verification_label_not_submitted) to R.color.verification_dot_neutral
                }
                val color = ContextCompat.getColor(this@ProviderHomeActivity, colorRes)
                statusDot.backgroundTintList = ColorStateList.valueOf(color)
                statusDot.contentDescription = "$a11yBase: $label"
            }
        }
    }

    private fun setupProviderNotificationEntry() {
        findViewById<ImageView>(R.id.btnProviderNotifications).setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33) {
                when {
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> openAppNotificationSettings()
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) -> {
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
