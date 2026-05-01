package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.nestore_15.R
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.session.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class StudentListingDetailActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val chatRepository = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_listing_detail)
        setupBackNavigation()

        val propertyId = intent.getStringExtra(EXTRA_PROPERTY_ID).orEmpty()
        val propertyTitle = intent.getStringExtra(EXTRA_PROPERTY_TITLE).orEmpty()
        val propertyImageUrl = intent.getStringExtra(EXTRA_PROPERTY_IMAGE_URL).orEmpty()
        val providerId = intent.getStringExtra(EXTRA_PROVIDER_ID).orEmpty()
        val location = intent.getStringExtra(EXTRA_PROPERTY_LOCATION).orEmpty()
        val price = intent.getStringExtra(EXTRA_PROPERTY_PRICE).orEmpty()
        val availability = intent.getStringExtra(EXTRA_PROPERTY_AVAILABILITY).orEmpty()

        if (propertyId.isBlank() || propertyTitle.isBlank() || providerId.isBlank()) {
            Toast.makeText(this, "Property details missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.tvStudentListingTitle).text = propertyTitle
        findViewById<TextView>(R.id.tvStudentListingPrice).text = price
        findViewById<TextView>(R.id.tvStudentListingLocation).text = location
        findViewById<TextView>(R.id.tvStudentListingAvailability).text = availability
        Glide.with(this)
            .load(propertyImageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .centerCrop()
            .into(findViewById<ImageView>(R.id.ivStudentListingImage))

        findViewById<MaterialButton>(R.id.btnContactProvider).setOnClickListener {
            val studentId = sessionManager.getCurrentUserId()
            if (studentId.isNullOrBlank()) {
                Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (studentId == providerId) {
                Toast.makeText(this, "You cannot chat about your own listing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                runCatching {
                    chatRepository.getOrCreateConversation(
                        propertyId = propertyId,
                        propertyTitle = propertyTitle,
                        propertyImageUrl = propertyImageUrl,
                        studentId = studentId,
                        providerId = providerId
                    )
                }.onSuccess { conversationId ->
                    startActivity(
                        Intent(this@StudentListingDetailActivity, ChatActivity::class.java).apply {
                            putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversationId)
                            putExtra(ChatActivity.EXTRA_PROPERTY_ID, propertyId)
                            putExtra(ChatActivity.EXTRA_PROPERTY_TITLE, propertyTitle)
                            putExtra(ChatActivity.EXTRA_PROPERTY_IMAGE_URL, propertyImageUrl)
                            putExtra(ChatActivity.EXTRA_PROPERTY_LOCATION, location)
                            putExtra(ChatActivity.EXTRA_PROPERTY_PRICE, price)
                            putExtra(ChatActivity.EXTRA_PROPERTY_AVAILABILITY, availability)
                            putExtra(ChatActivity.EXTRA_OWNER_ID, providerId)
                            putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, studentId)
                            putExtra(ChatActivity.EXTRA_RETURN_TO_PROPERTY, true)
                        }
                    )
                }.onFailure {
                    Toast.makeText(this@StudentListingDetailActivity, "Could not open chat", Toast.LENGTH_SHORT).show()
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

    companion object {
        const val EXTRA_PROPERTY_ID = "extra_property_id"
        const val EXTRA_PROVIDER_ID = "extra_provider_id"
        const val EXTRA_PROPERTY_TITLE = "extra_property_title"
        const val EXTRA_PROPERTY_IMAGE_URL = "extra_property_image_url"
        const val EXTRA_PROPERTY_LOCATION = "extra_property_location"
        const val EXTRA_PROPERTY_PRICE = "extra_property_price"
        const val EXTRA_PROPERTY_AVAILABILITY = "extra_property_availability"
    }
}
