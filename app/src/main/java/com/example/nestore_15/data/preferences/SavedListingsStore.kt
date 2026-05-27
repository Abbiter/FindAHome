package com.example.nestore_15.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.savedListingsDataStore by preferencesDataStore(name = "saved_listings")

class SavedListingsStore(private val context: Context) {

    private fun keyFor(userId: String) = stringSetPreferencesKey("saved_listing_ids_$userId")

    fun savedIdsFlow(userId: String): Flow<Set<String>> =
        context.savedListingsDataStore.data.map { prefs ->
            if (userId.isBlank()) emptySet() else prefs[keyFor(userId)] ?: emptySet()
        }

    suspend fun toggleSaved(userId: String, listingId: String): Boolean {
        if (userId.isBlank()) return false
        var nowSaved = false
        context.savedListingsDataStore.edit { prefs ->
            val key = keyFor(userId)
            val current = prefs[key]?.toMutableSet() ?: mutableSetOf()
            nowSaved = if (current.contains(listingId)) {
                current.remove(listingId)
                false
            } else {
                current.add(listingId)
                true
            }
            prefs[key] = current
        }
        return nowSaved
    }

    suspend fun isSaved(userId: String, listingId: String): Boolean =
        savedIdsFlow(userId).first().contains(listingId)
}
