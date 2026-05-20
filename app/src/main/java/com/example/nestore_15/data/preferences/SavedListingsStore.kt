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

    private val key = stringSetPreferencesKey("saved_listing_ids")

    val savedIdsFlow: Flow<Set<String>> =
        context.savedListingsDataStore.data.map { prefs -> prefs[key] ?: emptySet() }

    suspend fun toggleSaved(listingId: String): Boolean {
        var nowSaved = false
        context.savedListingsDataStore.edit { prefs ->
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

    suspend fun isSaved(listingId: String): Boolean =
        savedIdsFlow.first().contains(listingId)
}
