package com.fake.mzansilingo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        val headerTitle = findViewById<TextView>(R.id.tv_privacy_title)
        headerTitle.text = "PRIVACY & SECURITY"
    }

    private fun setupClickListeners() {
        // Back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}