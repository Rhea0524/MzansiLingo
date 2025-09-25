package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class HomeActivity : AppCompatActivity() {

    private lateinit var btnWords: Button
    private lateinit var btnPhrases: Button
    private lateinit var btnLeaderboard: Button
    private lateinit var btnSetGoals: Button
    private lateinit var btnAiChat: Button
    private lateinit var btnMenu: ImageView
    private lateinit var tvLanguage: TextView
    private lateinit var ivMascot: ImageView
    private lateinit var btnBottomDict: ImageView
    private lateinit var btnBottomQuotes: ImageView
    private lateinit var btnBottomStats: ImageView
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        initializeViews()

        // Set up the UI for Afrikaans
        setupAfrikaansUI()

        // Set up click listeners
        setupClickListeners()

        // Set up drawer
        setupDrawer()
    }

    private fun initializeViews() {
        btnWords = findViewById(R.id.btn_words)
        btnPhrases = findViewById(R.id.btn_phrases)
        btnLeaderboard = findViewById(R.id.btn_leaderboard)
        btnSetGoals = findViewById(R.id.btn_set_goals)
        btnAiChat = findViewById(R.id.btn_ai_chat)
        btnMenu = findViewById(R.id.btn_menu)
        tvLanguage = findViewById(R.id.tv_language)
        ivMascot = findViewById(R.id.imgRhino)
        btnBottomDict = findViewById(R.id.btn_bottom_dict)
        btnBottomQuotes = findViewById(R.id.btn_bottom_quotes)
        btnBottomStats = findViewById(R.id.btn_bottom_stats)
        drawerLayout = findViewById(R.id.drawer_layout)
    }

    private fun setupAfrikaansUI() {
        // Set language header
        tvLanguage.text = "LANGUAGE:\nAFRIKAANS"

        // Set button texts for Afrikaans
        btnWords.text = "WORDS / WOORDE"
        btnPhrases.text = "PHRASES / FRASES"
        btnLeaderboard.text = "LEADERBOARD / RANGLYS"
        btnSetGoals.text = "SET GOALS / STEL DOELWITTE"
        btnAiChat.text = "AI CHAT / KI-KLETS"

        // Set mascot image (using your existing rhino drawable)
        ivMascot.setImageResource(R.drawable.rhino)

        // Set background colors
        window.statusBarColor = ContextCompat.getColor(this, R.color.mz_navy_dark)
    }

    private fun setupDrawer() {
        // Menu button to open/close drawer
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Navigation drawer item listeners
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            // Already on home, so just close drawer
        }

        findViewById<TextView>(R.id.nav_language).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            // Navigate to language selection
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

        findViewById<TextView>(R.id.nav_leaderboard).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToLeaderboardActivity()
        }



        findViewById<TextView>(R.id.nav_settings).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToSettings()
        }

        findViewById<TextView>(R.id.nav_profile).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProfile()
        }

        findViewById<ImageView>(R.id.nav_back).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<ImageView>(R.id.nav_chat).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }

        findViewById<ImageView>(R.id.nav_dictionary).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToOfflineQuiz()
        }
    }

    private fun setupClickListeners() {
        btnWords.setOnClickListener {
            navigateToWordsActivity()
        }

        btnPhrases.setOnClickListener {
            navigateToPhrasesActivity()
        }

        btnLeaderboard.setOnClickListener {
            navigateToLeaderboardActivity()
        }

        btnSetGoals.setOnClickListener {
            navigateToSetGoalsActivity()
        }

        btnAiChat.setOnClickListener {
            navigateToAiChatActivity()
        }

        // Bottom navigation buttons
        btnBottomDict.setOnClickListener {
            navigateToOfflineQuiz()
        }

        btnBottomQuotes.setOnClickListener {
            navigateToQuotes()
        }

        btnBottomStats.setOnClickListener {
            navigateToStatistics()
        }
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

    private fun navigateToLeaderboardActivity() {
        val intent = Intent(this, LeaderboardActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToSetGoalsActivity() {
        val intent = Intent(this, SetGoalsActivity::class.java)
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

    private fun navigateToQuotes() {
        Toast.makeText(this, "Starting QuotesActivity...", Toast.LENGTH_SHORT).show()

        try {
            val intent = Intent(this, QuotesActivity::class.java)
            intent.putExtra("LANGUAGE", "afrikaans")
            startActivity(intent)
            Toast.makeText(this, "Intent sent successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("HomeActivity", "Navigation error", e)
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

// Data class to hold language-specific content
data class LanguageContent(
    val languageName: String,
    val wordsText: String,
    val phrasesText: String,
    val leaderboardText: String,
    val setGoalsText: String,
    val aiChatText: String,
    val welcomeMessage: String
)


object LanguageContentProvider {

    fun getAfrikaansContent(): LanguageContent {
        return LanguageContent(
            languageName = "AFRIKAANS",
            wordsText = "WORDS / WOORDE",
            phrasesText = "PHRASES / FRASES",
            leaderboardText = "LEADERBOARD / RANGLYS",
            setGoalsText = "SET GOALS / STEL DOELWITTE",
            aiChatText = "AI CHAT / KI-KLETS",
            welcomeMessage = "Welkom terug!"
        )
    }


    fun getSpanishContent(): LanguageContent {
        return LanguageContent(
            languageName = "ESPAÑOL",
            wordsText = "WORDS / PALABRAS",
            phrasesText = "PHRASES / FRASES",
            leaderboardText = "LEADERBOARD / TABLA DE POSICIONES",
            setGoalsText = "SET GOALS / ESTABLECER METAS",
            aiChatText = "AI CHAT / CHAT IA",
            welcomeMessage = "¡Bienvenido de nuevo!"
        )
    }
}