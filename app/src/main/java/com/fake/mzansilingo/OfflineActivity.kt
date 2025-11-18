package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class OfflineActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnBottomDict: ImageView
    private lateinit var btnBottomQuotes: ImageView
    private lateinit var btnBottomStats: ImageView

    // Add references to text views that need updating
    private lateinit var tvOfflineTitle: TextView
    private lateinit var tvOfflineDescription: TextView
    private lateinit var tvOfflineDescriptionAfrikaans: TextView
    private lateinit var tvComingSoon: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline)

        drawerLayout = findViewById(R.id.drawer_layout)
        initializeViews()
        initializeBottomNavigation()
        setupClickListeners()

        // Update UI with proper strings
        updateUIStrings()

        // Make the card clickable to start quiz
        findViewById<CardView>(R.id.content_card).setOnClickListener {
            startOfflineQuiz()
        }
    }

    private fun initializeViews() {
        // Initialize text views
        tvOfflineTitle = findViewById(R.id.tv_offline_title)
        tvOfflineDescription = findViewById(R.id.tv_offline_description)
        tvOfflineDescriptionAfrikaans = findViewById(R.id.tv_offline_description_afrikaans)
        tvComingSoon = findViewById(R.id.tv_coming_soon)
    }

    private fun updateUIStrings() {
        // Update all text using string resources
        tvOfflineTitle.text = getString(R.string.offline_quiz_title)
        tvOfflineDescription.text = getString(R.string.offline_quiz_description)
        tvOfflineDescriptionAfrikaans.text = getString(R.string.offline_quiz_description_afrikaans)
        tvComingSoon.text = getString(R.string.start_quiz)  // ← Changed this line

        // Update navigation drawer items
        findViewById<TextView>(R.id.nav_home).text = getString(R.string.nav_home).uppercase()
        findViewById<TextView>(R.id.nav_language).text = getString(R.string.nav_language).uppercase()
        findViewById<TextView>(R.id.nav_words).text = getString(R.string.nav_words).uppercase()
        findViewById<TextView>(R.id.nav_phrases).text = getString(R.string.nav_phrases).uppercase()
        findViewById<TextView>(R.id.nav_progress).text = getString(R.string.nav_progress).uppercase()
        findViewById<TextView>(R.id.nav_settings).text = getString(R.string.nav_settings).uppercase()
        findViewById<TextView>(R.id.nav_profile).text = getString(R.string.nav_profile).uppercase()
    }

    private fun startOfflineQuiz() {
        val intent = Intent(this, OfflineQuizActivity::class.java)
        intent.putExtra("LANGUAGE", getIntent().getStringExtra("LANGUAGE") ?: "afrikaans")
        startActivity(intent)
    }

    private fun initializeBottomNavigation() {
        try {
            btnBottomDict = findViewById(R.id.btn_bottom_dict)
            btnBottomQuotes = findViewById(R.id.btn_bottom_quotes)
            btnBottomStats = findViewById(R.id.btn_bottom_stats)
            Log.d("OfflineActivity", "Bottom navigation initialized successfully")
        } catch (e: Exception) {
            Log.w("OfflineActivity", "Some bottom navigation elements not found: ${e.message}")
        }
    }

    private fun setupClickListeners() {
        val menuButton = findViewById<ImageView>(R.id.btn_menu)
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }

        setupNavigationDrawerListeners()
        setupBottomNavigationListeners()
    }

    private fun setupNavigationDrawerListeners() {
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToHome()
        }

        findViewById<TextView>(R.id.nav_language).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToLanguageSelection()
        }

        findViewById<TextView>(R.id.nav_words).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToWordsActivity()
        }

        findViewById<TextView>(R.id.nav_phrases).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToPhrasesActivity()
        }

        findViewById<TextView>(R.id.nav_progress).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProgressActivity()
        }

        findViewById<TextView>(R.id.nav_settings).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToSettings()
        }

        findViewById<TextView>(R.id.nav_profile).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProfile()
        }

        findViewById<ImageView>(R.id.nav_dictionary).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            Toast.makeText(this, getString(R.string.already_on_offline_quiz), Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.nav_back).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            finish()
        }

        findViewById<ImageView>(R.id.nav_chat).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }
    }

    private fun setupBottomNavigationListeners() {
        try {
            btnBottomDict.setOnClickListener {
                Toast.makeText(this, getString(R.string.already_on_offline_quiz), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.w("OfflineActivity", "Bottom dict button not found")
        }

        try {
            btnBottomQuotes.setOnClickListener {
                navigateToQuotes()
            }
        } catch (e: Exception) {
            Log.w("OfflineActivity", "Bottom quotes button not found")
        }

        try {
            btnBottomStats.setOnClickListener {
                navigateToStatistics()
            }
        } catch (e: Exception) {
            Log.w("OfflineActivity", "Bottom stats button not found")
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToLanguageSelection() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
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

    private fun navigateToQuotes() {
        Toast.makeText(this, getString(R.string.starting_quotes), Toast.LENGTH_SHORT).show()
        try {
            val intent = Intent(this, QuotesActivity::class.java)
            intent.putExtra("LANGUAGE", "afrikaans")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("OfflineActivity", "Navigation error", e)
        }
    }

    private fun navigateToStatistics() {
        val intent = Intent(this, ProgressActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}