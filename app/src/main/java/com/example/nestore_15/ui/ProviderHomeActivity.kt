package com.example.nestore_15.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.ProviderDashboardUiState
import com.example.nestore_15.viewmodel.ProviderDashboardViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProviderHomeActivity : AppCompatActivity() {

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
        val recyclerView = findViewById<RecyclerView>(R.id.rvProviderListingsPreview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = listingsAdapter

        bindStatLabel(R.id.statTotalListings, "Total Listings")
        bindStatLabel(R.id.statActiveListings, "Active Listings")
        bindStatLabel(R.id.statInquiries, "Bookings / Inquiries")

        configureActionCard(
            cardId = R.id.actionAddProperty,
            iconRes = android.R.drawable.ic_input_add,
            title = "Add New Property",
            subtitle = "Publish a new listing"
        )
        configureActionCard(
            cardId = R.id.actionManageListings,
            iconRes = android.R.drawable.ic_menu_edit,
            title = "Manage Listings",
            subtitle = "Update availability and pricing"
        )
        configureActionCard(
            cardId = R.id.actionViewInquiries,
            iconRes = android.R.drawable.ic_dialog_email,
            title = "View Inquiries",
            subtitle = "Review bookings and requests"
        )

        findViewById<BottomNavigationView>(R.id.providerBottomNav).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.provider_nav_dashboard -> true
                R.id.provider_nav_listings -> {
                    startActivity(Intent(this, ProviderManageListingsActivity::class.java))
                    true
                }
                R.id.provider_nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        observeVerificationStatus()

        findViewById<ImageView>(R.id.btnProviderProfileIcon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
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

        findViewById<View>(R.id.btnProviderLogout).setOnClickListener {
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

        bindStatCard(
            cardId = R.id.statTotalListings,
            value = state.stats.totalListings.toString()
        )
        bindStatCard(
            cardId = R.id.statActiveListings,
            value = state.stats.activeListings.toString()
        )
        bindStatCard(
            cardId = R.id.statInquiries,
            value = state.stats.inquiriesCount.toString()
        )

        val hasListings = state.listingsPreview.isNotEmpty()
        findViewById<RecyclerView>(R.id.rvProviderListingsPreview).visibility =
            if (hasListings) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.providerEmptyState).visibility =
            if (hasListings) View.GONE else View.VISIBLE

        listingsAdapter.submitList(state.listingsPreview)
    }

    private fun bindStatCard(cardId: Int, value: String) {
        val card = findViewById<View>(cardId)
        card.findViewById<TextView>(R.id.tvStatValue).text = value
    }

    private fun bindStatLabel(cardId: Int, label: String) {
        val card = findViewById<View>(cardId)
        card.findViewById<TextView>(R.id.tvStatLabel).text = label
    }

    private fun configureActionCard(cardId: Int, iconRes: Int, title: String, subtitle: String) {
        val card = findViewById<View>(cardId)
        card.findViewById<ImageView>(R.id.ivActionIcon).setImageResource(iconRes)
        card.findViewById<TextView>(R.id.tvActionTitle).text = title
        card.findViewById<TextView>(R.id.tvActionSubtitle).text = subtitle
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
        val statusText = findViewById<TextView>(R.id.tvProviderVerificationStatus)
        val statusDot = findViewById<View>(R.id.viewProviderVerificationDot)

        lifecycleScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                val (label, colorRes) = when {
                    user == null -> "Not Verified" to R.color.status_not_verified_red
                    user.isVerified -> "Verified" to R.color.available_green
                    else -> "Pending" to R.color.status_pending_orange
                }
                statusText.text = label
                val color = ContextCompat.getColor(this@ProviderHomeActivity, colorRes)
                statusText.setTextColor(color)
                statusDot.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }
}
