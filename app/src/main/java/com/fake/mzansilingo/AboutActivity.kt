package com.fake.mzansilingo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        val headerTitle = findViewById<TextView>(R.id.tv_about_title)
        headerTitle.text = "ABOUT MZANSI LINGO"
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