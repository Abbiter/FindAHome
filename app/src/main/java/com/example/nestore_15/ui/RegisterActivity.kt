package com.example.nestore_15.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.example.nestore_15.R
import com.example.nestore_15.data.model.RegistrationRole
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.RegisterFieldErrors
import com.example.nestore_15.viewmodel.RegisterUiState
import com.example.nestore_15.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.factory(sessionManager = sessionManager)
    }

    private lateinit var btnStudent: AppCompatButton
    private lateinit var btnProvider: AppCompatButton
    private lateinit var fullNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var createAccountBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        setupBackNavigation()

        btnStudent = findViewById(R.id.btnStudent)
        btnProvider = findViewById(R.id.btnProvider)
        fullNameInput = findViewById(R.id.fullNameInput)
        phoneInput = findViewById(R.id.phoneInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        createAccountBtn = findViewById(R.id.createAccountBtn)
        setupPasswordVisibilityToggle(passwordInput)

        viewModel.selectedRole.observe(this) { role ->
            updateRoleToggleUi(role)
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                RegisterUiState.Idle -> Unit
                is RegisterUiState.Success -> {
                    clearFieldErrors()
                    viewModel.acknowledgeState()
                    startActivity(
                        Intent(this@RegisterActivity, CompleteProfileOnboardingActivity::class.java).apply {
                            putExtra(EXTRA_ROLE_OVERRIDE, state.role.name)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
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
                fullNameInput.text.toString(),
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
        fullNameInput.error = null
        phoneInput.error = null
        emailInput.error = null
        passwordInput.error = null
    }

    private fun renderValidationErrors(errors: RegisterFieldErrors) {
        fullNameInput.error = when {
            errors.fullNameRequired -> "Full name is required"
            errors.fullNameTooShort -> "Enter at least 2 characters"
            else -> null
        }
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

    private fun setupBackNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupPasswordVisibilityToggle(passwordField: EditText) {
        var isVisible = false
        passwordField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val tappedEnd = event.rawX >= (passwordField.right - passwordField.compoundPaddingEnd)
                if (tappedEnd && passwordField.compoundDrawables[drawableEnd] != null) {
                    isVisible = !isVisible
                    passwordField.transformationMethod = if (isVisible) {
                        HideReturnsTransformationMethod.getInstance()
                    } else {
                        PasswordTransformationMethod.getInstance()
                    }
                    passwordField.setSelection(passwordField.text?.length ?: 0)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    companion object {
        const val EXTRA_ROLE_OVERRIDE = "extra_role_override"
    }
}
