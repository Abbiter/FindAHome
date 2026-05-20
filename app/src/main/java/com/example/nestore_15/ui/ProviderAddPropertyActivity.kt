package com.example.nestore_15.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.nestore_15.R
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.session.ProviderSessionResult
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.data.session.resolveProviderSession
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri

class ProviderAddPropertyActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val propertyRepository = PropertyRepository()
    private val selectedUris = mutableListOf<Uri>()

    private val pickImages = registerForActivityResult(PickMultipleVisualMedia(20)) { uris ->
        selectedUris.clear()
        selectedUris.addAll(uris)
        updateImageCountLabel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            when (val gate = sessionManager.resolveProviderSession()) {
                is ProviderSessionResult.Active -> {
                    setContentView(R.layout.provider_property_form)
                    setupUi(gate.userId)
                }
                ProviderSessionResult.RedirectStudent -> {
                    startActivity(
                        Intent(this@ProviderAddPropertyActivity, HomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                    )
                    finish()
                }
                ProviderSessionResult.RedirectLogin -> {
                    startActivity(
                        Intent(this@ProviderAddPropertyActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )
                    finish()
                }
            }
        }
    }

    private fun setupUi(ownerId: String) {
        findViewById<TextView>(R.id.tvPropertyFormHeading).text = "Add New Property"
        findViewById<TextView>(R.id.tvExistingImageSummary).visibility = View.GONE

        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val sp = findViewById<Spinner>(R.id.spPropertyStatus)
        val labels = PropertyStatus.values().map { it.name }
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        sp.setSelection(0, false)

        findViewById<MaterialButton>(R.id.btnPickPropertyImages).setOnClickListener {
            pickImages.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        findViewById<MaterialButton>(R.id.btnSaveProperty).setOnClickListener {
            saveProperty(ownerId)
        }

        updateImageCountLabel()
    }

    private fun updateImageCountLabel() {
        val count = selectedUris.size
        findViewById<TextView>(R.id.tvSelectedImageCount).text = when (count) {
            0 -> "No photos selected — a default listing image will be used"
            1 -> "1 photo selected — saved with a catalog listing image"
            else -> "$count photos selected — saved with catalog listing images"
        }
    }

    private fun readStatus(sp: Spinner): PropertyStatus {
        val idx = if (sp.selectedItemPosition < 0) 0 else sp.selectedItemPosition
        return PropertyStatus.values().getOrElse(idx) { PropertyStatus.AVAILABLE }
    }

    private fun saveProperty(ownerId: String) {
        val title = findViewById<EditText>(R.id.etPropertyTitle).text.toString().trim()
        val description = findViewById<EditText>(R.id.etPropertyDescription).text.toString().trim()
        val location = findViewById<EditText>(R.id.etPropertyLocation).text.toString().trim()
        val priceRaw = findViewById<EditText>(R.id.etPropertyPrice).text.toString().replace(",", ".").trim()
        val roomsRaw = findViewById<EditText>(R.id.etPropertyRooms).text.toString().trim()
        val date = findViewById<EditText>(R.id.etPropertyAvailabilityDate).text.toString().trim()
        val status = readStatus(findViewById(R.id.spPropertyStatus))

        if (title.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Title and location are required", Toast.LENGTH_SHORT).show()
            return
        }
        val price = priceRaw.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }
        val rooms = roomsRaw.toIntOrNull() ?: 0
        if (rooms <= 0) {
            Toast.makeText(this, "Enter number of rooms", Toast.LENGTH_SHORT).show()
            return
        }

        val pb = findViewById<ProgressBar>(R.id.pbPropertySave)
        val btn = findViewById<MaterialButton>(R.id.btnSaveProperty)
        pb.visibility = View.VISIBLE
        btn.isEnabled = false

        lifecycleScope.launch {
            runCatching {
                propertyRepository.createProperty(
                    ownerId = ownerId,
                    title = title,
                    description = description,
                    location = location,
                    priceBwp = price,
                    roomCount = rooms,
                    availabilityStatus = status,
                    availabilityDate = date,
                    imageUris = selectedUris.toList()
                )
            }.onSuccess {
                Toast.makeText(this@ProviderAddPropertyActivity, "Property saved", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Toast.makeText(
                    this@ProviderAddPropertyActivity,
                    e.message ?: "Could not save property",
                    Toast.LENGTH_LONG
                ).show()
            }
            pb.visibility = View.GONE
            btn.isEnabled = true
        }
    }
}
