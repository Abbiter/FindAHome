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
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.ui.screens.ProfileScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.ProfileUiState
import com.example.nestore_15.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.observeAsState(ProfileUiState.Loading)
            FindAHomeTheme {
                ProfileScreen(
                    uiState = state ?: ProfileUiState.Loading,
                    onEditProfile = {
                        startActivity(Intent(this, EditProfileActivity::class.java))
                    },
                    onVerify = {
                        startActivity(Intent(this, VerificationActivity::class.java))
                    },
                    onLogout = { confirmLogout() },
                    onChangePassword = { sendPasswordReset(state) },
                    showBack = true,
                    onBack = { finish() }
                )
            }
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Log out")
            .setMessage("Sign out of this account?")
            .setPositiveButton("Log out") { _, _ ->
                lifecycleScope.launch {
                    sessionManager.clearSession()
                    startActivity(
                        Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun sendPasswordReset(state: ProfileUiState?) {
        val email = (state as? ProfileUiState.Success)?.user?.email?.takeIf { it.isNotBlank() }
            ?: FirebaseAuth.getInstance().currentUser?.email
        if (email.isNullOrBlank()) {
            Toast.makeText(this, "No email on file", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Could not send email", Toast.LENGTH_LONG).show()
                }
            }
    }
}
