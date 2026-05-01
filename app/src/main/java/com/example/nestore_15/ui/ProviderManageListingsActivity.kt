package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Property
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.session.ProviderSessionResult
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.data.session.resolveProviderSession
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProviderManageListingsActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val propertyRepository = PropertyRepository()
    private lateinit var adapter: ProviderManagePropertiesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            when (val gate = sessionManager.resolveProviderSession()) {
                is ProviderSessionResult.Active -> {
                    setContentView(R.layout.provider_manage_listings)
                    setupToolbar()
                    setupList(gate.userId)
                }
                ProviderSessionResult.RedirectStudent -> {
                    startActivity(Intent(this@ProviderManageListingsActivity, HomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                    finish()
                }
                ProviderSessionResult.RedirectLogin -> {
                    startActivity(
                        Intent(this@ProviderManageListingsActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupList(ownerId: String) {
        val rv = findViewById<RecyclerView>(R.id.rvProviderProperties)
        val empty = findViewById<View>(R.id.providerListingsEmpty)
        val pb = findViewById<ProgressBar>(R.id.pbManageListings)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = ProviderManagePropertiesAdapter(
            onEdit = { p ->
                startActivity(
                    Intent(this, ProviderEditPropertyActivity::class.java).apply {
                        putExtra(ProviderEditPropertyActivity.EXTRA_PROPERTY_ID, p.id)
                    }
                )
            },
            onDelete = { p -> confirmDelete(p) },
            onSetStatus = { p, status -> updateStatus(p, status) }
        )
        rv.adapter = adapter

        findViewById<MaterialButton>(R.id.btnEmptyAddProperty).setOnClickListener {
            startActivity(Intent(this, ProviderAddPropertyActivity::class.java))
        }

        lifecycleScope.launch {
            propertyRepository.observePropertiesByOwner(ownerId).collect { list ->
                pb.visibility = View.GONE
                adapter.submitList(list)
                val has = list.isNotEmpty()
                rv.visibility = if (has) View.VISIBLE else View.GONE
                empty.visibility = if (has) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateStatus(p: Property, status: PropertyStatus) {
        lifecycleScope.launch {
            runCatching {
                propertyRepository.updateAvailabilityStatus(p.id, status)
            }.onFailure {
                Toast.makeText(
                    this@ProviderManageListingsActivity,
                    it.message ?: "Could not update status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun confirmDelete(p: Property) {
        AlertDialog.Builder(this)
            .setTitle("Delete property")
            .setMessage("Remove \"${p.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    runCatching { propertyRepository.deleteProperty(p.id) }
                        .onSuccess {
                            Toast.makeText(this@ProviderManageListingsActivity, "Deleted", Toast.LENGTH_SHORT).show()
                        }
                        .onFailure {
                            Toast.makeText(
                                this@ProviderManageListingsActivity,
                                it.message ?: "Delete failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
