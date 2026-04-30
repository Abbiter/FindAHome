package com.example.nestore_15.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.R
import com.example.nestore_15.debug.DebugLogger
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.view.View

class ProviderHomeActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val roleFromIntent = intent.getStringExtra(RegisterActivity.EXTRA_ROLE_OVERRIDE)
                ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
            val role = sessionManager.userRole.first() ?: roleFromIntent
            // #region agent log
            DebugLogger.log(
                runId = "pre-fix",
                hypothesisId = "H5",
                location = "ProviderHomeActivity.kt:32",
                message = "ProviderHomeActivity gate evaluated role",
                data = mapOf("role" to (role?.name ?: "null"))
            )
            // #endregion
            when (role) {
                UserRole.PROVIDER -> {
                    setContentView(R.layout.provider_home)
                    bindProviderActions()
                }
                UserRole.STUDENT -> {
                    startActivity(
                        Intent(this@ProviderHomeActivity, HomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                    )
                    // #region agent log
                    DebugLogger.log(
                        runId = "pre-fix",
                        hypothesisId = "H5",
                        location = "ProviderHomeActivity.kt:49",
                        message = "ProviderHomeActivity redirected student to HomeActivity"
                    )
                    // #endregion
                    finish()
                }
                null -> {
                    startActivity(
                        Intent(this@ProviderHomeActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    // #region agent log
                    DebugLogger.log(
                        runId = "pre-fix",
                        hypothesisId = "H5",
                        location = "ProviderHomeActivity.kt:63",
                        message = "ProviderHomeActivity redirected null role to LoginActivity"
                    )
                    // #endregion
                    finish()
                }
            }
        }
    }

    private fun bindProviderActions() {
        observeVerificationStatus()
        findViewById<ImageView>(R.id.btnProviderProfileIcon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnManageListings).setOnClickListener {
            Toast.makeText(this, "Manage listings screen coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnAddProperty).setOnClickListener {
            Toast.makeText(this, "Add property screen coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnViewInquiries).setOnClickListener {
            Toast.makeText(this, "Inquiries screen coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnProviderProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnProviderLogout).setOnClickListener {
            lifecycleScope.launch {
                sessionManager.clearSession()
                startActivity(
                    Intent(this@ProviderHomeActivity, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                )
                finish()
            }
        }
    }

    private fun observeVerificationStatus() {
        val statusText = findViewById<TextView>(R.id.tvProviderVerificationStatus)
        val statusDot = findViewById<View>(R.id.viewProviderVerificationDot)

        lifecycleScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                val (label, colorRes) = when {
                    user == null -> "Not Verified" to R.color.status_not_verified_red
                    user.isVerified -> "Verified" to R.color.available_green
                    else -> "Pending" to R.color.status_pending_orange
                }
                statusText.text = label
                val color = ContextCompat.getColor(this@ProviderHomeActivity, colorRes)
                statusText.setTextColor(color)
                statusDot.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }
}
