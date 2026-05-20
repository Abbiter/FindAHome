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
import com.example.nestore_15.data.model.RegistrationRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.RegisterScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.RegisterUiState
import com.example.nestore_15.viewmodel.RegisterViewModel

class RegisterActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.factory(sessionManager = sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.observeAsState(RegisterUiState.Idle)
            val role by viewModel.selectedRole.observeAsState(RegistrationRole.STUDENT)
            val fieldErrors = (uiState as? RegisterUiState.InvalidInput)?.errors

            FindAHomeTheme {
                RegisterScreen(
                    selectedRole = role ?: RegistrationRole.STUDENT,
                    onRoleSelected = viewModel::selectRole,
                    fieldErrors = fieldErrors,
                    isSubmitting = false,
                    onSubmit = { fullName, phone, email, password ->
                        viewModel.submitRegistration(fullName, phone, email, password)
                    },
                    onBack = { finish() }
                )
            }
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                RegisterUiState.Idle -> Unit
                is RegisterUiState.Success -> {
                    viewModel.acknowledgeState()
                    startActivity(
                        Intent(this, CompleteProfileOnboardingActivity::class.java).apply {
                            putExtra(EXTRA_ROLE_OVERRIDE, state.role.name)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
                is RegisterUiState.InvalidInput -> viewModel.acknowledgeState()
                is RegisterUiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.acknowledgeState()
                }
            }
        }
    }

    companion object {
        const val EXTRA_ROLE_OVERRIDE = "extra_role_override"
    }
}
