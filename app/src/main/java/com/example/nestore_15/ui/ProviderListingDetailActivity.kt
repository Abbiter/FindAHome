package com.example.nestore_15.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.nestore_15.R
import com.google.android.material.imageview.ShapeableImageView

class ProviderListingDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.provider_listing_detail)
        setupBackNavigation()

        val image = findViewById<ShapeableImageView>(R.id.ivDetailImage)
        val title = findViewById<TextView>(R.id.tvDetailTitle)
        val location = findViewById<TextView>(R.id.tvDetailLocation)
        val price = findViewById<TextView>(R.id.tvDetailPrice)
        val status = findViewById<TextView>(R.id.tvDetailStatus)

        title.text = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        location.text = intent.getStringExtra(EXTRA_LOCATION).orEmpty()
        price.text = intent.getStringExtra(EXTRA_PRICE).orEmpty()
        status.text = intent.getStringExtra(EXTRA_STATUS).orEmpty()

        Glide.with(this)
            .load(intent.getStringExtra(EXTRA_IMAGE_URL))
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .centerCrop()
            .into(image)
    }

    private fun setupBackNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_LOCATION = "extra_location"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }
}
