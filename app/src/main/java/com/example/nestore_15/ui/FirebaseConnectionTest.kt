package com.example.nestore_15.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG_FIREBASE_TEST = "FirebaseTest"

@Volatile
private var hasRunFirebaseConnectionTest = false

// Temporary Firebase setup check; remove after verifying integration.
fun testFirebaseConnection(context: Context) {
    if (hasRunFirebaseConnectionTest) return
    hasRunFirebaseConnectionTest = true

    try {
        val appContext = context.applicationContext

        val auth = FirebaseAuth.getInstance()
        if (auth != null) {
            Log.d(TAG_FIREBASE_TEST, "FirebaseAuth instance initialized")
        } else {
            Log.e(TAG_FIREBASE_TEST, "FirebaseAuth instance unavailable")
        }

        val firestore = FirebaseFirestore.getInstance()
        val payload = hashMapOf(
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "test"
        )

        firestore.collection("test_connection")
            .add(payload)
            .addOnSuccessListener {
                Log.d(TAG_FIREBASE_TEST, "Firebase connected successfully")
                Toast.makeText(appContext, "Firebase connection successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Log.e(TAG_FIREBASE_TEST, "Firebase connection failed: ${error.message}", error)
                Toast.makeText(appContext, "Firebase connection failed", Toast.LENGTH_SHORT).show()
            }
    } catch (error: Exception) {
        Log.e(TAG_FIREBASE_TEST, "Firebase connection failed: ${error.message}", error)
        Toast.makeText(context.applicationContext, "Firebase connection failed", Toast.LENGTH_SHORT)
            .show()
    }
}
