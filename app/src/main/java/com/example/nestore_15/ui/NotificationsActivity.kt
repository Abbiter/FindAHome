package com.example.nestore_15.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.ui.screens.NotificationsScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme

class NotificationsActivity : ComponentActivity() {

    private val notificationStore by lazy { AppNotificationStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val notifications by notificationStore.notificationsFlow.collectAsState(initial = emptyList())
            FindAHomeTheme {
                NotificationsScreen(
                    notifications = notifications,
                    onBack = { finish() }
                )
            }
        }
    }
}
