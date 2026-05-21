package com.example.nestore_15.notifications

import android.content.Context
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.ListingFilterPreferences
import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.data.preferences.ListingFilterPreferencesStore
import com.example.nestore_15.data.preferences.ListingSeenStore
import com.example.nestore_15.data.repository.ListingRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Notifies students only when:
 * - They have set at least one browse filter (price or location), and
 * - A listing appears that they have not been alerted about before (persisted per user).
 *
 * Without active filters, every seeded property would match and flood notifications.
 */
class ListingMatchNotifier(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val listingRepository: ListingRepository,
    private val filterStore: ListingFilterPreferencesStore,
    private val notificationStore: AppNotificationStore,
    private val listingSeenStore: ListingSeenStore
) {

    private val notificationHelper = AppNotificationHelper(context)

    fun start(scope: CoroutineScope) {
        scope.launch {
            runCatching {
                combine(
                    sessionManager.getCurrentUser(),
                    filterStore.preferencesFlow,
                    listingRepository.getBrowsableListings()
                ) { user, filters, listings ->
                    Triple(user, filters, listings)
                }
                    .distinctUntilChanged { a, b ->
                        a.first?.id == b.first?.id &&
                            a.second == b.second &&
                            a.third.map { it.id } == b.third.map { it.id }
                    }
                    .collectLatest { (user, filters, listings) ->
                        handleListingsUpdate(user, filters, listings)
                    }
            }
        }
    }

    private suspend fun handleListingsUpdate(
        user: User?,
        filters: ListingFilterPreferences,
        listings: List<Listing>
    ) {
        runCatching {
            val userId = user?.id.orEmpty()
            if (userId.isBlank()) return

            val ids = listings.map { it.id }.toSet()

            if (!listingSeenStore.isSeeded(userId)) {
                listingSeenStore.markSeededWithIds(userId, ids)
                return
            }

            if (!filters.hasActiveFilters()) return

            val seen = listingSeenStore.getSeenIds(userId)
            val newListings = listings.filter { it.id !in seen }
            if (newListings.isEmpty()) return

            listingSeenStore.addSeenIds(userId, newListings.map { it.id })

            newListings.forEach { listing ->
                notificationHelper.notifyUser(
                    userId = userId,
                    dedupKey = "listing:${listing.id}",
                    title = "New matching listing",
                    message = "${listing.title} in ${listing.location}",
                    type = NotificationType.LISTING_MATCH,
                    subtitle = "Matches your saved filters"
                )
            }
        }
    }
}
