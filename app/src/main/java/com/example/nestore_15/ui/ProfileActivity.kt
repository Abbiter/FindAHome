package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.nestore_15.R
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.ProfileUiState
import com.example.nestore_15.viewmodel.ProfileViewModel

class ProfileActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(sessionManager)
    }

    private lateinit var emailValue: TextView
    private lateinit var roleValue: TextView
    private lateinit var verificationValue: TextView
    private lateinit var verifyAccountBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        emailValue = findViewById(R.id.emailValue)
        roleValue = findViewById(R.id.roleValue)
        verificationValue = findViewById(R.id.verificationValue)
        verifyAccountBtn = findViewById(R.id.verifyAccountBtn)

        verifyAccountBtn.setOnClickListener {
            startActivity(Intent(this, VerificationActivity::class.java))
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                ProfileUiState.Loading -> {
                    verificationValue.text = "Loading..."
                    verifyAccountBtn.visibility = View.GONE
                }
                is ProfileUiState.Success -> {
                    emailValue.text = state.user.email
                    roleValue.text = when (state.user.role) {
                        UserRole.STUDENT -> "Student"
                        UserRole.PROVIDER -> "Provider"
                    }
                    verificationValue.text = if (state.user.isVerified) "Verified" else "Not Verified"
                    verifyAccountBtn.visibility = if (state.user.isVerified) View.GONE else View.VISIBLE
                }
                ProfileUiState.Error -> {
                    Toast.makeText(this, "Unable to load profile", Toast.LENGTH_SHORT).show()
                    verifyAccountBtn.visibility = View.GONE
                }
            }
        }
    }
}
