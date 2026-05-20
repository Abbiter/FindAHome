package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.PropertyDetailScreen
import com.example.nestore_15.ui.screens.PropertyDetailUi
import com.example.nestore_15.ui.theme.FindAHomeTheme
import kotlinx.coroutines.launch

class StudentListingDetailActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val chatRepository = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        val detail = PropertyDetailUi(
            title = propertyTitle,
            price = price,
            location = location,
            availability = availability,
            imageUrl = propertyImageUrl
        )

        setContent {
            FindAHomeTheme {
                PropertyDetailScreen(
                    detail = detail,
                    onBack = { finish() },
                    onContactProvider = {
                        contactProvider(propertyId, propertyTitle, propertyImageUrl, providerId, location)
                    },
                    onReserve = {
                        Toast.makeText(this, "Use Reserve on the listing card to complete booking", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun contactProvider(
        propertyId: String,
        propertyTitle: String,
        propertyImageUrl: String,
        providerId: String,
        location: String
    ) {
        val studentId = sessionManager.getCurrentUserId()
        if (studentId.isNullOrBlank()) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            return
        }
        if (studentId == providerId) {
            Toast.makeText(this, "You cannot chat about your own listing", Toast.LENGTH_SHORT).show()
            return
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
                    }
                )
            }.onFailure {
                Toast.makeText(this@StudentListingDetailActivity, it.message ?: "Could not start chat", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_PROPERTY_ID = "extra_property_id"
        const val EXTRA_PROVIDER_ID = "extra_provider_id"
        const val EXTRA_PROPERTY_TITLE = "extra_property_title"
        const val EXTRA_PROPERTY_IMAGE_URL = "extra_property_image_url"
        const val EXTRA_PROPERTY_PRICE = "extra_property_price"
        const val EXTRA_PROPERTY_LOCATION = "extra_property_location"
        const val EXTRA_PROPERTY_AVAILABILITY = "extra_property_availability"
    }
}
