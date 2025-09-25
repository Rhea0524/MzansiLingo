package com.fake.mzansilingo

import android.content.Intent
import android.content.SharedPreferences
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

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences

    // Views
    private lateinit var actvHomeLang: AutoCompleteTextView
    private lateinit var actvLearnLang: AutoCompleteTextView
    private lateinit var tilHomeLang: TextInputLayout
    private lateinit var tilLearnLang: TextInputLayout

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

    // Language data - Limited to 3 home languages for Part 2
    // NOTE: Home language editing and expanded options will be implemented in Part 3
    private val homeLanguages = listOf(
        "English", "Afrikaans", "isiZulu"
    )

    // Limited to 7 languages for learning as per requirements
    private val learnLanguages = listOf(
        "isiZulu", "isiXhosa", "Afrikaans", "English",
        "Sepedi", "Setswana", "Sesotho"
    )

    private var selectedHomeLanguage: String = ""
    private var selectedLearnLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        initializeViews()
        setupLanguageDropdowns()
        setupClickListeners()
        setupNavigationDrawer()
        loadSavedLanguages()
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        actvHomeLang = findViewById(R.id.actv_home_lang)
        actvLearnLang = findViewById(R.id.actv_learn_lang)
        tilHomeLang = findViewById(R.id.til_home_lang)
        tilLearnLang = findViewById(R.id.til_learn_lang)

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
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_save).setOnClickListener {
            saveLanguages()
        }
    }

    private fun setupLanguageDropdowns() {
        // Home language adapter - Limited to 3 options for Part 2
        // NOTE: Home language will become editable/searchable in Part 3
        val homeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, homeLanguages)
        actvHomeLang.setAdapter(homeAdapter)
        // Make home language non-editable for Part 2
        actvHomeLang.keyListener = null
        actvHomeLang.isFocusable = false

        // Learn language adapter - Limited to 7 options
        val learnAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, learnLanguages)
        actvLearnLang.setAdapter(learnAdapter)
    }

    private fun setupClickListeners() {
        // Home language selection listener
        actvHomeLang.setOnItemClickListener { _, _, position, _ ->
            selectedHomeLanguage = homeLanguages[position]
            clearErrors()
            performHapticFeedback()
        }

        // Learn language selection listener
        actvLearnLang.setOnItemClickListener { _, _, position, _ ->
            selectedLearnLanguage = learnLanguages[position]
            clearErrors()
            performHapticFeedback()
        }

        // Text change listeners to update selection when typed (learn language only)
        actvLearnLang.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = actvLearnLang.text.toString().trim()
                if (text.isNotEmpty() && learnLanguages.contains(text)) {
                    selectedLearnLanguage = text
                }
            }
        }
    }

    private fun setupNavigationDrawer() {
        // Navigation drawer click listeners - matching HomeActivity and PhrasesActivity implementation
        navHome.setOnClickListener {
            closeDrawer()
            navigateToHomeActivity()
        }

        navLanguage.setOnClickListener {
            // Already on language page, just close drawer
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

    // Navigation methods matching HomeActivity and PhrasesActivity
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

    private fun clearErrors() {
        tilHomeLang.error = null
        tilLearnLang.error = null
    }

    private fun performHapticFeedback() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    private fun loadSavedLanguages() {
        // Load previously saved languages
        selectedHomeLanguage = sharedPreferences.getString("home_language", "") ?: ""
        selectedLearnLanguage = sharedPreferences.getString("learn_language", "") ?: ""

        // Update UI based on saved selections
        if (selectedHomeLanguage.isNotEmpty() && homeLanguages.contains(selectedHomeLanguage)) {
            actvHomeLang.setText(selectedHomeLanguage, false)
        }

        if (selectedLearnLanguage.isNotEmpty()) {
            actvLearnLang.setText(selectedLearnLanguage, false)
        }
    }

    private fun validateLanguageSelection(): Boolean {
        var isValid = true

        // Get current text from UI
        val currentHomeLang = actvHomeLang.text.toString().trim()
        val currentLearnLang = actvLearnLang.text.toString().trim()

        // Validate home language
        if (currentHomeLang.isEmpty()) {
            tilHomeLang.error = "Please select your home language"
            isValid = false
        } else if (!homeLanguages.contains(currentHomeLang)) {
            tilHomeLang.error = "Please select from the available languages"
            isValid = false
        }

        // Validate learn language
        if (currentLearnLang.isEmpty()) {
            tilLearnLang.error = "Please select a language to learn"
            isValid = false
        } else if (!learnLanguages.contains(currentLearnLang)) {
            tilLearnLang.error = "Please select from the available languages"
            isValid = false
        }

        // Check if languages are the same
        if (isValid && currentHomeLang == currentLearnLang) {
            tilLearnLang.error = "Learn language must be different from home language"
            Toast.makeText(this, "Home and learn languages cannot be the same\nTuis- en leertale kan nie dieselfde wees nie", Toast.LENGTH_LONG).show()
            isValid = false
        }

        return isValid
    }

    private fun saveLanguages() {
        if (!validateLanguageSelection()) {
            return
        }

        // Save to SharedPreferences
        with(sharedPreferences.edit()) {
            putString("home_language", selectedHomeLanguage)
            putString("learn_language", selectedLearnLanguage)
            putBoolean("languages_selected", true)
            putLong("languages_updated_at", System.currentTimeMillis())
            apply()
        }

        Toast.makeText(this, "Languages saved successfully!\nTale suksesvol gestoor!", Toast.LENGTH_SHORT).show()

        // Return to previous activity with result
        val resultIntent = Intent().apply {
            putExtra("home_language", selectedHomeLanguage)
            putExtra("learn_language", selectedLearnLanguage)
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

        // Helper function to get language display name
        fun getLanguageDisplayName(language: String): String {
            return when (language) {
                "isiZulu" -> "isiZulu"
                "isiXhosa" -> "isiXhosa"
                "Afrikaans" -> "Afrikaans"
                "English" -> "English"
                "Sepedi" -> "Sepedi"
                "Setswana" -> "Setswana"
                "Sesotho" -> "Sesotho"
                else -> language
            }
        }

        // Helper function to get language code
        fun getLanguageCode(language: String): String {
            return when (language) {
                "English" -> "en"
                "Afrikaans" -> "af"
                "isiZulu" -> "zu"
                "isiXhosa" -> "xh"
                "Sepedi" -> "nso"
                "Setswana" -> "tn"
                "Sesotho" -> "st"
                else -> "en"
            }
        }

        // TODO: Part 3 Implementation
        // - Make home language editable/searchable
        // - Add language preference learning based on user selection
        // - Implement adaptive UI text based on selected home language
        // - Add voice recognition for language selection
        // - Add support for additional learn languages if needed
    }
}