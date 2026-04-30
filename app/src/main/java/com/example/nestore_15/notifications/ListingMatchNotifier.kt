package com.example.nestore_15.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.preferences.ListingFilterPreferencesStore
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ListingMatchNotifier(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val listingRepository: ListingRepository,
    private val filterStore: ListingFilterPreferencesStore
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var hasInitialSnapshot = false
    private val knownListingIds = mutableSetOf<String>()

    fun start(scope: CoroutineScope) {
        scope.launch {
            combine(
                sessionManager.getCurrentUser(),
                filterStore.preferencesFlow
            ) { user, filters -> user to filters }
                .distinctUntilChanged()
                .flatMapLatest { (user, filters) ->
                    if (user == null) {
                        knownListingIds.clear()
                        hasInitialSnapshot = false
                        flowOf(emptyList())
                    } else {
                        listingRepository.getFilteredListings(
                            minPriceBwp = filters.minPriceBwp,
                            maxPriceBwp = filters.maxPriceBwp,
                            location = filters.location
                        )
                    }
                }
                .collectLatest { listings ->
                    processListings(listings)
                }
        }
    }

    private fun processListings(listings: List<Listing>) {
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

        newListings.forEach { listing ->
            showNotification(listing)
        }
    }

    private fun showNotification(listing: Listing) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, LISTING_MATCH_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("New matching listing")
            .setContentText("${listing.title} in ${listing.location}")
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
