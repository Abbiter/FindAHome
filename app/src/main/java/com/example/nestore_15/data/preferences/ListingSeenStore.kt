package com.example.nestore_15.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray

private val Context.listingSeenDataStore by preferencesDataStore(name = "listing_seen_v1")

/**
 * Persists which listing IDs a user has already been notified about,
 * so a Firestore re-sync does not create dozens of "new listing" alerts.
 */
class ListingSeenStore(private val context: Context) {

    private fun idsKey(userId: String) = stringPreferencesKey("seen_ids_$userId")
    private fun seededKey(userId: String) = booleanPreferencesKey("seeded_$userId")

    suspend fun getSeenIds(userId: String): Set<String> {
        if (userId.isBlank()) return emptySet()
        val prefs = context.listingSeenDataStore.data.first()
        return decodeIds(prefs[idsKey(userId)].orEmpty())
    }

    suspend fun isSeeded(userId: String): Boolean {
        if (userId.isBlank()) return false
        val prefs = context.listingSeenDataStore.data.first()
        return prefs[seededKey(userId)] == true
    }

    suspend fun markSeededWithIds(userId: String, listingIds: Collection<String>) {
        if (userId.isBlank()) return
        context.listingSeenDataStore.edit { prefs ->
            prefs[idsKey(userId)] = encodeIds(listingIds.toSet())
            prefs[seededKey(userId)] = true
        }
    }

    suspend fun addSeenIds(userId: String, listingIds: Collection<String>) {
        if (userId.isBlank() || listingIds.isEmpty()) return
        context.listingSeenDataStore.edit { prefs ->
            val merged = decodeIds(prefs[idsKey(userId)].orEmpty()) + listingIds
            prefs[idsKey(userId)] = encodeIds(merged)
            prefs[seededKey(userId)] = true
        }
    }

    private fun encodeIds(ids: Set<String>): String {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun decodeIds(raw: String): Set<String> {
        if (raw.isBlank()) return emptySet()
        return runCatching {
            val arr = JSONArray(raw)
            buildSet {
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i)
                    if (id.isNotBlank()) add(id)
                }
            }
        }.getOrDefault(emptySet())
    }
}
