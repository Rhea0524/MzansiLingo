package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class NotificationActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        initViews()
        setupClickListeners()
        setupNavigationDrawer()
        setupBottomNavigation()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)
        tvTitle = findViewById(R.id.tv_title)
        tvMessage = findViewById(R.id.tv_message)

        tvTitle.text = "NOTIFICATIONS"
        tvMessage.text = "To be implemented in part 3"
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }

    private fun setupNavigationDrawer() {
        // Home navigation
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToHome()
        }

        // Language navigation
        findViewById<TextView>(R.id.nav_language).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToLanguageSelection()
        }

        // Words navigation
        findViewById<TextView>(R.id.nav_words).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToWordsActivity()
        }

        // Phrases navigation
        findViewById<TextView>(R.id.nav_phrases).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToPhrasesActivity()
        }

        // Progress navigation
        findViewById<TextView>(R.id.nav_progress).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProgressActivity()
        }

        // Visibility modes navigation
        findViewById<TextView>(R.id.nav_visibility).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToVisibilityModes()
        }

        // Settings navigation
        findViewById<TextView>(R.id.nav_settings).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToSettings()
        }

        // Profile navigation
        findViewById<TextView>(R.id.nav_profile).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProfile()
        }

        // Navigation drawer bottom icons
        findViewById<ImageView>(R.id.nav_dictionary).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToOfflineQuiz()
        }

        findViewById<ImageView>(R.id.nav_back).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            onBackPressed()
        }

        findViewById<ImageView>(R.id.nav_chat).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }
    }

    private fun setupBottomNavigation() {
        // Bottom book/dictionary button - navigate to OfflineActivity
        findViewById<ImageView>(R.id.btn_bottom_dict)?.setOnClickListener {
            navigateToOfflineQuiz()
        }

        // Bottom quotes button - navigate to QuotesActivity
        findViewById<ImageView>(R.id.btn_bottom_quotes)?.setOnClickListener {
            navigateToQuotes()
        }

        // Bottom bars/statistics button - navigate to ProgressActivity
        findViewById<ImageView>(R.id.btn_bottom_stats)?.setOnClickListener {
            navigateToStatistics()
        }
    }

    // Navigation methods matching HomeActivity functionality
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
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

    private fun navigateToVisibilityModes() {
        val intent = Intent(this, VisibilityModesActivity::class.java)
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

    private fun navigateToAiChatActivity() {
        val intent = Intent(this, AiChatActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToOfflineQuiz() {
        val intent = Intent(this, OfflineActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToQuotes() {
        Toast.makeText(this, "Starting QuotesActivity...", Toast.LENGTH_SHORT).show()

        try {
            val intent = Intent(this, QuotesActivity::class.java)
            intent.putExtra("LANGUAGE", "afrikaans")
            startActivity(intent)
            Toast.makeText(this, "Intent sent successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("NotificationActivity", "Navigation error", e)
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