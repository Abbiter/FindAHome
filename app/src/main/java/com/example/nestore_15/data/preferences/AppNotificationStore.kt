package com.example.nestore_15.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nestore_15.data.model.AppNotification
import com.example.nestore_15.data.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.notificationDataStore by preferencesDataStore(name = "app_notifications_v2")

class AppNotificationStore(private val context: Context) {

    private val notificationsKey = stringPreferencesKey("notifications_json")
    private val dedupKeysKey = stringPreferencesKey("notification_dedup_keys")
    private val floodCleanupKey = booleanPreferencesKey("listing_flood_cleaned_v1")

    fun notificationsForUser(userId: String): Flow<List<AppNotification>> =
        notificationsFlow.map { list ->
            if (userId.isBlank()) emptyList()
            else list.filter { it.userId == userId }
        }

    val notificationsFlow: Flow<List<AppNotification>> =
        context.notificationDataStore.data.map { prefs ->
            decode(prefs[notificationsKey].orEmpty())
        }

    suspend fun hasDedupKey(userId: String, dedupKey: String): Boolean {
        if (userId.isBlank() || dedupKey.isBlank()) return false
        val prefs = context.notificationDataStore.data.first()
        val keys = loadDedupKeys(prefs[dedupKeysKey].orEmpty())
        return keys[userId]?.contains(dedupKey) == true
    }

    suspend fun addOnce(
        userId: String,
        dedupKey: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        subtitle: String = ""
    ): Boolean {
        if (userId.isBlank() || dedupKey.isBlank()) return false
        if (hasDedupKey(userId, dedupKey)) return false
        add(userId, title, message, type, subtitle, dedupKey)
        recordDedupKey(userId, dedupKey)
        return true
    }

    /**
     * One-time cleanup for the listing-match flood (seed data + restart without filters).
     * Returns true if cleanup ran this call.
     */
    suspend fun cleanupListingNotificationFlood(userId: String): Boolean {
        if (userId.isBlank()) return false
        val prefs = context.notificationDataStore.data.first()
        if (prefs[floodCleanupKey] == true) return false

        context.notificationDataStore.edit { editPrefs ->
            val current = decode(editPrefs[notificationsKey].orEmpty())
            val removing = current.filter {
                it.userId == userId && it.type == NotificationType.LISTING_MATCH
            }
            val keys = loadDedupKeys(editPrefs[dedupKeysKey].orEmpty()).toMutableMap()
            val userKeys = keys.getOrPut(userId) { mutableSetOf() }.toMutableSet()
            removing.forEach { n ->
                val key = n.dedupKey.ifBlank { dedupKeyFor(n) }
                userKeys.add(key)
                if (n.type == NotificationType.LISTING_MATCH) {
                    listingIdFromDedupKey(key)?.let { userKeys.add("listing:$it") }
                }
            }
            keys[userId] = userKeys
            editPrefs[dedupKeysKey] = encodeDedupKeys(keys)
            editPrefs[notificationsKey] = encode(
                current.filter { it.userId != userId || it.type != NotificationType.LISTING_MATCH }
            )
            editPrefs[floodCleanupKey] = true
        }
        return true
    }

    suspend fun clearForUser(userId: String) {
        if (userId.isBlank()) return
        context.notificationDataStore.edit { prefs ->
            val current = decode(prefs[notificationsKey].orEmpty())
            val removing = current.filter { it.userId == userId }
            val keys = loadDedupKeys(prefs[dedupKeysKey].orEmpty()).toMutableMap()
            val userKeys = keys.getOrPut(userId) { mutableSetOf() }.toMutableSet()
            removing.forEach { n ->
                val key = n.dedupKey.ifBlank { dedupKeyFor(n) }
                userKeys.add(key)
                listingIdFromDedupKey(key)?.let { userKeys.add("listing:$it") }
            }
            keys[userId] = userKeys
            prefs[dedupKeysKey] = encodeDedupKeys(keys)
            prefs[notificationsKey] = encode(current.filter { it.userId != userId })
        }
    }

    suspend fun add(
        userId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        subtitle: String = "",
        dedupKey: String = ""
    ) {
        if (userId.isBlank()) return
        context.notificationDataStore.edit { prefs ->
            val current = decode(prefs[notificationsKey].orEmpty()).toMutableList()
            current.add(
                0,
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = type,
                    subtitle = subtitle,
                    dedupKey = dedupKey
                )
            )
            prefs[notificationsKey] = encode(current.take(80))
        }
    }

    private suspend fun recordDedupKey(userId: String, dedupKey: String) {
        context.notificationDataStore.edit { prefs ->
            val keys = loadDedupKeys(prefs[dedupKeysKey].orEmpty()).toMutableMap()
            val userKeys = keys.getOrPut(userId) { mutableSetOf() }.toMutableSet()
            userKeys.add(dedupKey)
            keys[userId] = userKeys
            prefs[dedupKeysKey] = encodeDedupKeys(keys)
        }
    }

    private fun loadDedupKeys(raw: String): Map<String, Set<String>> {
        if (raw.isBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            buildMap {
                root.keys().forEach { userId ->
                    val arr = root.optJSONArray(userId) ?: JSONArray()
                    put(userId, buildSet {
                        for (i in 0 until arr.length()) {
                            add(arr.optString(i))
                        }
                    })
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun encodeDedupKeys(keys: Map<String, Set<String>>): String {
        val root = JSONObject()
        keys.forEach { (userId, set) ->
            val arr = JSONArray()
            set.forEach { arr.put(it) }
            root.put(userId, arr)
        }
        return root.toString()
    }

    fun dedupKeyFor(notification: AppNotification): String =
        notification.dedupKey.ifBlank {
            "${notification.type.name}|${notification.title}|${notification.subtitle}|${notification.message}"
        }

    private fun listingIdFromDedupKey(key: String): String? =
        if (key.startsWith("listing:")) key.removePrefix("listing:") else null

    private fun encode(list: List<AppNotification>): String {
        val arr = JSONArray()
        list.forEach { n ->
            arr.put(
                JSONObject()
                    .put("id", n.id)
                    .put("userId", n.userId)
                    .put("title", n.title)
                    .put("message", n.message)
                    .put("subtitle", n.subtitle)
                    .put("timestamp", n.timestamp)
                    .put("type", n.type.name)
                    .put("dedupKey", n.dedupKey)
            )
        }
        return arr.toString()
    }

    private fun decode(raw: String): List<AppNotification> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val userId = o.optString("userId")
                    if (userId.isBlank()) continue
                    add(
                        AppNotification(
                            id = o.optString("id"),
                            userId = userId,
                            title = o.optString("title"),
                            message = o.optString("message"),
                            subtitle = o.optString("subtitle"),
                            timestamp = o.optLong("timestamp"),
                            type = runCatching {
                                NotificationType.valueOf(o.optString("type"))
                            }.getOrDefault(NotificationType.GENERAL),
                            dedupKey = o.optString("dedupKey")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
