package com.fake.mzansilingo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HelpSupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        val headerTitle = findViewById<TextView>(R.id.tv_help_title)
        headerTitle.text = "HELP & SUPPORT"
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