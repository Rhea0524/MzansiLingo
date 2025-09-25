package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class WordsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView

    // Category cards
    private lateinit var cardAnimals: CardView
    private lateinit var cardColors: CardView
    private lateinit var cardFood: CardView
    private lateinit var cardEmotions: CardView

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
        setContentView(R.layout.activity_words)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Initialize drawer and menu
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)

        // Initialize category cards
        cardAnimals = findViewById(R.id.card_animals)
        cardColors = findViewById(R.id.card_colors)
        cardFood = findViewById(R.id.card_food)
        cardEmotions = findViewById(R.id.card_emotions)

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
        cardAnimals.setOnClickListener {
            openCategoryActivity("Animals")
        }

        cardColors.setOnClickListener {
            openCategoryActivity("Colors")
        }

        cardFood.setOnClickListener {
            openCategoryActivity("Food")
        }

        cardEmotions.setOnClickListener {
            openCategoryActivity("Emotions")
        }

        // Navigation drawer click listeners
        navHome.setOnClickListener {
            navigateToHomeActivity()
        }

        navLanguage.setOnClickListener {
            navigateToLanguageSelection()
        }

        navWords.setOnClickListener {
            // Already in Words activity, just close drawer
            closeDrawer()
        }

        navPhrases.setOnClickListener {
            navigateToPhrasesActivity()
        }

        navProgress.setOnClickListener {
            navigateToProgressActivity()
        }



        navSettings.setOnClickListener {
            navigateToSettings()
        }

        navProfile.setOnClickListener {
            navigateToProfile()
        }

        navBack.setOnClickListener {
            closeDrawer()
            onBackPressed()
        }

        navChat.setOnClickListener {
            navigateToAiChatActivity()
        }

        navDictionary.setOnClickListener {
            navigateToOfflineQuiz()
        }
    }

    private fun openCategoryActivity(category: String) {
        val intent = Intent(this, CategoryWordsActivity::class.java)
        intent.putExtra("CATEGORY", category)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        closeDrawer()
    }

    private fun navigateToLanguageSelection() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        startActivity(intent)
        closeDrawer()
    }

    private fun navigateToPhrasesActivity() {
        val intent = Intent(this, PhrasesActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
        closeDrawer()
    }

    private fun navigateToProgressActivity() {
        val intent = Intent(this, ProgressActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
        closeDrawer()
    }

    private fun navigateToAiChatActivity() {
        val intent = Intent(this, AiChatActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
        closeDrawer()
    }



    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        closeDrawer()
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        closeDrawer()
    }

    private fun navigateToOfflineQuiz() {
        val intent = Intent(this, OfflineActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
        closeDrawer()
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
}