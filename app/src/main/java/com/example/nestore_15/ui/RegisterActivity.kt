package com.example.nestore_15.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.nestore_15.R
import com.example.nestore_15.data.model.RegistrationRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.RegisterFieldErrors
import com.example.nestore_15.viewmodel.RegisterUiState
import com.example.nestore_15.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.factory(sessionManager = sessionManager)
    }

    private lateinit var btnStudent: Button
    private lateinit var btnProvider: Button
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var createAccountBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        btnStudent = findViewById(R.id.btnStudent)
        btnProvider = findViewById(R.id.btnProvider)
        phoneInput = findViewById(R.id.phoneInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        createAccountBtn = findViewById(R.id.createAccountBtn)

        viewModel.selectedRole.observe(this) { role ->
            updateRoleToggleUi(role)
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                RegisterUiState.Idle -> Unit
                RegisterUiState.Success -> {
                    clearFieldErrors()
                    Toast.makeText(this, "Registration completed successfully", Toast.LENGTH_LONG).show()
                    viewModel.acknowledgeState()
                }
                is RegisterUiState.InvalidInput -> {
                    renderValidationErrors(state.errors)
                    viewModel.acknowledgeState()
                }
            }
        }

        btnStudent.setOnClickListener { viewModel.selectRole(RegistrationRole.STUDENT) }
        btnProvider.setOnClickListener { viewModel.selectRole(RegistrationRole.HOME_PROVIDER) }

        createAccountBtn.setOnClickListener {
            clearFieldErrors()
            viewModel.submitRegistration(
                phoneInput.text.toString(),
                emailInput.text.toString(),
                passwordInput.text.toString()
            )
        }
    }

    private fun updateRoleToggleUi(role: RegistrationRole) {
        when (role) {
            RegistrationRole.STUDENT -> {
                btnStudent.setBackgroundResource(R.drawable.btn_primary_gradient)
                btnStudent.setTextColor(Color.WHITE)
                btnProvider.setBackgroundResource(android.R.color.transparent)
                btnProvider.setTextColor(getColor(R.color.deep_royal_text))
            }
            RegistrationRole.HOME_PROVIDER -> {
                btnProvider.setBackgroundResource(R.drawable.btn_primary_gradient)
                btnProvider.setTextColor(Color.WHITE)
                btnStudent.setBackgroundResource(android.R.color.transparent)
                btnStudent.setTextColor(getColor(R.color.deep_royal_text))
            }
        }
    }

    private fun clearFieldErrors() {
        phoneInput.error = null
        emailInput.error = null
        passwordInput.error = null
    }

    private fun renderValidationErrors(errors: RegisterFieldErrors) {
        phoneInput.error = if (errors.phoneInvalid) "Enter a valid phone number (at least 8 digits)" else null
        emailInput.error = when {
            errors.emailRequired -> "Email is required"
            errors.emailInvalid -> "Enter a valid email address"
            else -> null
        }
        passwordInput.error = when {
            errors.passwordRequired -> "Password is required"
            errors.passwordTooShort -> "Use at least 8 characters"
            else -> null
        }
    }
}
