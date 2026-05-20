package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.ui.screens.SplashScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.SplashDestination
import com.example.nestore_15.viewmodel.SplashUiState
import com.example.nestore_15.viewmodel.SplashViewModel

class SplashActivity : ComponentActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private var hasNavigatedAway = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.uiState.value is SplashUiState.ConnectivityIssue) {
                        finishAffinity()
                    }
                }
            }
        )

        setContent {
            val state by viewModel.uiState.observeAsState(SplashUiState.Loading)
            FindAHomeTheme {
                SplashScreen(
                    isLoading = state is SplashUiState.Loading,
                    errorMessage = (state as? SplashUiState.ConnectivityIssue)?.message,
                    onRetry = { viewModel.startStartupFlow() }
                )
            }
        }

        viewModel.uiState.observe(this) { state ->
            if (state is SplashUiState.Ready && !hasNavigatedAway) {
                hasNavigatedAway = true
                navigateAndFinish(state.destination)
            }
        }

        viewModel.startStartupFlow()
    }

    private fun navigateAndFinish(destination: SplashDestination) {
        val intent = when (destination) {
            is SplashDestination.Login -> Intent(this, LoginActivity::class.java)
            is SplashDestination.Home -> {
                val destClass = when (destination.role) {
                    UserRole.STUDENT -> HomeActivity::class.java
                    UserRole.PROVIDER -> ProviderHomeActivity::class.java
                }
                Intent(this, destClass)
            }
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}
