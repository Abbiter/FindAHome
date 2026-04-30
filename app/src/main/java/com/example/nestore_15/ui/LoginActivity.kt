package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.R
import com.example.nestore_15.debug.DebugLogger
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.LoginError
import com.example.nestore_15.viewmodel.LoginUiState
import com.example.nestore_15.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(sessionManager = sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.login)
        testFirebaseConnection(this)

        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val registerBtn = findViewById<Button>(R.id.registerBtn)

        viewModel.uiState.observe(this) { state ->
            when (state) {
                LoginUiState.Idle -> Unit
                LoginUiState.Loading -> Unit
                is LoginUiState.Success -> {
                    // #region agent log
                    DebugLogger.log(
                        runId = "pre-fix",
                        hypothesisId = "H1",
                        location = "LoginActivity.kt:49",
                        message = "Login success observer reached"
                    )
                    // #endregion
                    Toast.makeText(this, "Welcome to Find A Home!", Toast.LENGTH_SHORT).show()
                    viewModel.acknowledgeState()
                    lifecycleScope.launch {
                        val role = state.role
                        // #region agent log
                        DebugLogger.log(
                            runId = "post-fix",
                            hypothesisId = "H6",
                            location = "LoginActivity.kt:64",
                            message = "Using role from LoginUiState.Success payload",
                            data = mapOf("role" to role.name)
                        )
                        // #endregion
                        val destination = when (role) {
                            UserRole.STUDENT -> HomeActivity::class.java
                            UserRole.PROVIDER -> ProviderHomeActivity::class.java
                        }
                        // #region agent log
                        DebugLogger.log(
                            runId = "pre-fix",
                            hypothesisId = "H2",
                            location = "LoginActivity.kt:66",
                            message = "Resolved role and destination after login",
                            data = mapOf("role" to role.name, "destination" to destination.simpleName)
                        )
                        // #endregion
                        startActivity(Intent(this@LoginActivity, destination))
                        // #region agent log
                        DebugLogger.log(
                            runId = "pre-fix",
                            hypothesisId = "H2",
                            location = "LoginActivity.kt:74",
                            message = "startActivity invoked from LoginActivity"
                        )
                        // #endregion
                        finish()
                    }
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

        loginBtn.setOnClickListener {
            viewModel.submitLogin(email.text.toString(), password.text.toString())
        }

        registerBtn.setOnClickListener {
            try {
                startActivity(Intent(this, RegisterActivity::class.java))
            } catch (e: Exception) {
                android.util.Log.e("NESTORA_DEBUG", "Transition failed: ${e.message}")
                Toast.makeText(this, "Navigation Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
