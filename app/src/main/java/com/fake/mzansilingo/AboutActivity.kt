package com.fake.mzansilingo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class AboutActivity : BaseActivity() {  // Changed from AppCompatActivity to BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupViews()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the activity if language changed
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = prefs.getString("home_language", "English") ?: "English"

        // Check if the locale matches the saved language
        val currentLocale = resources.configuration.locales[0].language
        val expectedLocale = when (currentLanguage) {
            "English" -> "en"
            "isiZulu" -> "zu"
            else -> "en"
        }

        if (currentLocale != expectedLocale) {
            recreate() // Recreate activity to apply new language
        }
    }

    private fun setupViews() {
        val headerTitle = findViewById<TextView>(R.id.tv_about_title)
        // Use string resource so it changes with language
        headerTitle.text = getString(R.string.about_title)
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