package com.example.nestore_15.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONObject

private val Context.chatSeenDataStore by preferencesDataStore(name = "chat_seen_v1")

/**
 * Stores last-notified chat message per conversation, scoped by user id.
 * This avoids re-notifying when the app resumes or when snapshot listeners re-fire.
 */
class ChatSeenStore(private val context: Context) {

    private val key = stringPreferencesKey("chat_last_notified_json")

    suspend fun lastNotifiedMessageId(userId: String, conversationId: String): String {
        if (userId.isBlank() || conversationId.isBlank()) return ""
        val prefs = context.chatSeenDataStore.data.first()
        val raw = prefs[key].orEmpty()
        if (raw.isBlank()) return ""
        return runCatching {
            val root = JSONObject(raw)
            val userObj = root.optJSONObject(userId) ?: JSONObject()
            userObj.optString(conversationId).orEmpty()
        }.getOrDefault("")
    }

    suspend fun setLastNotifiedMessageId(userId: String, conversationId: String, messageId: String) {
        if (userId.isBlank() || conversationId.isBlank() || messageId.isBlank()) return
        context.chatSeenDataStore.edit { prefs ->
            val root = runCatching { JSONObject(prefs[key].orEmpty()) }.getOrDefault(JSONObject())
            val userObj = root.optJSONObject(userId) ?: JSONObject()
            userObj.put(conversationId, messageId)
            root.put(userId, userObj)
            prefs[key] = root.toString()
        }
    }
}

