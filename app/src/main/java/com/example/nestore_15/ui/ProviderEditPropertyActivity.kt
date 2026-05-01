package com.example.nestore_15.ui

import android.content.Intent
import android.net.Uri
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
import com.example.nestore_15.data.model.Property
import com.example.nestore_15.data.model.PropertyStatus
import com.example.nestore_15.data.repository.PropertyRepository
import com.example.nestore_15.data.session.ProviderSessionResult
import com.example.nestore_15.data.session.SessionManager
import com.example.nestore_15.data.session.resolveProviderSession
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ProviderEditPropertyActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val propertyRepository = PropertyRepository()
    private val newImageUris = mutableListOf<Uri>()
    private var loadedProperty: Property? = null

    private val pickImages = registerForActivityResult(PickMultipleVisualMedia(20)) { uris ->
        newImageUris.clear()
        newImageUris.addAll(uris)
        updateImageLabels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val propertyId = intent.getStringExtra(EXTRA_PROPERTY_ID).orEmpty()
        if (propertyId.isEmpty()) {
            Toast.makeText(this, "Missing property", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            when (val gate = sessionManager.resolveProviderSession()) {
                is ProviderSessionResult.Active -> {
                    setContentView(R.layout.provider_property_form)
                    setupToolbar()
                    populateOrLoad(gate.userId, propertyId)
                }
                ProviderSessionResult.RedirectStudent -> {
                    startActivity(Intent(this@ProviderEditPropertyActivity, HomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                    finish()
                }
                ProviderSessionResult.RedirectLogin -> {
                    startActivity(
                        Intent(this@ProviderEditPropertyActivity, LoginActivity::class.java).apply {
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

    private fun populateOrLoad(ownerId: String, propertyId: String) {
        findViewById<TextView>(R.id.tvPropertyFormHeading).text = "Edit Property"
        val sp = findViewById<Spinner>(R.id.spPropertyStatus)
        val labels = PropertyStatus.values().map { it.name }
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)

        findViewById<MaterialButton>(R.id.btnPickPropertyImages).setOnClickListener {
            pickImages.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        val pb = findViewById<ProgressBar>(R.id.pbPropertySave)
        pb.visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnSaveProperty).isEnabled = false

        lifecycleScope.launch {
            val prop = propertyRepository.getProperty(propertyId)
            pb.visibility = View.GONE
            findViewById<MaterialButton>(R.id.btnSaveProperty).isEnabled = true

            if (prop == null || prop.ownerId != ownerId) {
                Toast.makeText(this@ProviderEditPropertyActivity, "Unable to load property", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            loadedProperty = prop
            bind(prop)
            findViewById<MaterialButton>(R.id.btnSaveProperty).setOnClickListener {
                saveChanges(ownerId, prop)
            }
        }
    }

    private fun bind(p: Property) {
        findViewById<EditText>(R.id.etPropertyTitle).setText(p.title)
        findViewById<EditText>(R.id.etPropertyDescription).setText(p.description)
        findViewById<EditText>(R.id.etPropertyLocation).setText(p.location)
        findViewById<EditText>(R.id.etPropertyPrice).setText(
            if (p.priceBwp % 1.0 == 0.0) p.priceBwp.toInt().toString() else p.priceBwp.toString()
        )
        findViewById<EditText>(R.id.etPropertyRooms).setText(p.roomCount.toString())
        findViewById<EditText>(R.id.etPropertyAvailabilityDate).setText(p.availabilityDate)

        val sp = findViewById<Spinner>(R.id.spPropertyStatus)
        sp.setSelection(PropertyStatus.values().indexOf(p.availabilityStatus).coerceAtLeast(0), false)

        val summary = findViewById<TextView>(R.id.tvExistingImageSummary)
        summary.visibility = View.VISIBLE
        summary.text = "${p.imageUrls.size} existing photos on listing"

        updateImageLabels()
    }

    private fun updateImageLabels() {
        findViewById<TextView>(R.id.tvSelectedImageCount).text =
            "${newImageUris.size} new photos selected"
    }

    private fun readStatus(sp: Spinner): PropertyStatus {
        val idx = if (sp.selectedItemPosition < 0) 0 else sp.selectedItemPosition
        return PropertyStatus.values().getOrElse(idx) { PropertyStatus.AVAILABLE }
    }

    private fun saveChanges(ownerId: String, original: Property) {
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
                propertyRepository.updateProperty(
                    propertyId = original.id,
                    ownerId = ownerId,
                    title = title,
                    description = description,
                    location = location,
                    priceBwp = price,
                    roomCount = rooms,
                    availabilityStatus = status,
                    availabilityDate = date,
                    existingImageUrls = original.imageUrls,
                    newImageUris = newImageUris.toList()
                )
            }.onSuccess {
                Toast.makeText(this@ProviderEditPropertyActivity, "Property updated", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Toast.makeText(
                    this@ProviderEditPropertyActivity,
                    e.message ?: "Could not update property",
                    Toast.LENGTH_LONG
                ).show()
            }
            pb.visibility = View.GONE
            btn.isEnabled = true
        }
    }

    companion object {
        const val EXTRA_PROPERTY_ID = "extra_property_id"
    }
}
