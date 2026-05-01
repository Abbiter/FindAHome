package com.example.nestore_15.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.nestore_15.R
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.VerificationUiState
import com.example.nestore_15.viewmodel.VerificationViewModel
import com.google.android.material.button.MaterialButton

class VerificationActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: VerificationViewModel by viewModels {
        VerificationViewModel.factory(sessionManager)
    }

    private val pickDocument = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        val idle = viewModel.uiState.value as? VerificationUiState.Idle ?: return@registerForActivityResult
        when (idle.user.role) {
            UserRole.STUDENT -> viewModel.uploadEnrollmentDocument(uri)
            UserRole.PROVIDER -> viewModel.uploadOwnershipProof(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification)
        setupBackNavigation()

        val instructionText = findViewById<TextView>(R.id.instructionText)
        val uploadStatusText = findViewById<TextView>(R.id.uploadStatusText)
        val uploadButton = findViewById<MaterialButton>(R.id.uploadDocumentBtn)
        val submitButton = findViewById<MaterialButton>(R.id.submitVerificationBtn)

        uploadButton.setOnClickListener {
            pickDocument.launch("*/*")
        }

        submitButton.setOnClickListener {
            viewModel.submitForReview()
        }

        viewModel.userMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.consumeMessage()
            }
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                VerificationUiState.Loading -> {
                    submitButton.isEnabled = false
                    uploadButton.isEnabled = false
                    uploadStatusText.text = "Working…"
                }
                is VerificationUiState.Idle -> {
                    submitButton.isEnabled = true
                    uploadButton.isEnabled = true
                    instructionText.text = state.instruction
                    val hasDoc = when (state.user.role) {
                        UserRole.STUDENT -> state.user.verificationDocumentUrl.isNotBlank()
                        UserRole.PROVIDER -> state.user.providerOwnershipProofUrl.isNotBlank()
                    }
                    uploadStatusText.text = if (hasDoc) {
                        "Document on file — you can submit for review."
                    } else {
                        "No document uploaded yet"
                    }
                    uploadButton.text = when (state.user.role) {
                        UserRole.STUDENT -> "Upload enrollment proof"
                        UserRole.PROVIDER -> "Upload ownership proof"
                    }
                }
                VerificationUiState.Success -> {
                    Toast.makeText(this, "Verification submitted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
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
}
