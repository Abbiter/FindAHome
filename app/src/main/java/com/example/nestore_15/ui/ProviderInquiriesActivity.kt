package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Inquiry
import com.example.nestore_15.data.model.InquiryThreadStatus
import com.example.nestore_15.data.repository.InquiryRepository
import com.example.nestore_15.data.session.ProviderSessionResult
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.data.session.resolveProviderSession
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProviderInquiriesActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val inquiryRepository = InquiryRepository()
    private lateinit var adapter: ProviderInquiriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            when (val gate = sessionManager.resolveProviderSession()) {
                is ProviderSessionResult.Active -> {
                    setContentView(R.layout.provider_inquiries)
                    setupToolbar()
                    setupList(gate.userId)
                }
                ProviderSessionResult.RedirectStudent -> {
                    startActivity(Intent(this@ProviderInquiriesActivity, HomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                    finish()
                }
                ProviderSessionResult.RedirectLogin -> {
                    startActivity(
                        Intent(this@ProviderInquiriesActivity, LoginActivity::class.java).apply {
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

    private fun setupList(providerId: String) {
        val rv = findViewById<RecyclerView>(R.id.rvProviderInquiries)
        val empty = findViewById<View>(R.id.providerInquiriesEmpty)
        val pb = findViewById<ProgressBar>(R.id.pbInquiries)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = ProviderInquiriesAdapter(
            onMarkPending = { updateStatus(it, InquiryThreadStatus.PENDING) },
            onMarkResponded = { updateStatus(it, InquiryThreadStatus.RESPONDED) }
        )
        rv.adapter = adapter

        lifecycleScope.launch {
            inquiryRepository.observeInquiriesForProvider(providerId).collect { list ->
                pb.visibility = View.GONE
                adapter.submitList(list)
                val has = list.isNotEmpty()
                rv.visibility = if (has) View.VISIBLE else View.GONE
                empty.visibility = if (has) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateStatus(inquiry: Inquiry, status: InquiryThreadStatus) {
        lifecycleScope.launch {
            runCatching {
                inquiryRepository.updateInquiryStatus(inquiry.id, status)
            }.onFailure {
                Toast.makeText(
                    this@ProviderInquiriesActivity,
                    it.message ?: "Could not update inquiry",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
