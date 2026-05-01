package com.example.nestore_15.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.nestore_15.R
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.ProfileUiState
import com.example.nestore_15.viewmodel.ProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnProfileSubmitDocs).setOnClickListener {
            startActivity(Intent(this, VerificationActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnProfileLogout).setOnClickListener {
            confirmLogout()
        }
        findViewById<MaterialButton>(R.id.btnChangePassword).setOnClickListener {
            sendPasswordReset()
        }
        findViewById<MaterialButton>(R.id.btnDeleteAccount).setOnClickListener {
            Toast.makeText(this, "Account deletion is not available in the app yet.", Toast.LENGTH_LONG).show()
        }

        viewModel.uiState.observe(this) { state ->
            val progress = findViewById<ProgressBar>(R.id.profileProgress)
            val scroll = findViewById<View>(R.id.profileScroll)
            when (state) {
                ProfileUiState.Loading -> {
                    progress.visibility = View.VISIBLE
                    scroll.alpha = 0.5f
                }
                is ProfileUiState.Success -> {
                    progress.visibility = View.GONE
                    scroll.alpha = 1f
                    bindProfile(state.user)
                }
                ProfileUiState.Error -> {
                    progress.visibility = View.GONE
                    scroll.alpha = 1f
                    Toast.makeText(this, "Unable to load profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bindProfile(user: User) {
        findViewById<TextView>(R.id.tvProfileDisplayName).text = user.displayNameOrEmail()
        findViewById<TextView>(R.id.tvProfileEmailHeader).text = user.email
        findViewById<TextView>(R.id.tvProfileEmail).text = user.email
        findViewById<TextView>(R.id.tvProfileFullName).text = user.fullName.ifBlank { "—" }
        findViewById<TextView>(R.id.tvProfilePhone).text = user.phone.ifBlank { "—" }
        findViewById<TextView>(R.id.tvProfileRole).text = when (user.role) {
            UserRole.STUDENT -> "Student"
            UserRole.PROVIDER -> "Home Provider"
        }

        val status = user.effectiveVerificationStatus()
        val (_, detail, hint) = verificationCopy(user, status)
        findViewById<TextView>(R.id.tvProfileVerificationDetail).text = detail
        findViewById<TextView>(R.id.tvProfileVerificationHint).text = hint

        val headerDot = findViewById<View>(R.id.viewProfileHeaderVerificationDot)
        val dotColorRes = when (status) {
            VerificationStatus.VERIFIED -> R.color.available_green
            VerificationStatus.PENDING_REVIEW -> R.color.status_pending_orange
            VerificationStatus.NOT_SUBMITTED -> R.color.verification_dot_neutral
        }
        headerDot.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, dotColorRes))
        headerDot.contentDescription = "${getString(R.string.cd_verification_status)}: $detail"

        val studentCard = findViewById<View>(R.id.cardStudentDetails)
        val providerCard = findViewById<View>(R.id.cardProviderDetails)
        if (user.role == UserRole.STUDENT) {
            studentCard.visibility = View.VISIBLE
            providerCard.visibility = View.GONE
            findViewById<TextView>(R.id.tvStudentInstitution).text = user.studentInstitution.ifBlank { "—" }
            findViewById<TextView>(R.id.tvStudentId).text = user.studentId.ifBlank { "—" }
            val budget = user.studentBudgetMax?.let { "Up to BWP ${if (it % 1.0 == 0.0) it.toInt() else it} / mo" } ?: "—"
            findViewById<TextView>(R.id.tvStudentPreferences).text =
                "${user.studentPreferredLocation.ifBlank { "—" }} · $budget"
            findViewById<TextView>(R.id.tvStudentDocStatus).text =
                if (user.verificationDocumentUrl.isNotBlank()) "Document on file"
                else "No document uploaded"
        } else {
            studentCard.visibility = View.GONE
            providerCard.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvProviderBusiness).text = user.providerBusinessName.ifBlank { "—" }
            findViewById<TextView>(R.id.tvProviderAddress).text = user.providerContactAddress.ifBlank { "—" }
            findViewById<TextView>(R.id.tvProviderPropertyCount).text =
                user.providerPropertyCount?.toString() ?: "—"
            findViewById<TextView>(R.id.tvProviderOwnershipStatus).text =
                if (user.providerOwnershipProofUrl.isNotBlank()) "Proof on file"
                else "No proof uploaded"
        }

        val avatar = findViewById<ShapeableImageView>(R.id.ivProfileAvatar)
        if (user.photoUrl.isNotBlank()) {
            Glide.with(this).load(user.photoUrl).centerCrop().into(avatar)
        } else {
            avatar.setImageResource(R.drawable.profile_placeholder)
        }

        val submitDocs = findViewById<MaterialButton>(R.id.btnProfileSubmitDocs)
        submitDocs.visibility = if (status == VerificationStatus.VERIFIED) View.GONE else View.VISIBLE
    }

    private fun verificationCopy(user: User, status: VerificationStatus): Triple<String, String, String> {
        return when (status) {
            VerificationStatus.VERIFIED -> Triple(
                "Verified",
                "Your account is verified.",
                "You have full access to booking and listing features that require verification."
            )
            VerificationStatus.PENDING_REVIEW -> Triple(
                "Pending review",
                "Documents received; our team will review your profile.",
                "You can still browse the app. Some actions may stay limited until approval."
            )
            VerificationStatus.NOT_SUBMITTED -> Triple(
                "Not submitted",
                "Add your details and verification documents.",
                if (user.role == UserRole.STUDENT) {
                    "Upload proof of enrollment from Edit profile or Verification."
                } else {
                    "Upload ownership proof from Edit profile or Verification."
                }
            )
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
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

    private fun sendPasswordReset() {
        val user = (viewModel.uiState.value as? ProfileUiState.Success)?.user
        val email = user?.email?.takeIf { it.isNotBlank() } ?: FirebaseAuth.getInstance().currentUser?.email
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
