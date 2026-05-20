package com.example.nestore_15.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.data.repository.toListing
import com.example.nestore_15.notifications.AppNotificationHelper
import com.example.nestore_15.notifications.ChatMessageNotifier
import com.example.nestore_15.ui.screens.PaymentSummaryUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class PaymentUiState {
    data object Loading : PaymentUiState()
    data class Form(val summary: PaymentSummaryUi, val listing: Listing) : PaymentUiState()
    data class Success(val reservationRef: String, val summary: PaymentSummaryUi) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

class PaymentViewModel(
    private val listingId: String,
    private val propertyRepository: PropertyRepository,
    private val listingRepository: ListingRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val notificationHelper: AppNotificationHelper,
    private val chatMessageNotifier: ChatMessageNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Loading)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private var cachedListing: Listing? = null

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            val result = runCatching {
                val property = propertyRepository.getProperty(listingId)
                val listing = if (property != null) {
                    property.toListing()
                } else {
                    listingRepository.getListingById(listingId)
                        ?: throw IllegalStateException("Listing not found")
                }
                cachedListing = listing
                PaymentSummaryUi(
                    listingId = listing.id,
                    title = listing.title,
                    location = listing.location,
                    priceText = formatPrice(listing.priceBwp),
                    imageUrl = listing.imageUrl
                ) to listing
            }
            _uiState.value = result.fold(
                onSuccess = { (summary, listing) -> PaymentUiState.Form(summary, listing) },
                onFailure = { PaymentUiState.Error(it.message ?: "Could not load listing") }
            )
        }
    }

    fun confirmPayment(currentUserId: String) {
        val listing = cachedListing ?: return
        viewModelScope.launch {
            val current = _uiState.value
            if (current !is PaymentUiState.Form) return@launch
            _uiState.value = PaymentUiState.Loading
            val result = runCatching {
                if (listing.isReserved) {
                    throw IllegalStateException("This property is already reserved")
                }
                if (listing.isPropertyListing) {
                    val property = propertyRepository.getProperty(listing.id)
                    if (property?.availabilityStatus == PropertyStatus.RENTED) {
                        throw IllegalStateException("This property is already reserved")
                    }
                }
                val ref = if (listing.isPropertyListing) {
                    propertyRepository.reservePropertyAsRented(listing.id, currentUserId)
                } else {
                    listingRepository.reserveListing(listing.id, currentUserId)
                }

                val property = propertyRepository.getProperty(listing.id)
                val ownerId = listing.ownerId.ifBlank { property?.ownerId.orEmpty() }

                val studentName = userRepository.getUser(currentUserId)?.displayNameOrEmail()
                    ?: "A student"

                notificationHelper.notifyUser(
                    userId = currentUserId,
                    dedupKey = "reservation:student:$currentUserId:${listing.id}:$ref",
                    title = "Reservation confirmed",
                    message = "You reserved ${current.summary.title} in ${current.summary.location}.",
                    type = NotificationType.RESERVATION,
                    subtitle = "Ref: $ref"
                )

                if (ownerId.isNotBlank() && ownerId != currentUserId) {
                    notificationHelper.notifyUser(
                        userId = ownerId,
                        dedupKey = "reservation:provider:$ownerId:${listing.id}:$ref",
                        title = "Property reserved",
                        message = "$studentName reserved ${current.summary.title}. It is now marked as rented.",
                        type = NotificationType.RESERVATION_RECEIVED,
                        subtitle = "Ref: $ref"
                    )
                }

                if (ownerId.isNotBlank()) {
                    runCatching {
                        val conversationId = chatRepository.getOrCreateConversation(
                            propertyId = listing.id,
                            propertyTitle = current.summary.title,
                            propertyImageUrl = current.summary.imageUrl,
                            studentId = currentUserId,
                            providerId = ownerId
                        )
                        val systemText =
                            "Reservation confirmed (ref $ref). This listing is now marked as rented for both parties."
                        chatRepository.sendSystemMessage(conversationId, systemText)
                        chatMessageNotifier.notifyRecipients(
                            conversationId = conversationId,
                            senderId = ChatRepository.SYSTEM_SENDER_ID,
                            messagePreview = systemText
                        )
                    }
                }

                ref
            }
            _uiState.value = result.fold(
                onSuccess = { ref ->
                    PaymentUiState.Success(ref, current.summary)
                },
                onFailure = { e ->
                    val msg = when {
                        e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                            "Permission denied. Publish the latest firestore.rules in Firebase Console."
                        else -> e.message ?: "Payment could not be completed"
                    }
                    PaymentUiState.Error(msg)
                }
            )
        }
    }

    private fun formatPrice(price: Double): String {
        val formatted = if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
        return "P$formatted / month"
    }

    companion object {
        fun factory(listingId: String, appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val helper = AppNotificationHelper(appContext)
                    return PaymentViewModel(
                        listingId = listingId,
                        propertyRepository = PropertyRepository(),
                        listingRepository = ListingRepository(),
                        chatRepository = ChatRepository(),
                        userRepository = UserRepository(),
                        notificationHelper = helper,
                        chatMessageNotifier = ChatMessageNotifier(
                            ChatRepository(),
                            UserRepository(),
                            helper
                        )
                    ) as T
                }
            }
    }
}
