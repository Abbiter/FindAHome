package com.example.nestore_15.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.nestore_15.data.preferences.AppNotificationStore
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.PaymentScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.PaymentViewModel

class PaymentActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val listingId = intent.getStringExtra(EXTRA_LISTING_ID).orEmpty()
        if (listingId.isBlank()) {
            Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val notificationStore = AppNotificationStore(applicationContext)
        val viewModel: PaymentViewModel by viewModels {
            PaymentViewModel.factory(listingId, notificationStore)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            FindAHomeTheme {
                PaymentScreen(
                    uiState = uiState,
                    onBack = { finish() },
                    onConfirmPayment = {
                        val uid = sessionManager.getCurrentUserId()
                        if (uid.isNullOrBlank()) {
                            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.confirmPayment(uid)
                        }
                    },
                    onBackToHome = { finishAffinity() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_LISTING_ID = "extra_listing_id"
    }
}
