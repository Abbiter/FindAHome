package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.nestore_15.R
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
            var pendingDestination by remember { mutableStateOf<SplashDestination?>(null) }
            var exitAnimationFinished by remember { mutableStateOf(false) }

            LaunchedEffect(state) {
                val ready = state as? SplashUiState.Ready ?: return@LaunchedEffect
                if (pendingDestination == null) {
                    pendingDestination = ready.destination
                }
            }

            FindAHomeTheme {
                SplashScreen(
                    isLoading = state is SplashUiState.Loading,
                    errorMessage = (state as? SplashUiState.ConnectivityIssue)?.message,
                    isNavigatingAway = pendingDestination != null && !exitAnimationFinished,
                    onExitAnimationFinished = {
                        if (!exitAnimationFinished) {
                            exitAnimationFinished = true
                            pendingDestination?.let { dest ->
                                if (!hasNavigatedAway) {
                                    hasNavigatedAway = true
                                    navigateAndFinish(dest)
                                }
                            }
                        }
                    },
                    onRetry = {
                        pendingDestination = null
                        exitAnimationFinished = false
                        viewModel.startStartupFlow()
                    }
                )
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
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.splash_fade_in, R.anim.splash_fade_out)
        finish()
    }
}
