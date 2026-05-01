package com.example.nestore_15.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.nestore_15.R
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.viewmodel.EditProfileUiState
import com.example.nestore_15.viewmodel.EditProfileViewModel
import com.google.android.material.button.MaterialButton

class EditProfileActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val viewModel: EditProfileViewModel by viewModels {
        EditProfileViewModel.factory(sessionManager)
    }

    private var profilePhotoUri: Uri? = null
    private var enrollmentDocUri: Uri? = null
    private var ownershipDocUri: Uri? = null

    private enum class DocPick { NONE, ENROLLMENT, OWNERSHIP }
    private var pendingDocPick = DocPick.NONE

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        profilePhotoUri = uri
        findViewById<TextView>(R.id.tvEditPhotoStatus).text =
            if (uri != null) "New photo selected" else "No new photo"
    }

    private val pickDocument = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        when (pendingDocPick) {
            DocPick.ENROLLMENT -> {
                enrollmentDocUri = uri
                findViewById<MaterialButton>(R.id.btnPickEnrollmentProof).text =
                    if (uri != null) "Enrollment proof selected" else "Upload enrollment proof"
            }
            DocPick.OWNERSHIP -> {
                ownershipDocUri = uri
                findViewById<MaterialButton>(R.id.btnPickOwnershipProof).text =
                    if (uri != null) "Ownership proof selected" else "Upload ownership proof"
            }
            DocPick.NONE -> Unit
        }
        pendingDocPick = DocPick.NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        val toolbar = findViewById<Toolbar>(R.id.editProfileToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        findViewById<MaterialButton>(R.id.btnPickProfilePhoto).setOnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        findViewById<MaterialButton>(R.id.btnPickEnrollmentProof).setOnClickListener {
            pendingDocPick = DocPick.ENROLLMENT
            pickDocument.launch("*/*")
        }
        findViewById<MaterialButton>(R.id.btnPickOwnershipProof).setOnClickListener {
            pendingDocPick = DocPick.OWNERSHIP
            pickDocument.launch("*/*")
        }

        findViewById<MaterialButton>(R.id.btnSaveProfile).setOnClickListener { saveProfile() }

        viewModel.uiState.observe(this) { state ->
            val pb = findViewById<ProgressBar>(R.id.pbEditProfile)
            val saveBtn = findViewById<MaterialButton>(R.id.btnSaveProfile)
            when (state) {
                EditProfileUiState.Loading -> {
                    pb.visibility = View.VISIBLE
                    saveBtn.isEnabled = false
                }
                is EditProfileUiState.Ready -> {
                    pb.visibility = View.GONE
                    saveBtn.isEnabled = true
                    populateForm(state.user)
                }
                EditProfileUiState.Saving -> {
                    pb.visibility = View.VISIBLE
                    saveBtn.isEnabled = false
                }
                EditProfileUiState.SaveSuccess -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        viewModel.saveError.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.consumeSaveError()
            }
        }
    }

    private fun populateForm(user: User) {
        findViewById<EditText>(R.id.etEditFullName).setText(user.fullName)
        findViewById<EditText>(R.id.etEditPhone).setText(user.phone)
        findViewById<TextView>(R.id.tvEditEmailReadonly).text = user.email
        findViewById<TextView>(R.id.tvEditPhotoStatus).text =
            if (user.photoUrl.isNotBlank()) "Current photo on file" else "No profile photo yet"

        val studentLabel = findViewById<TextView>(R.id.labelStudentSection)
        val providerLabel = findViewById<TextView>(R.id.labelProviderSection)
        if (user.role == UserRole.STUDENT) {
            studentLabel.visibility = View.VISIBLE
            listOf(
                R.id.etStudentInstitution,
                R.id.etStudentId,
                R.id.etStudentPreferredLocation,
                R.id.etStudentBudget,
                R.id.btnPickEnrollmentProof
            ).forEach { findViewById<View>(it).visibility = View.VISIBLE }
            providerLabel.visibility = View.GONE
            listOf(
                R.id.etProviderBusinessName,
                R.id.etProviderAddress,
                R.id.etProviderPropertyCount,
                R.id.btnPickOwnershipProof
            ).forEach { findViewById<View>(it).visibility = View.GONE }

            findViewById<EditText>(R.id.etStudentInstitution).setText(user.studentInstitution)
            findViewById<EditText>(R.id.etStudentId).setText(user.studentId)
            findViewById<EditText>(R.id.etStudentPreferredLocation).setText(user.studentPreferredLocation)
            findViewById<EditText>(R.id.etStudentBudget).setText(
                user.studentBudgetMax?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty()
            )
        } else {
            providerLabel.visibility = View.VISIBLE
            listOf(
                R.id.etProviderBusinessName,
                R.id.etProviderAddress,
                R.id.etProviderPropertyCount,
                R.id.btnPickOwnershipProof
            ).forEach { findViewById<View>(it).visibility = View.VISIBLE }
            studentLabel.visibility = View.GONE
            listOf(
                R.id.etStudentInstitution,
                R.id.etStudentId,
                R.id.etStudentPreferredLocation,
                R.id.etStudentBudget,
                R.id.btnPickEnrollmentProof
            ).forEach { findViewById<View>(it).visibility = View.GONE }

            findViewById<EditText>(R.id.etProviderBusinessName).setText(user.providerBusinessName)
            findViewById<EditText>(R.id.etProviderAddress).setText(user.providerContactAddress)
            findViewById<EditText>(R.id.etProviderPropertyCount).setText(
                user.providerPropertyCount?.toString().orEmpty()
            )
        }
    }

    private fun readDraft(base: User): User {
        val budgetRaw = findViewById<EditText>(R.id.etStudentBudget).text.toString().replace(",", ".").trim()
        val countRaw = findViewById<EditText>(R.id.etProviderPropertyCount).text.toString().trim()
        return base.copy(
            fullName = findViewById<EditText>(R.id.etEditFullName).text.toString().trim(),
            phone = findViewById<EditText>(R.id.etEditPhone).text.toString().trim(),
            studentInstitution = findViewById<EditText>(R.id.etStudentInstitution).text.toString().trim(),
            studentId = findViewById<EditText>(R.id.etStudentId).text.toString().trim(),
            studentPreferredLocation = findViewById<EditText>(R.id.etStudentPreferredLocation).text.toString().trim(),
            studentBudgetMax = budgetRaw.toDoubleOrNull(),
            providerBusinessName = findViewById<EditText>(R.id.etProviderBusinessName).text.toString().trim(),
            providerContactAddress = findViewById<EditText>(R.id.etProviderAddress).text.toString().trim(),
            providerPropertyCount = countRaw.toIntOrNull()
        )
    }

    private fun saveProfile() {
        val state = viewModel.uiState.value
        val base = (state as? EditProfileUiState.Ready)?.user ?: return
        val draft = readDraft(base)
        if (draft.fullName.isBlank()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.save(
            draft = draft,
            profilePhotoUri = profilePhotoUri,
            verificationOrEnrollmentUri = enrollmentDocUri,
            ownershipProofUri = ownershipDocUri
        )
    }
}
