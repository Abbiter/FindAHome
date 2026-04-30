package com.example.nestore_15.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.nestore_15.R
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.VerificationUiState
import com.example.nestore_15.viewmodel.VerificationViewModel

class VerificationActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: VerificationViewModel by viewModels {
        VerificationViewModel.factory(sessionManager)
    }

    private lateinit var instructionText: TextView
    private lateinit var uploadStatusText: TextView
    private lateinit var uploadButton: Button
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification)

        instructionText = findViewById(R.id.instructionText)
        uploadStatusText = findViewById(R.id.uploadStatusText)
        uploadButton = findViewById(R.id.uploadDocumentBtn)
        submitButton = findViewById(R.id.submitVerificationBtn)

        uploadButton.setOnClickListener {
            viewModel.mockUploadDocument()
            Toast.makeText(this, "Document uploaded (mock)", Toast.LENGTH_SHORT).show()
        }

        submitButton.setOnClickListener {
            viewModel.submitVerification()
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                VerificationUiState.Loading -> {
                    submitButton.isEnabled = false
                    uploadButton.isEnabled = false
                    uploadStatusText.text = "Loading..."
                }
                is VerificationUiState.Idle -> {
                    submitButton.isEnabled = true
                    uploadButton.isEnabled = true
                    instructionText.text = state.instruction
                    uploadStatusText.text = if (state.isDocumentUploaded) {
                        "Document ready for submission"
                    } else {
                        "No document uploaded yet"
                    }
                }
                VerificationUiState.Success -> {
                    Toast.makeText(this, "Verification submitted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is VerificationUiState.Error -> {
                    submitButton.isEnabled = true
                    uploadButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
