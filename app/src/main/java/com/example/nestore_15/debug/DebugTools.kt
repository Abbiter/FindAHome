package com.example.nestore_15.debug

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo

object DebugTools {
    // Single global switch: set false to disable all debug/testing helpers.
    private const val ENABLE_DEBUG_TOOLS = true

    @Volatile
    private var application: Application? = null

    /**
     * Call from [android.app.Application.onCreate]. Also safe to call from any
     * [Context] via [ensureInitialized] so debug gates work even if init order differs.
     */
    fun init(app: Application) {
        application = app
    }

    fun ensureInitialized(context: Context) {
        if (application != null) return
        val app = context.applicationContext
        if (app is Application) application = app
    }

    private fun isDebuggableInstall(): Boolean {
        val app = application ?: return false
        return (app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    val available: Boolean
        get() = ENABLE_DEBUG_TOOLS && isDebuggableInstall()

    private var sessionDebugMode: Boolean = false

    fun activateSessionDebugMode() {
        if (available) sessionDebugMode = true
    }

    fun isSessionDebugModeActive(): Boolean = available && sessionDebugMode
}
