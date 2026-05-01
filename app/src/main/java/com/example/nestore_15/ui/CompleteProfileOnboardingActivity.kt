package com.example.nestore_15.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.R
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.data.session.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Post-registration step: optional role-specific profile fields.
 * Users can skip and finish later from Profile.
 */
class CompleteProfileOnboardingActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val userRepository = UserRepository()

    private var enrollmentUri: Uri? = null
    private var ownershipUri: Uri? = null

    private val pickEnrollment = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        enrollmentUri = uri
        findViewById<TextView>(R.id.tvOnboardEnrollmentStatus).text =
            if (uri != null) "File selected — will upload when you save" else ""
    }

    private val pickOwnership = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        ownershipUri = uri
        findViewById<TextView>(R.id.tvOnboardOwnershipStatus).text =
            if (uri != null) "File selected — will upload when you save" else ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.complete_profile_onboarding)

        val role = intent.getStringExtra(RegisterActivity.EXTRA_ROLE_OVERRIDE)
            ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: UserRole.STUDENT

        val toolbar = findViewById<Toolbar>(R.id.onboardingToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { goHome(role) }

        findViewById<TextView>(R.id.tvOnboardingRoleIntro).text = when (role) {
            UserRole.STUDENT -> "Student details"
            UserRole.PROVIDER -> "Provider details"
        }
        findViewById<View>(R.id.onboardingStudentBlock).visibility =
            if (role == UserRole.STUDENT) View.VISIBLE else View.GONE
        findViewById<View>(R.id.onboardingProviderBlock).visibility =
            if (role == UserRole.PROVIDER) View.VISIBLE else View.GONE

        findViewById<MaterialButton>(R.id.btnOnboardPickEnrollment).setOnClickListener {
            pickEnrollment.launch("*/*")
        }
        findViewById<MaterialButton>(R.id.btnOnboardPickOwnership).setOnClickListener {
            pickOwnership.launch("*/*")
        }

        findViewById<MaterialButton>(R.id.btnOnboardingSkip).setOnClickListener { goHome(role) }

        findViewById<MaterialButton>(R.id.btnOnboardingSave).setOnClickListener {
            saveAndContinue(role)
        }
    }

    private fun saveAndContinue(role: UserRole) {
        val pb = findViewById<ProgressBar>(R.id.pbOnboarding)
        val saveBtn = findViewById<MaterialButton>(R.id.btnOnboardingSave)
        pb.visibility = View.VISIBLE
        saveBtn.isEnabled = false

        lifecycleScope.launch {
            // StateFlow starts as null until auth + Firestore emit; .first() was always null here.
            val base = sessionManager.awaitCurrentUser()
            if (base == null) {
                Toast.makeText(this@CompleteProfileOnboardingActivity, "Session expired", Toast.LENGTH_SHORT).show()
                goToLogin()
                return@launch
            }
            runCatching {
                var next = when (role) {
                    UserRole.STUDENT -> base.copy(
                        studentInstitution = findViewById<EditText>(R.id.etOnboardStudentInstitution).text.toString().trim(),
                        studentId = findViewById<EditText>(R.id.etOnboardStudentId).text.toString().trim(),
                        studentPreferredLocation = findViewById<EditText>(R.id.etOnboardStudentLocation).text.toString().trim(),
                        studentBudgetMax = findViewById<EditText>(R.id.etOnboardStudentBudget).text.toString()
                            .replace(",", ".").trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                    )
                    UserRole.PROVIDER -> base.copy(
                        providerBusinessName = findViewById<EditText>(R.id.etOnboardProviderBusiness).text.toString().trim(),
                        providerContactAddress = findViewById<EditText>(R.id.etOnboardProviderAddress).text.toString().trim(),
                        providerPropertyCount = findViewById<EditText>(R.id.etOnboardProviderCount).text.toString()
                            .trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                    )
                }
                if (enrollmentUri != null && role == UserRole.STUDENT) {
                    val url = userRepository.uploadVerificationDocument(base.id, enrollmentUri!!)
                    next = next.copy(
                        verificationDocumentUrl = url,
                        verificationStatus = VerificationStatus.PENDING_REVIEW,
                        isVerified = false
                    )
                }
                if (ownershipUri != null && role == UserRole.PROVIDER) {
                    val url = userRepository.uploadOwnershipProof(base.id, ownershipUri!!)
                    next = next.copy(
                        providerOwnershipProofUrl = url,
                        verificationStatus = VerificationStatus.PENDING_REVIEW,
                        isVerified = false
                    )
                }
                sessionManager.saveUser(next)
            }.onSuccess {
                Toast.makeText(this@CompleteProfileOnboardingActivity, "Profile saved", Toast.LENGTH_SHORT).show()
                goHome(role)
            }.onFailure {
                Toast.makeText(
                    this@CompleteProfileOnboardingActivity,
                    it.message ?: "Could not save",
                    Toast.LENGTH_LONG
                ).show()
            }
            pb.visibility = View.GONE
            saveBtn.isEnabled = true
        }
    }

    private fun goHome(role: UserRole) {
        val dest = when (role) {
            UserRole.STUDENT -> HomeActivity::class.java
            UserRole.PROVIDER -> ProviderHomeActivity::class.java
        }
        startActivity(
            Intent(this, dest).apply {
                putExtra(RegisterActivity.EXTRA_ROLE_OVERRIDE, role.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}
