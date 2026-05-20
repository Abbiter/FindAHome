package com.example.nestore_15.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.data.preferences.ListingFilterPreferencesStore
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListingMatchNotifier(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val listingRepository: ListingRepository,
    private val filterStore: ListingFilterPreferencesStore,
    private val notificationStore: AppNotificationStore
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var hasInitialSnapshot = false
    private val knownListingIds = mutableSetOf<String>()

    fun start(scope: CoroutineScope) {
        scope.launch {
            combine(
                sessionManager.getCurrentUser(),
                filterStore.preferencesFlow,
                listingRepository.getBrowsableListings()
            ) { user, filters, listings ->
                if (user == null) emptyList()
                else listingRepository.applyBrowseFilters(
                    listings = listings,
                    minPriceBwp = filters.minPriceBwp,
                    maxPriceBwp = filters.maxPriceBwp,
                    locationQuery = filters.location
                )
            }
                .distinctUntilChanged { a, b -> a.map { it.id } == b.map { it.id } }
                .collectLatest { listings ->
                    processListings(scope, listings)
                }
        }
    }

    private suspend fun processListings(scope: CoroutineScope, listings: List<Listing>) {
        val currentIds = listings.map { it.id }.toSet()
        if (!hasInitialSnapshot) {
            knownListingIds.clear()
            knownListingIds.addAll(currentIds)
            hasInitialSnapshot = true
            return
        }

        val newListings = listings.filter { !knownListingIds.contains(it.id) }
        knownListingIds.clear()
        knownListingIds.addAll(currentIds)

        val userId = sessionManager.getCurrentUserId().orEmpty()
        if (userId.isBlank()) return

        newListings.forEach { listing ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    notificationStore.add(
                        userId = userId,
                        title = "New matching listing",
                        message = "${listing.title} in ${listing.location}",
                        type = NotificationType.LISTING_MATCH,
                        subtitle = "Matches your saved filters"
                    )
                }
            }
            showNotification(listing)
        }
    }

    private fun showNotification(listing: Listing) {
        if (!canPostNotifications()) return
        val title = "New matching listing"
        val body = "${listing.title} in ${listing.location}"

        val notification = NotificationCompat.Builder(context, LISTING_MATCH_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(listing.id.hashCode(), notification)
    }

    private fun canPostNotifications(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    companion object {
        const val LISTING_MATCH_CHANNEL_ID = "listing_match_channel"
    }
}
