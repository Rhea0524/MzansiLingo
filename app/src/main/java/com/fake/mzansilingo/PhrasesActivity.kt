package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class PhrasesActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView

    // Category cards
    private lateinit var cardCommunication: CardView
    private lateinit var cardPolite: CardView
    private lateinit var cardPersonal: CardView
    private lateinit var cardTravel: CardView

    // Category TextViews
    private lateinit var tvCommunicationEn: TextView
    private lateinit var tvCommunicationAf: TextView
    private lateinit var tvPoliteEn: TextView
    private lateinit var tvPoliteAf: TextView
    private lateinit var tvPersonalEn: TextView
    private lateinit var tvPersonalAf: TextView
    private lateinit var tvTravelEn: TextView
    private lateinit var tvTravelAf: TextView

    // Title
    private lateinit var tvPhrasesTitle: TextView

    // Navigation drawer items
    private lateinit var navHome: TextView
    private lateinit var navLanguage: TextView
    private lateinit var navWords: TextView
    private lateinit var navPhrases: TextView
    private lateinit var navProgress: TextView
    private lateinit var navSettings: TextView
    private lateinit var navProfile: TextView
    private lateinit var navBack: ImageView
    private lateinit var navChat: ImageView
    private lateinit var navDictionary: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phrases)

        initializeViews()
        setupUI()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Initialize drawer and menu
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)

        // Initialize category cards
        cardCommunication = findViewById(R.id.card_communication)
        cardPolite = findViewById(R.id.card_polite)
        cardPersonal = findViewById(R.id.card_personal)
        cardTravel = findViewById(R.id.card_travel)

        // Initialize category TextViews
        tvCommunicationEn = findViewById(R.id.tv_communication_en)
        tvCommunicationAf = findViewById(R.id.tv_communication_af)
        tvPoliteEn = findViewById(R.id.tv_polite_en)
        tvPoliteAf = findViewById(R.id.tv_polite_af)
        tvPersonalEn = findViewById(R.id.tv_personal_en)
        tvPersonalAf = findViewById(R.id.tv_personal_af)
        tvTravelEn = findViewById(R.id.tv_travel_en)
        tvTravelAf = findViewById(R.id.tv_travel_af)

        // Initialize title
        tvPhrasesTitle = findViewById(R.id.tv_phrases_title)

        // Initialize navigation drawer items
        navHome = findViewById(R.id.nav_home)
        navLanguage = findViewById(R.id.nav_language)
        navWords = findViewById(R.id.nav_words)
        navPhrases = findViewById(R.id.nav_phrases)
        navProgress = findViewById(R.id.nav_progress)
        navSettings = findViewById(R.id.nav_settings)
        navProfile = findViewById(R.id.nav_profile)
        navBack = findViewById(R.id.nav_back)
        navChat = findViewById(R.id.nav_chat)
        navDictionary = findViewById(R.id.nav_dictionary)
    }

    private fun setupUI() {
        // Set title using string resources
        tvPhrasesTitle.text = getString(R.string.phrases_title)

        // Set category texts - English/isiZulu text changes, Afrikaans stays same
        // Communication & Information
        tvCommunicationEn.text = getString(R.string.category_communication_en)
        tvCommunicationAf.text = getString(R.string.category_communication_af)

        // Polite and Essential
        tvPoliteEn.text = getString(R.string.category_polite_en)
        tvPoliteAf.text = getString(R.string.category_polite_af)

        // Personal Information
        tvPersonalEn.text = getString(R.string.category_personal_en)
        tvPersonalAf.text = getString(R.string.category_personal_af)

        // Travel & Daily Needs
        tvTravelEn.text = getString(R.string.category_travel_en)
        tvTravelAf.text = getString(R.string.category_travel_af)

        // Set navigation drawer texts using string resources
        navHome.text = getString(R.string.nav_home).uppercase()
        navLanguage.text = getString(R.string.nav_language).uppercase()
        navWords.text = getString(R.string.nav_words).uppercase()
        navPhrases.text = getString(R.string.nav_phrases).uppercase()
        navProgress.text = getString(R.string.nav_progress).uppercase()
        navSettings.text = getString(R.string.nav_settings).uppercase()
        navProfile.text = getString(R.string.nav_profile).uppercase()
    }

    private fun setupClickListeners() {
        // Menu button to open drawer
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Category card click listeners
        cardCommunication.setOnClickListener {
            openCategoryActivity("Communication")
        }

        cardPolite.setOnClickListener {
            openCategoryActivity("Polite")
        }

        cardPersonal.setOnClickListener {
            openCategoryActivity("Personal")
        }

        cardTravel.setOnClickListener {
            openCategoryActivity("Travel")
        }

        // Navigation drawer click listeners
        navHome.setOnClickListener {
            closeDrawer()
            navigateToHomeActivity()
        }

        navLanguage.setOnClickListener {
            closeDrawer()
            navigateToLanguageSelection()
        }

        navWords.setOnClickListener {
            closeDrawer()
            navigateToWordsActivity()
        }

        navPhrases.setOnClickListener {
            // Already in Phrases activity, just close drawer
            closeDrawer()
        }

        navProgress.setOnClickListener {
            closeDrawer()
            navigateToProgressActivity()
        }

        navSettings.setOnClickListener {
            closeDrawer()
            navigateToSettings()
        }

        navProfile.setOnClickListener {
            closeDrawer()
            navigateToProfile()
        }

        navBack.setOnClickListener {
            closeDrawer()
            onBackPressed()
        }

        navChat.setOnClickListener {
            closeDrawer()
            navigateToAiChatActivity()
        }

        navDictionary.setOnClickListener {
            closeDrawer()
            navigateToOfflineQuiz()
        }
    }

    private fun openCategoryActivity(category: String) {
        val intent = Intent(this, CategoryPhrasesActivity::class.java)
        intent.putExtra("CATEGORY", category)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    // Navigation methods
    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToWordsActivity() {
        val intent = Intent(this, WordsActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToProgressActivity() {
        val intent = Intent(this, ProgressActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToAiChatActivity() {
        val intent = Intent(this, AiChatActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToLanguageSelection() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToOfflineQuiz() {
        val intent = Intent(this, OfflineActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}