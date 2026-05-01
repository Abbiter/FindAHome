package com.example.nestore_15

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.example.nestore_15.debug.DebugTools
import com.example.nestore_15.notifications.ListingMatchNotifier
import com.google.firebase.FirebaseApp

class NestoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DebugTools.init(this)
        val app = FirebaseApp.initializeApp(this)
        if (app == null) {
            Log.w("NestoreApplication", "Firebase not configured yet. Add google-services.json.")
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ListingMatchNotifier.LISTING_MATCH_CHANNEL_ID,
            "Listing Match Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for new listings that match saved filters."
        }
        manager.createNotificationChannel(channel)
    }
}
