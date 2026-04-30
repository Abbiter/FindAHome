package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.nestore_15.R
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.viewmodel.SplashDestination
import com.example.nestore_15.viewmodel.SplashUiState
import com.example.nestore_15.viewmodel.SplashViewModel
import com.google.android.material.button.MaterialButton

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private var hasNavigatedAway = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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

        findViewById<MaterialButton>(R.id.btnSplashRetry).setOnClickListener {
            showLoading()
            viewModel.startStartupFlow()
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                SplashUiState.Loading -> showLoading()
                is SplashUiState.Ready -> {
                    if (hasNavigatedAway) return@observe
                    hasNavigatedAway = true
                    showLoading()
                    navigateAndFinish(state.destination)
                }
                is SplashUiState.ConnectivityIssue -> showError(state.message)
            }
        }

        viewModel.startStartupFlow()
    }

    private fun showLoading() {
        findViewById<ImageView>(R.id.splashLogo).visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.splashProgress).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.splashErrorContainer).visibility = View.GONE
    }

    private fun showError(message: String) {
        findViewById<ProgressBar>(R.id.splashProgress).visibility = View.GONE
        findViewById<LinearLayout>(R.id.splashErrorContainer).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvSplashError).text =
            getString(R.string.splash_connection_issue, message)
    }

    private fun navigateAndFinish(destination: SplashDestination) {
        val intent = when (destination) {
            is SplashDestination.Login ->
                Intent(this, LoginActivity::class.java)
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
