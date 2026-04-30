package com.example.nestore_15.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nestore_15.data.model.ListingFilterPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.filterDataStore by preferencesDataStore(name = "listing_filter_preferences")

class ListingFilterPreferencesStore(private val context: Context) {

    private object Keys {
        val minPrice = doublePreferencesKey("min_price_bwp")
        val maxPrice = doublePreferencesKey("max_price_bwp")
        val location = stringPreferencesKey("filter_location")
    }

    val preferencesFlow: Flow<ListingFilterPreferences> =
        context.filterDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { prefs ->
                ListingFilterPreferences(
                    minPriceBwp = prefs[Keys.minPrice],
                    maxPriceBwp = prefs[Keys.maxPrice],
                    location = prefs[Keys.location]?.takeIf { it.isNotBlank() }
                )
            }

    suspend fun saveLocationFilter(location: String?) {
        context.filterDataStore.edit { prefs ->
            val clean = location?.trim().orEmpty()
            if (clean.isBlank()) {
                prefs.remove(Keys.location)
            } else {
                prefs[Keys.location] = clean
            }
        }
    }
}
