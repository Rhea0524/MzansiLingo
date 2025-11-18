package com.fake.mzansilingo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class HelpSupportActivity : BaseActivity() {

    // View references
    private lateinit var btnBack: ImageView
    private lateinit var tvHelpTitle: TextView
    private lateinit var tvHelpSubtitle: TextView

    // Section titles
    private lateinit var tvGettingStartedTitle: TextView
    private lateinit var tvAccountTitle: TextView
    private lateinit var tvLearningTitle: TextView
    private lateinit var tvTechnicalTitle: TextView
    private lateinit var tvSubscriptionTitle: TextView
    private lateinit var tvProgressTitle: TextView
    private lateinit var tvOfflineTitle: TextView
    private lateinit var tvFaqTitle: TextView
    private lateinit var tvContactTitle: TextView

    // Section content
    private lateinit var tvGettingStartedText: TextView
    private lateinit var tvAccountText: TextView
    private lateinit var tvLearningText: TextView
    private lateinit var tvTechnicalText: TextView
    private lateinit var tvSubscriptionText: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var tvOfflineText: TextView
    private lateinit var tvFaqText: TextView
    private lateinit var tvContactText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        initializeViews()
        setupUI()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        tvHelpTitle = findViewById(R.id.tv_help_title)

        // We'll need to add IDs to the XML for the subtitle and all sections
        // For now, find them by their position in the layout
        // You'll need to add android:id attributes to each TextView in the XML
    }

    private fun setupUI() {
        // Set all text using string resources
        tvHelpTitle.text = getString(R.string.help_support_title)

        // Note: You'll need to add android:id attributes to all TextViews in your XML
        // Then uncomment and use these:
        /*
        tvHelpSubtitle.text = getString(R.string.help_support_subtitle)

        tvGettingStartedTitle.text = getString(R.string.help_getting_started_title)
        tvGettingStartedText.text = getString(R.string.help_getting_started_text)

        tvAccountTitle.text = getString(R.string.help_account_title)
        tvAccountText.text = getString(R.string.help_account_text)

        tvLearningTitle.text = getString(R.string.help_learning_title)
        tvLearningText.text = getString(R.string.help_learning_text)

        tvTechnicalTitle.text = getString(R.string.help_technical_title)
        tvTechnicalText.text = getString(R.string.help_technical_text)

        tvSubscriptionTitle.text = getString(R.string.help_subscription_title)
        tvSubscriptionText.text = getString(R.string.help_subscription_text)

        tvProgressTitle.text = getString(R.string.help_progress_title)
        tvProgressText.text = getString(R.string.help_progress_text)

        tvOfflineTitle.text = getString(R.string.help_offline_title)
        tvOfflineText.text = getString(R.string.help_offline_text)

        tvFaqTitle.text = getString(R.string.help_faq_title)
        tvFaqText.text = getString(R.string.help_faq_text)

        tvContactTitle.text = getString(R.string.help_contact_title)
        tvContactText.text = getString(R.string.help_contact_text)
        */
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI when language changes
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = prefs.getString("home_language", "English") ?: "English"

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

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}