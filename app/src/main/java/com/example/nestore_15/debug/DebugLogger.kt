package com.example.nestore_15.debug

import org.json.JSONObject
import java.io.File

object DebugLogger {
    private const val LOG_PATH = "debug-640022.log"
    private const val SESSION_ID = "640022"

    fun log(
        runId: String,
        hypothesisId: String,
        location: String,
        message: String,
        data: Map<String, Any?> = emptyMap()
    ) {
        runCatching {
            val payload = JSONObject().apply {
                put("sessionId", SESSION_ID)
                put("runId", runId)
                put("hypothesisId", hypothesisId)
                put("location", location)
                put("message", message)
                put("data", JSONObject(data))
                put("timestamp", System.currentTimeMillis())
            }
            File(LOG_PATH).appendText(payload.toString() + "\n")
        }
    }
}
