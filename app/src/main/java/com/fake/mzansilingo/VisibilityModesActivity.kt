package com.fake.mzansilingo

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton

class VisibilityModesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnLightMode: MaterialButton
    private lateinit var btnDarkMode: MaterialButton
    private lateinit var btnMenu: ImageView

    // Navigation drawer items
    private lateinit var navHome: TextView
    private lateinit var navLanguage: TextView
    private lateinit var navWords: TextView
    private lateinit var navPhrases: TextView
    private lateinit var navProgress: TextView
    private lateinit var navVisibility: TextView
    private lateinit var navSettings: TextView
    private lateinit var navProfile: TextView
    private lateinit var navBack: ImageView
    private lateinit var navChat: ImageView
    private lateinit var navDictionary: ImageView

    companion object {
        private const val TAG = "VisibilityModesActivity"
        private const val PREFS_NAME = "ThemePrefs"
        private const val KEY_THEME_MODE = "theme_mode"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before inflating layout
        applySavedTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visibility_modes)

        initViews()
        setupClickListeners()
        updateButtonStates()
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM)

        when (savedTheme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnLightMode = findViewById(R.id.btn_light_mode)
        btnDarkMode = findViewById(R.id.btn_dark_mode)
        btnMenu = findViewById(R.id.btn_menu)

        // Initialize navigation drawer items
        navHome = findViewById(R.id.nav_home)
        navLanguage = findViewById(R.id.nav_language)
        navWords = findViewById(R.id.nav_words)
        navPhrases = findViewById(R.id.nav_phrases)
        navProgress = findViewById(R.id.nav_progress)
        navVisibility = findViewById(R.id.nav_visibility)
        navSettings = findViewById(R.id.nav_settings)
        navProfile = findViewById(R.id.nav_profile)
        navBack = findViewById(R.id.nav_back)
        navChat = findViewById(R.id.nav_chat)
        navDictionary = findViewById(R.id.nav_dictionary)
    }

    private fun setupClickListeners() {
        // Menu button - open/close drawer
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Light Mode button
        btnLightMode.setOnClickListener {
            setThemeMode(THEME_LIGHT)
        }

        // Dark Mode button
        btnDarkMode.setOnClickListener {
            setThemeMode(THEME_DARK)
        }

        // Navigation drawer items - matching other activities
        setupNavigation()
    }

    private fun setThemeMode(themeMode: String) {
        // Save preference
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, themeMode).apply()

        // Apply theme
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        // Show toast
        val toastMessage = when (themeMode) {
            THEME_LIGHT -> "Light mode activated"
            THEME_DARK -> "Dark mode activated"
            THEME_SYSTEM -> "System mode activated"
            else -> ""
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()

        // Recreate activity to apply theme immediately
        recreate()
    }

    private fun updateButtonStates() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentTheme = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // Reset both buttons first
        resetButtonState(btnLightMode)
        resetButtonState(btnDarkMode)

        // Highlight active one
        when {
            currentTheme == THEME_LIGHT ||
                    (currentTheme == THEME_SYSTEM && currentNightMode == Configuration.UI_MODE_NIGHT_NO) -> {
                highlightButton(btnLightMode, "Light Mode (Active)")
            }
            currentTheme == THEME_DARK ||
                    (currentTheme == THEME_SYSTEM && currentNightMode == Configuration.UI_MODE_NIGHT_YES) -> {
                highlightButton(btnDarkMode, "Dark Mode (Active)")
            }
        }
    }

    private fun resetButtonState(button: MaterialButton) {
        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.mz_accent_green)
        button.setTextColor(ContextCompat.getColor(this, R.color.mz_yellow))
        button.strokeColor = ContextCompat.getColorStateList(this, R.color.mz_white)

        when (button.id) {
            R.id.btn_light_mode -> button.text = "Light Mode"
            R.id.btn_dark_mode -> button.text = "Dark Mode"
        }
    }

    private fun highlightButton(button: MaterialButton, text: String) {
        button.text = text
        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.mz_yellow)
        button.setTextColor(ContextCompat.getColor(this, R.color.mz_navy_dark))
        button.strokeColor = ContextCompat.getColorStateList(this, R.color.mz_navy_dark)
    }

    private fun setupNavigation() {
        // Navigation drawer click listeners - matching other activities
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
            closeDrawer()
            navigateToPhrasesActivity()
        }

        navProgress.setOnClickListener {
            closeDrawer()
            navigateToProgressActivity()
        }

        navVisibility.setOnClickListener {
            // Already in Visibility activity, just close drawer
            closeDrawer()
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

    // Navigation methods matching other activities
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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }
}