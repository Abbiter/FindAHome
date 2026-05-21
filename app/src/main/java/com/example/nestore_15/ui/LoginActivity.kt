package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.LoginScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.LoginError
import com.example.nestore_15.viewmodel.LoginUiState
import com.example.nestore_15.viewmodel.LoginViewModel

class LoginActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(sessionManager = sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.observeAsState(LoginUiState.Idle)
            FindAHomeTheme {
                LoginScreen(
                    isLoading = state is LoginUiState.Loading,
                    onLogin = { email, password -> viewModel.submitLogin(email, password) },
                    onRegister = {
                        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                    }
                )
            }
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                LoginUiState.Idle, LoginUiState.Loading -> Unit
                is LoginUiState.Success -> {
                    Toast.makeText(this, "Welcome to Find A Home!", Toast.LENGTH_SHORT).show()
                    viewModel.acknowledgeState()
                    val destination = when (state.role) {
                        UserRole.STUDENT -> HomeActivity::class.java
                        UserRole.PROVIDER -> ProviderHomeActivity::class.java
                    }
                    startActivity(
                        Intent(this, destination).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
                is LoginUiState.Error -> {
                    val message = when (state.reason) {
                        LoginError.EMPTY_CREDENTIALS -> "Please enter email and password"
                        LoginError.INVALID_CREDENTIALS -> "Invalid credentials"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    viewModel.acknowledgeState()
                }
            }
        }
    }
}
