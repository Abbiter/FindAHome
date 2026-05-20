package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.R
import com.example.nestore_15.data.util.ListingImageResolver
import com.example.nestore_15.data.util.loadListingImage
import com.example.nestore_15.data.model.Property
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.session.ProviderSessionResult
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.data.session.resolveProviderSession
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import java.util.Locale

class ProviderListingDetailActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val propertyRepository = PropertyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            when (val gate = sessionManager.resolveProviderSession()) {
                is ProviderSessionResult.Active -> {
                    setContentView(R.layout.provider_listing_detail)
                    setupBackNavigation()
                    bindScreen(gate.userId)
                }
                ProviderSessionResult.RedirectStudent -> {
                    startActivity(Intent(this@ProviderListingDetailActivity, HomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                    finish()
                }
                ProviderSessionResult.RedirectLogin -> {
                    startActivity(
                        Intent(this@ProviderListingDetailActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
            }
        }
    }

    private fun setupBackNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun bindScreen(ownerId: String) {
        val propertyId = intent.getStringExtra(EXTRA_PROPERTY_ID)
        val isProperty = intent.getBooleanExtra(EXTRA_IS_PROPERTY_LISTING, false)
        val pb = findViewById<ProgressBar>(R.id.pbDetailLoading)
        val scroll = findViewById<View>(R.id.svPropertyDetail)
        val actions = findViewById<View>(R.id.rowStatusActions)
        val editBtn = findViewById<MaterialButton>(R.id.btnDetailEdit)
        val delBtn = findViewById<MaterialButton>(R.id.btnDetailDelete)

        if (!propertyId.isNullOrBlank() && isProperty) {
            pb.visibility = View.VISIBLE
            scroll.alpha = 0.4f
            lifecycleScope.launch {
                val p = propertyRepository.getProperty(propertyId)
                pb.visibility = View.GONE
                scroll.alpha = 1f
                if (p == null || p.ownerId != ownerId) {
                    Toast.makeText(this@ProviderListingDetailActivity, "Listing not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
                bindProperty(p)
                actions.visibility = View.VISIBLE
                editBtn.visibility = View.VISIBLE
                delBtn.visibility = View.VISIBLE
                wireActions(ownerId, p)
            }
        } else {
            pb.visibility = View.GONE
            bindFromIntent()
            actions.visibility = View.GONE
            editBtn.visibility = View.GONE
            delBtn.visibility = View.GONE
        }
    }

    private fun bindFromIntent() {
        val image = findViewById<ShapeableImageView>(R.id.ivDetailImage)
        findViewById<TextView>(R.id.tvDetailTitle).text = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        findViewById<TextView>(R.id.tvDetailLocation).text = intent.getStringExtra(EXTRA_LOCATION).orEmpty()
        findViewById<TextView>(R.id.tvDetailPrice).text = intent.getStringExtra(EXTRA_PRICE).orEmpty()
        findViewById<TextView>(R.id.tvDetailStatus).text = intent.getStringExtra(EXTRA_STATUS).orEmpty()
        findViewById<TextView>(R.id.tvDetailRooms).visibility = View.GONE
        findViewById<TextView>(R.id.tvDetailDescription).visibility = View.GONE
        image.loadListingImage(intent.getStringExtra(EXTRA_IMAGE_URL))
    }

    private fun bindProperty(p: Property) {
        val image = findViewById<ShapeableImageView>(R.id.ivDetailImage)
        findViewById<TextView>(R.id.tvDetailTitle).text = p.title
        findViewById<TextView>(R.id.tvDetailLocation).text = p.location
        findViewById<TextView>(R.id.tvDetailPrice).text = getString(
            R.string.listing_price_monthly,
            formatPrice(p.priceBwp)
        )
        findViewById<TextView>(R.id.tvDetailStatus).text = statusLabel(p.availabilityStatus)
        findViewById<TextView>(R.id.tvDetailRooms).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvDetailRooms).text = "${p.roomCount} rooms"
        findViewById<TextView>(R.id.tvDetailDescription).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvDetailDescription).text = p.description.ifBlank { "No description" }
        image.loadListingImage(ListingImageResolver.primaryFromList(p.imageUrls))
    }

    private fun wireActions(ownerId: String, p: Property) {
        findViewById<MaterialButton>(R.id.btnStatusAvailable).setOnClickListener {
            updateStatus(p.id, PropertyStatus.AVAILABLE)
        }
        findViewById<MaterialButton>(R.id.btnStatusPending).setOnClickListener {
            updateStatus(p.id, PropertyStatus.PENDING)
        }
        findViewById<MaterialButton>(R.id.btnStatusRented).setOnClickListener {
            updateStatus(p.id, PropertyStatus.RENTED)
        }
        findViewById<MaterialButton>(R.id.btnDetailEdit).setOnClickListener {
            startActivity(
                Intent(this, ProviderEditPropertyActivity::class.java).apply {
                    putExtra(ProviderEditPropertyActivity.EXTRA_PROPERTY_ID, p.id)
                }
            )
        }
        findViewById<MaterialButton>(R.id.btnDetailDelete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete property")
                .setMessage("Remove \"${p.title}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        runCatching { propertyRepository.deleteProperty(p.id) }
                            .onSuccess {
                                Toast.makeText(this@ProviderListingDetailActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .onFailure {
                                Toast.makeText(
                                    this@ProviderListingDetailActivity,
                                    it.message ?: "Delete failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun updateStatus(propertyId: String, status: PropertyStatus) {
        lifecycleScope.launch {
            runCatching { propertyRepository.updateAvailabilityStatus(propertyId, status) }
                .onSuccess {
                    findViewById<TextView>(R.id.tvDetailStatus).text = statusLabel(status)
                    Toast.makeText(this@ProviderListingDetailActivity, "Status updated", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(
                        this@ProviderListingDetailActivity,
                        it.message ?: "Update failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun statusLabel(s: PropertyStatus): String = when (s) {
        PropertyStatus.AVAILABLE -> "Available"
        PropertyStatus.PENDING -> "Pending"
        PropertyStatus.RENTED -> "Rented"
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_LOCATION = "extra_location"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_PROPERTY_ID = "extra_property_id"
        const val EXTRA_IS_PROPERTY_LISTING = "extra_is_property_listing"
    }
}
