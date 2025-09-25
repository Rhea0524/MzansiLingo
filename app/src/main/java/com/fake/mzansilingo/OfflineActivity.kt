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



class OfflineActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // Bottom navigation elements
    private lateinit var btnBottomDict: ImageView
    private lateinit var btnBottomQuotes: ImageView
    private lateinit var btnBottomStats: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline)

        // Initialize drawer layout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize bottom navigation
        initializeBottomNavigation()

        // Setup click listeners
        setupClickListeners()
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
        // Menu button to open drawer
        val menuButton = findViewById<ImageView>(R.id.btn_menu)
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Back button
        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }

        // Setup navigation drawer listeners
        setupNavigationDrawerListeners()

        // Setup bottom navigation listeners
        setupBottomNavigationListeners()
    }

    private fun setupNavigationDrawerListeners() {
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

        // Quotes navigation (if present in drawer)
        try {
            findViewById<TextView>(R.id.nav_quotes).setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                navigateToQuotes()
            }
        } catch (e: Exception) {
            Log.d("OfflineActivity", "nav_quotes not found in drawer")
        }

        // Progress navigation
        findViewById<TextView>(R.id.nav_progress).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProgressActivity()
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
            // Already on offline quiz screen - just close drawer
            Toast.makeText(this, "Already on Offline Quiz screen", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.nav_back).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            finish() // Go back to previous screen
        }

        findViewById<ImageView>(R.id.nav_chat).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }
    }

    private fun setupBottomNavigationListeners() {
        // Bottom book/dictionary button - already on offline activity
        try {
            btnBottomDict.setOnClickListener {
                // Already on OfflineActivity - show feedback
                Toast.makeText(this, "Already on Offline Quiz screen", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.w("OfflineActivity", "Bottom dict button not found")
        }

        // Bottom quotes button - navigate to QuotesActivity
        try {
            btnBottomQuotes.setOnClickListener {
                navigateToQuotes()
            }
        } catch (e: Exception) {
            Log.w("OfflineActivity", "Bottom quotes button not found")
        }

        // Bottom bars/statistics button - navigate to ProgressActivity
        try {
            btnBottomStats.setOnClickListener {
                navigateToStatistics()
            }
        } catch (e: Exception) {
            Log.w("OfflineActivity", "Bottom stats button not found")
        }
    }

    // Navigation methods matching other activities functionality
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
        Toast.makeText(this, "Starting QuotesActivity...", Toast.LENGTH_SHORT).show()

        try {
            val intent = Intent(this, QuotesActivity::class.java)
            intent.putExtra("LANGUAGE", "afrikaans")
            startActivity(intent)
            Toast.makeText(this, "Intent sent successfully", Toast.LENGTH_SHORT).show()
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