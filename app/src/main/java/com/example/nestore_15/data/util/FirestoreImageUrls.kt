package com.example.nestore_15.data.util

import com.google.firebase.firestore.DocumentSnapshot

/** Reads `imageUrls` from Firestore whether stored as a list or a single string. */
internal fun DocumentSnapshot.parseImageUrlList(): List<String> {
    return when (val raw = get("imageUrls")) {
        null -> emptyList()
        is List<*> -> raw.mapNotNull { element ->
            when (element) {
                is String -> element.trim().takeIf { it.isNotEmpty() }
                else -> element?.toString()?.trim()?.takeIf { it.isNotEmpty() }
            }
        }
        is String -> {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) emptyList() else listOf(trimmed)
        }
        else -> emptyList()
    }
}
