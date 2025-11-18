package com.fake.mzansilingo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class PrivacyActivity : BaseActivity() {

    private lateinit var tvPrivacyTitle: TextView
    private lateinit var tvPrivacyPolicyTitle: TextView
    private lateinit var tvLastUpdated: TextView
    private lateinit var tvSection1Title: TextView
    private lateinit var tvSection1Content: TextView
    private lateinit var tvSection2Title: TextView
    private lateinit var tvSection2Content: TextView
    private lateinit var tvSection3Title: TextView
    private lateinit var tvSection3Content: TextView
    private lateinit var tvSection4Title: TextView
    private lateinit var tvSection4Content: TextView
    private lateinit var tvSection5Title: TextView
    private lateinit var tvSection5Content: TextView
    private lateinit var tvSection6Title: TextView
    private lateinit var tvSection6Content: TextView
    private lateinit var tvSection7Title: TextView
    private lateinit var tvSection7Content: TextView
    private lateinit var tvSection8Title: TextView
    private lateinit var tvSection8Content: TextView
    private lateinit var tvSection9Title: TextView
    private lateinit var tvSection9Content: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        initializeViews()
        setupUI()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvPrivacyTitle = findViewById(R.id.tv_privacy_title)
        tvPrivacyPolicyTitle = findViewById(R.id.tv_privacy_policy_title)
        tvLastUpdated = findViewById(R.id.tv_last_updated)

        tvSection1Title = findViewById(R.id.tv_section1_title)
        tvSection1Content = findViewById(R.id.tv_section1_content)

        tvSection2Title = findViewById(R.id.tv_section2_title)
        tvSection2Content = findViewById(R.id.tv_section2_content)

        tvSection3Title = findViewById(R.id.tv_section3_title)
        tvSection3Content = findViewById(R.id.tv_section3_content)

        tvSection4Title = findViewById(R.id.tv_section4_title)
        tvSection4Content = findViewById(R.id.tv_section4_content)

        tvSection5Title = findViewById(R.id.tv_section5_title)
        tvSection5Content = findViewById(R.id.tv_section5_content)

        tvSection6Title = findViewById(R.id.tv_section6_title)
        tvSection6Content = findViewById(R.id.tv_section6_content)

        tvSection7Title = findViewById(R.id.tv_section7_title)
        tvSection7Content = findViewById(R.id.tv_section7_content)

        tvSection8Title = findViewById(R.id.tv_section8_title)
        tvSection8Content = findViewById(R.id.tv_section8_content)

        tvSection9Title = findViewById(R.id.tv_section9_title)
        tvSection9Content = findViewById(R.id.tv_section9_content)
    }

    private fun setupUI() {
        // Set all text using string resources
        tvPrivacyTitle.text = getString(R.string.privacy_security_title)
        tvPrivacyPolicyTitle.text = getString(R.string.privacy_policy_title)
        tvLastUpdated.text = getString(R.string.privacy_last_updated)

        tvSection1Title.text = getString(R.string.privacy_section1_title)
        tvSection1Content.text = getString(R.string.privacy_section1_content)

        tvSection2Title.text = getString(R.string.privacy_section2_title)
        tvSection2Content.text = getString(R.string.privacy_section2_content)

        tvSection3Title.text = getString(R.string.privacy_section3_title)
        tvSection3Content.text = getString(R.string.privacy_section3_content)

        tvSection4Title.text = getString(R.string.privacy_section4_title)
        tvSection4Content.text = getString(R.string.privacy_section4_content)

        tvSection5Title.text = getString(R.string.privacy_section5_title)
        tvSection5Content.text = getString(R.string.privacy_section5_content)

        tvSection6Title.text = getString(R.string.privacy_section6_title)
        tvSection6Content.text = getString(R.string.privacy_section6_content)

        tvSection7Title.text = getString(R.string.privacy_section7_title)
        tvSection7Content.text = getString(R.string.privacy_section7_content)

        tvSection8Title.text = getString(R.string.privacy_section8_title)
        tvSection8Content.text = getString(R.string.privacy_section8_content)

        tvSection9Title.text = getString(R.string.privacy_section9_title)
        tvSection9Content.text = getString(R.string.privacy_section9_content)
    }

    private fun setupClickListeners() {
        // Back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the activity if language changed
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = prefs.getString("home_language", "English") ?: "English"

        val currentLocale = resources.configuration.locales[0].language
        val expectedLocale = when (currentLanguage) {
            "English" -> "en"
            "isiZulu" -> "zu"
            else -> "en"
        }

        if (currentLocale != expectedLocale) {
            recreate()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}