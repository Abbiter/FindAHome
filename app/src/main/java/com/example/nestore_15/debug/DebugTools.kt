package com.example.nestore_15.debug

object DebugTools {
    // Single global switch: set false to disable all debug/testing helpers.
    private const val ENABLE_DEBUG_TOOLS = true

    val available: Boolean
        get() = ENABLE_DEBUG_TOOLS && isDebugBuild()

    private var sessionDebugMode: Boolean = false

    fun activateSessionDebugMode() {
        if (available) sessionDebugMode = true
    }

    fun isSessionDebugModeActive(): Boolean = available && sessionDebugMode

    private fun isDebugBuild(): Boolean {
        return runCatching {
            val clazz = Class.forName("com.example.nestore_15.BuildConfig")
            clazz.getField("DEBUG").getBoolean(null)
        }.getOrDefault(false)
    }
}
