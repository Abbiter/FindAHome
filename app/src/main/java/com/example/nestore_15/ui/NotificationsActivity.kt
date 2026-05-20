package com.example.nestore_15.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.NotificationsScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import kotlinx.coroutines.launch

class NotificationsActivity : ComponentActivity() {

    private val notificationStore by lazy { AppNotificationStore(applicationContext) }
    private val sessionManager by lazy { SessionManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = sessionManager.getCurrentUserId()
        if (userId.isNullOrBlank()) {
            Toast.makeText(this, "Please log in to view notifications", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            val notifications by notificationStore.notificationsForUser(userId)
                .collectAsState(initial = emptyList())
            FindAHomeTheme {
                NotificationsScreen(
                    notifications = notifications,
                    onBack = { finish() },
                    onClearAll = {
                        lifecycleScope.launch {
                            notificationStore.clearForUser(userId)
                            Toast.makeText(
                                this@NotificationsActivity,
                                "Notifications cleared",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}
