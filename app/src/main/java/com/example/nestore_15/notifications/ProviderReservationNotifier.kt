package com.example.nestore_15.notifications

import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * When a provider is logged in, watches their properties and alerts on new reservations
 * (Firestore real-time — works even if the student reserved from another device).
 */
class ProviderReservationNotifier(
    private val propertyRepository: PropertyRepository,
    private val userRepository: UserRepository,
    private val notificationHelper: AppNotificationHelper
) {

    private var ownerId: String? = null
    private val knownReserved = mutableMapOf<String, String>()

    fun start(scope: CoroutineScope, providerId: String) {
        if (providerId.isBlank()) return
        ownerId = providerId
        scope.launch {
            propertyRepository.observePropertiesByOwner(providerId).collectLatest { properties ->
                properties.forEach { property ->
                    if (property.availabilityStatus != PropertyStatus.RENTED) {
                        knownReserved.remove(property.id)
                        return@forEach
                    }
                    val ref = property.reservationRef.ifBlank { property.reservedBy }
                    if (ref.isBlank()) return@forEach
                    val key = property.id
                    if (knownReserved[key] == ref) return@forEach

                    val wasTracked = knownReserved.containsKey(key)
                    knownReserved[key] = ref

                    if (!wasTracked) {
                        val studentName = userRepository.getUser(property.reservedBy)?.displayNameOrEmail()
                            ?: "A student"
                        notificationHelper.notifyUser(
                            userId = providerId,
                            title = "Property reserved",
                            message = "$studentName reserved \"${property.title}\" in ${property.location}.",
                            type = NotificationType.RESERVATION_RECEIVED,
                            subtitle = if (property.reservationRef.isNotBlank()) {
                                "Ref: ${property.reservationRef}"
                            } else {
                                "Status: Rented"
                            }
                        )
                    }
                }
            }
        }
    }
}
