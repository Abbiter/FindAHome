package com.example.nestore_15

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.nestore_15.notifications.AppNotificationHelper
import com.google.firebase.FirebaseApp

class NestoreApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        val app = FirebaseApp.initializeApp(this)
        if (app == null) {
            Log.w("NestoreApplication", "Firebase not configured yet. Add google-services.json.")
        }
        createNotificationChannels()
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            Triple(
                AppNotificationHelper.CHANNEL_LISTINGS,
                "New listings",
                "Alerts when listings match your filters"
            ),
            Triple(
                AppNotificationHelper.CHANNEL_RESERVATIONS,
                "Reservations",
                "Alerts when a property is reserved"
            ),
            Triple(
                AppNotificationHelper.CHANNEL_MESSAGES,
                "Messages",
                "New chat messages"
            ),
            Triple(
                AppNotificationHelper.CHANNEL_GENERAL,
                "General",
                "Other app updates"
            )
        )
        channels.forEach { (id, name, description) ->
            manager.createNotificationChannel(
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                    this.description = description
                }
            )
        }
    }
}
