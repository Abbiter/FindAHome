package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.ListingDetailsScreen
import com.example.nestore_15.ui.screens.ListingDetailsUi
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.ListingDetailViewModel
import kotlinx.coroutines.launch

class StudentListingDetailActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val chatRepository = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val listingId = intent.getStringExtra(EXTRA_PROPERTY_ID).orEmpty()
        if (listingId.isBlank()) {
            Toast.makeText(this, "Property details missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val viewModel: ListingDetailViewModel by viewModels {
            ListingDetailViewModel.factory(listingId)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            FindAHomeTheme {
                ListingDetailsScreen(
                    uiState = uiState,
                    onBack = { finish() },
                    onReserveProperty = {
                        startActivity(
                            Intent(this@StudentListingDetailActivity, PaymentActivity::class.java).apply {
                                putExtra(PaymentActivity.EXTRA_LISTING_ID, listingId)
                            }
                        )
                    },
                    onContactProvider = { detail -> contactProvider(detail) }
                )
            }
        }
    }

    private fun contactProvider(detail: ListingDetailsUi) {
        val studentId = sessionManager.getCurrentUserId()
        if (studentId.isNullOrBlank()) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            return
        }
        if (studentId == detail.ownerId) {
            Toast.makeText(this, "You cannot chat about your own listing", Toast.LENGTH_SHORT).show()
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
                    Intent(this@StudentListingDetailActivity, ChatActivity::class.java).apply {
                        putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversationId)
                        putExtra(ChatActivity.EXTRA_PROPERTY_ID, detail.id)
                        putExtra(ChatActivity.EXTRA_PROPERTY_TITLE, detail.title)
                        putExtra(ChatActivity.EXTRA_PROPERTY_IMAGE_URL, detail.imageUrls.firstOrNull().orEmpty())
                        putExtra(ChatActivity.EXTRA_PROPERTY_LOCATION, detail.location)
                        putExtra(ChatActivity.EXTRA_OWNER_ID, detail.ownerId)
                        putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, studentId)
                        putExtra(ChatActivity.EXTRA_RETURN_TO_PROPERTY, true)
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
