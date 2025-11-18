package com.fake.mzansilingo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences

    // Views
    private lateinit var actvHomeLang: AutoCompleteTextView
    private lateinit var tilHomeLang: TextInputLayout
    private lateinit var tvLanguageHeader: TextView
    private lateinit var tvHomeLangTitle: TextView
    private lateinit var tvHomeLangSubtitle: TextView
    private lateinit var tvLearnLangTitle: TextView
    private lateinit var btnSave: androidx.appcompat.widget.AppCompatButton

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

    // Language data - Choice between English and isiZulu only
    private val homeLanguages = listOf(
        "English", "isiZulu"
    )

    // Fixed learn language - Afrikaans only
    private val learnLanguage = "Afrikaans"

    private var selectedHomeLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language before setting content view
        applySavedLanguage()

        setContentView(R.layout.activity_language_selection)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        initializeViews()
        setupLanguageDropdown()
        setupClickListeners()
        setupNavigationDrawer()
        loadSavedLanguage()
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("home_language", "English") ?: "English"
        Log.d("LanguageSelection", "attachBaseContext - Saved language: $savedLanguage")
        super.attachBaseContext(updateLocale(newBase, savedLanguage))
    }

    private fun updateLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "English" -> Locale("en")
            "isiZulu" -> Locale("zu")
            else -> Locale("en")
        }

        Log.d("LanguageSelection", "updateLocale - Setting locale to: ${locale.language} for language: $language")

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        actvHomeLang = findViewById(R.id.actv_home_lang)
        tilHomeLang = findViewById(R.id.til_home_lang)
        tvLanguageHeader = findViewById(R.id.tv_language_header)
        tvHomeLangTitle = findViewById(R.id.tv_home_lang_title)
        tvHomeLangSubtitle = findViewById(R.id.tv_home_lang_subtitle)
        tvLearnLangTitle = findViewById(R.id.tv_learn_lang_title)
        btnSave = findViewById(R.id.btn_save)

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

        // Back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // Menu button
        findViewById<ImageView>(R.id.btn_menu).setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Save button
        btnSave.setOnClickListener {
            saveLanguage()
        }
    }

    private fun setupLanguageDropdown() {
        // Home language adapter - Choice between English and isiZulu
        val homeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, homeLanguages)
        actvHomeLang.setAdapter(homeAdapter)
        actvHomeLang.keyListener = null
        actvHomeLang.isFocusable = true
    }

    private fun setupClickListeners() {
        // Home language selection listener
        actvHomeLang.setOnItemClickListener { _, _, position, _ ->
            selectedHomeLanguage = homeLanguages[position]
            clearError()
            performHapticFeedback()

            // Save and immediately apply language change
            with(sharedPreferences.edit()) {
                putString("home_language", selectedHomeLanguage)
                putString("learn_language", learnLanguage)
                putBoolean("languages_selected", true)
                apply()
            }

            // Recreate the activity to apply the language change
            recreate()
        }
    }

    private fun setupNavigationDrawer() {
        // Navigation drawer click listeners
        navHome.setOnClickListener {
            closeDrawer()
            navigateToHomeActivity()
        }

        navLanguage.setOnClickListener {
            closeDrawer()
        }

        navWords.setOnClickListener {
            closeDrawer()
            navigateToWordsActivity()
        }

        navPhrases.setOnClickListener {
            closeDrawer()
            navigateToPhrasesActivity()
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

    private fun applySavedLanguage() {
        // This method is kept for compatibility but the real work is done in attachBaseContext
    }

    private fun applyLanguageChange(language: String) {
        // Recreate activity to apply language changes
        recreate()
    }

    private fun setAppLocale(language: String) {
        // This method is no longer needed since locale is set in attachBaseContext
        // Kept for potential future use
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

    private fun navigateToPhrasesActivity() {
        val intent = Intent(this, PhrasesActivity::class.java)
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

    private fun clearError() {
        tilHomeLang.error = null
    }

    private fun performHapticFeedback() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    private fun loadSavedLanguage() {
        selectedHomeLanguage = sharedPreferences.getString("home_language", "English") ?: "English"

        if (selectedHomeLanguage.isNotEmpty() && homeLanguages.contains(selectedHomeLanguage)) {
            actvHomeLang.setText(selectedHomeLanguage, false)
        } else if (selectedHomeLanguage.isEmpty()) {
            // Set default to English if nothing is saved
            selectedHomeLanguage = "English"
            actvHomeLang.setText(selectedHomeLanguage, false)
        }
    }

    private fun validateLanguageSelection(): Boolean {
        val currentHomeLang = actvHomeLang.text.toString().trim()

        if (currentHomeLang.isEmpty()) {
            tilHomeLang.error = getString(R.string.error_select_home_language)
            return false
        } else if (!homeLanguages.contains(currentHomeLang)) {
            tilHomeLang.error = getString(R.string.error_select_available_language)
            return false
        }

        return true
    }

    private fun saveLanguage() {
        if (!validateLanguageSelection()) {
            return
        }

        with(sharedPreferences.edit()) {
            putString("home_language", selectedHomeLanguage)
            putString("learn_language", learnLanguage)
            putBoolean("languages_selected", true)
            putLong("languages_updated_at", System.currentTimeMillis())
            apply()
        }

        Toast.makeText(this, getString(R.string.language_saved_success), Toast.LENGTH_SHORT).show()

        val resultIntent = Intent().apply {
            putExtra("home_language", selectedHomeLanguage)
            putExtra("learn_language", learnLanguage)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val REQUEST_CODE_LANGUAGE_SELECTION = 100

        fun getLanguageDisplayName(language: String): String {
            return when (language) {
                "isiZulu" -> "isiZulu"
                "Afrikaans" -> "Afrikaans"
                "English" -> "English"
                else -> language
            }
        }

        fun getLanguageCode(language: String): String {
            return when (language) {
                "English" -> "en"
                "Afrikaans" -> "af"
                "isiZulu" -> "zu"
                else -> "en"
            }
        }
    }
}