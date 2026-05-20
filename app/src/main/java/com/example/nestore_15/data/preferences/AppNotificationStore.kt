package com.example.nestore_15.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nestore_15.data.model.AppNotification
import com.example.nestore_15.data.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.notificationDataStore by preferencesDataStore(name = "app_notifications")

class AppNotificationStore(private val context: Context) {

    private val key = stringPreferencesKey("notifications_json")

    val notificationsFlow: Flow<List<AppNotification>> =
        context.notificationDataStore.data.map { prefs ->
            decode(prefs[key].orEmpty())
        }

    suspend fun add(
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL
    ) {
        context.notificationDataStore.edit { prefs ->
            val current = decode(prefs[key].orEmpty()).toMutableList()
            current.add(
                0,
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = type
                )
            )
            prefs[key] = encode(current.take(50))
        }
    }

    private fun encode(list: List<AppNotification>): String {
        val arr = JSONArray()
        list.forEach { n ->
            arr.put(
                JSONObject()
                    .put("id", n.id)
                    .put("title", n.title)
                    .put("message", n.message)
                    .put("timestamp", n.timestamp)
                    .put("type", n.type.name)
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
                    add(
                        AppNotification(
                            id = o.optString("id"),
                            title = o.optString("title"),
                            message = o.optString("message"),
                            timestamp = o.optLong("timestamp"),
                            type = runCatching {
                                NotificationType.valueOf(o.optString("type"))
                            }.getOrDefault(NotificationType.GENERAL)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
