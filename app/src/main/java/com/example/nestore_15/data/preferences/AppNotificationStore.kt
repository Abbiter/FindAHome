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

private val Context.notificationDataStore by preferencesDataStore(name = "app_notifications_v2")

class AppNotificationStore(private val context: Context) {

    private val key = stringPreferencesKey("notifications_json")

    fun notificationsForUser(userId: String): Flow<List<AppNotification>> =
        notificationsFlow.map { list ->
            if (userId.isBlank()) emptyList()
            else list.filter { it.userId == userId || it.userId.isBlank() }
        }

    val notificationsFlow: Flow<List<AppNotification>> =
        context.notificationDataStore.data.map { prefs ->
            decode(prefs[key].orEmpty())
        }

    suspend fun add(
        userId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        subtitle: String = ""
    ) {
        if (userId.isBlank()) return
        context.notificationDataStore.edit { prefs ->
            val current = decode(prefs[key].orEmpty()).toMutableList()
            current.add(
                0,
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = type,
                    subtitle = subtitle
                )
            )
            prefs[key] = encode(current.take(80))
        }
    }

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
                            userId = o.optString("userId"),
                            title = o.optString("title"),
                            message = o.optString("message"),
                            subtitle = o.optString("subtitle"),
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
