package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import java.io.Serializable

data class PhraseItem(
    val english: String,
    val afrikaans: String
) : Serializable

class CategoryPhrasesActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var tvPhrasesTitle: TextView
    private lateinit var tvCategorySubtitle: TextView
    private lateinit var currentCategory: String
    private lateinit var btnTestYourself: MaterialButton

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

    // Phrase collections for each category
    private val politePhrases = arrayListOf(
        PhraseItem("Hello", "Hallo"),
        PhraseItem("Goodbye", "Totsiens"),
        PhraseItem("Please", "Asseblief"),
        PhraseItem("Thank you", "Dankie"),
        PhraseItem("Yes", "Ja"),
        PhraseItem("No", "Nee")
    )

    private val communicationPhrases = arrayListOf(
        PhraseItem("I need help. Can you help me?", "Ek het hulp nodig. Kan jy my help?"),
        PhraseItem("I don't understand", "Ek verstaan nie"),
        PhraseItem("What time is it?", "Hoe laat is dit?"),
        PhraseItem("How much does this cost?", "Hoeveel kos dit?"),
        PhraseItem("Where is the bathroom?", "Waar is die badkamer?"),
        PhraseItem("Can you speak English?", "Kan jy Engels praat?")
    )

    private val personalPhrases = arrayListOf(
        PhraseItem("My name is...", "My naam is..."),
        PhraseItem("I am from...", "Ek kom van..."),
        PhraseItem("How old are you?", "Hoe oud is jy?"),
        PhraseItem("Where do you live?", "Waar bly jy?"),
        PhraseItem("What is your phone number?", "Wat is jou foonnommer?"),
        PhraseItem("What do you do for work?", "Wat doen jy vir werk?")
    )

    private val travelPhrases = arrayListOf(
        PhraseItem("Where is the train station?", "Waar is die treinstasie?"),
        PhraseItem("I need a taxi", "Ek het 'n taxi nodig"),
        PhraseItem("Where can I buy food?", "Waar kan ek kos koop?"),
        PhraseItem("Is there a hospital nearby?", "Is daar 'n hospitaal naby?"),
        PhraseItem("Where is the nearest ATM?", "Waar is die naaste OTM?"),
        PhraseItem("I am lost. Can you help me?", "Ek is verlore. Kan jy my help?")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the category from intent
        currentCategory = intent.getStringExtra("CATEGORY") ?: "Polite and Essential"

        // Debug logging
        Log.d("CategoryPhrases", "Received category: '$currentCategory'")

        // Set layout based on category
        setLayoutForCategory(currentCategory)

        try {
            initializeCommonViews()
            setupCommonClickListeners()
            setupPhraseButtonClickListeners()
            setupTestButtonClickListener()
        } catch (e: Exception) {
            Log.e("CategoryPhrases", "Error initializing views: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error loading activity: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setLayoutForCategory(category: String) {
        val normalizedCategory = category.toLowerCase().replace(" ", "").replace("&", "")
        Log.d("CategoryPhrases", "Normalized category: '$normalizedCategory'")

        when (normalizedCategory) {
            "communicationandinformation", "communicationinformation", "communication" -> {
                Log.d("CategoryPhrases", "Loading communication layout")
                setContentView(R.layout.activity_phrases_communication)
            }
            "personalinformation", "personal" -> {
                Log.d("CategoryPhrases", "Loading personal information layout")
                setContentView(R.layout.activity_phrases_personal)
            }
            "traveldailyneeds", "travel", "travelanddailyneeds" -> {
                Log.d("CategoryPhrases", "Loading travel layout")
                setContentView(R.layout.activity_phrases_travel)
            }
            "politeandessential", "politeessential", "polite", "politeness" -> {
                Log.d("CategoryPhrases", "Loading politeness layout")
                setContentView(R.layout.activity_phrases_politeness)
            }
            else -> {
                Log.d("CategoryPhrases", "Loading default (politeness) layout for category: '$normalizedCategory'")
                setContentView(R.layout.activity_phrases_politeness)
            }
        }
    }

    private fun initializeCommonViews() {
        // Check if views exist before initializing
        try {
            drawerLayout = findViewById(R.id.drawer_layout)
            btnMenu = findViewById(R.id.btn_menu)
            tvPhrasesTitle = findViewById(R.id.tv_phrases_title)
            tvCategorySubtitle = findViewById(R.id.tv_category_subtitle)
            btnTestYourself = findViewById(R.id.btnTestYourself)

            // Navigation drawer items - check if they exist
            try {
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
            } catch (e: Exception) {
                Log.w("CategoryPhrases", "Some navigation views not found: ${e.message}")
                // Continue without navigation drawer if views don't exist
            }

            // Update category subtitle based on current category
            tvCategorySubtitle.text = when (currentCategory.toLowerCase().replace(" ", "").replace("&", "")) {
                "communicationandinformation", "communicationinformation", "communication" ->
                    "Communication & Information/\nKommunikasie & Inligting"
                "personalinformation", "personal" ->
                    "Personal Information/\nPersoonlike Inligting"
                "traveldailyneeds", "travel", "travelanddailyneeds" ->
                    "Travel & Daily Needs/\nReis & Daaglikse Behoeftes"
                else -> currentCategory
            }

            Log.d("CategoryPhrases", "Views initialized successfully")

        } catch (e: Exception) {
            Log.e("CategoryPhrases", "Error initializing main views: ${e.message}")
            throw e
        }
    }

    private fun setupCommonClickListeners() {
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Only set up navigation listeners if the views exist
        try {
            navHome.setOnClickListener { navigateToActivity(HomeActivity::class.java) }
            navLanguage.setOnClickListener { closeDrawer() }
            navWords.setOnClickListener { navigateToActivity(WordsActivity::class.java) }
            navPhrases.setOnClickListener { closeDrawer() }
            navProgress.setOnClickListener { closeDrawer() }
            navVisibility.setOnClickListener { closeDrawer() }
            navSettings.setOnClickListener { closeDrawer() }
            navProfile.setOnClickListener { closeDrawer() }
            navBack.setOnClickListener { onBackPressed() }
            navChat.setOnClickListener { closeDrawer() }
        } catch (e: Exception) {
            Log.w("CategoryPhrases", "Navigation drawer not fully set up: ${e.message}")
        }
    }

    private fun setupPhraseButtonClickListeners() {
        val normalizedCategory = currentCategory.toLowerCase().replace(" ", "").replace("&", "")

        when (normalizedCategory) {
            "communicationandinformation", "communicationinformation", "communication" -> {
                setupCommunicationButtonClickListeners()
            }
            "personalinformation", "personal" -> {
                setupPersonalButtonClickListeners()
            }
            "traveldailyneeds", "travel", "travelanddailyneeds" -> {
                setupTravelButtonClickListeners()
            }
            "politeandessential", "politeessential", "polite", "politeness" -> {
                setupPolitenessButtonClickListeners()
            }
        }
    }

    private fun setupCommunicationButtonClickListeners() {
        try {

            findViewById<MaterialButton>(R.id.btn_need_help_en)?.setOnClickListener {
                navigateToPhraseDetail("I need help. Can you help me?", "Ek het hulp nodig. Kan jy my help?")
            }
            findViewById<MaterialButton>(R.id.btn_need_help_af)?.setOnClickListener {
                navigateToPhraseDetail("I need help. Can you help me?", "Ek het hulp nodig. Kan jy my help?")
            }


            findViewById<MaterialButton>(R.id.btn_dont_understand_en)?.setOnClickListener {
                navigateToPhraseDetail("I don't understand", "Ek verstaan nie")
            }
            findViewById<MaterialButton>(R.id.btn_dont_understand_af)?.setOnClickListener {
                navigateToPhraseDetail("I don't understand", "Ek verstaan nie")
            }

            // What time buttons
            findViewById<MaterialButton>(R.id.btn_what_time_en)?.setOnClickListener {
                navigateToPhraseDetail("What time is it?", "Hoe laat is dit?")
            }
            findViewById<MaterialButton>(R.id.btn_what_time_af)?.setOnClickListener {
                navigateToPhraseDetail("What time is it?", "Hoe laat is dit?")
            }

            // How much cost buttons
            findViewById<MaterialButton>(R.id.btn_how_much_cost_en)?.setOnClickListener {
                navigateToPhraseDetail("How much does this cost?", "Hoeveel kos dit?")
            }
            findViewById<MaterialButton>(R.id.btn_how_much_cost_af)?.setOnClickListener {
                navigateToPhraseDetail("How much does this cost?", "Hoeveel kos dit?")
            }

            // Bathroom buttons
            findViewById<MaterialButton>(R.id.btn_bathroom_en)?.setOnClickListener {
                navigateToPhraseDetail("Where is the bathroom?", "Waar is die badkamer?")
            }
            findViewById<MaterialButton>(R.id.btn_bathroom_af)?.setOnClickListener {
                navigateToPhraseDetail("Where is the bathroom?", "Waar is die badkamer?")
            }

            // Speak English buttons
            findViewById<MaterialButton>(R.id.btn_speak_english_en)?.setOnClickListener {
                navigateToPhraseDetail("Can you speak English?", "Kan jy Engels praat?")
            }
            findViewById<MaterialButton>(R.id.btn_speak_english_af)?.setOnClickListener {
                navigateToPhraseDetail("Can you speak English?", "Kan jy Engels praat?")
            }

        } catch (e: Exception) {
            Log.w("CategoryPhrases", "Error setting up communication button listeners: ${e.message}")
        }
    }

    private fun setupPersonalButtonClickListeners() {
        try {
            // My name buttons
            findViewById<MaterialButton>(R.id.btn_my_name_en)?.setOnClickListener {
                navigateToPhraseDetail("My name is...", "My naam is...")
            }
            findViewById<MaterialButton>(R.id.btn_my_name_af)?.setOnClickListener {
                navigateToPhraseDetail("My name is...", "My naam is...")
            }

            // I am from buttons
            findViewById<MaterialButton>(R.id.btn_i_am_from_en)?.setOnClickListener {
                navigateToPhraseDetail("I am from...", "Ek kom van...")
            }
            findViewById<MaterialButton>(R.id.btn_i_am_from_af)?.setOnClickListener {
                navigateToPhraseDetail("I am from...", "Ek kom van...")
            }

            // How old buttons
            findViewById<MaterialButton>(R.id.btn_how_old_en)?.setOnClickListener {
                navigateToPhraseDetail("How old are you?", "Hoe oud is jy?")
            }
            findViewById<MaterialButton>(R.id.btn_how_old_af)?.setOnClickListener {
                navigateToPhraseDetail("How old are you?", "Hoe oud is jy?")
            }

            // Where live buttons
            findViewById<MaterialButton>(R.id.btn_where_live_en)?.setOnClickListener {
                navigateToPhraseDetail("Where do you live?", "Waar bly jy?")
            }
            findViewById<MaterialButton>(R.id.btn_where_live_af)?.setOnClickListener {
                navigateToPhraseDetail("Where do you live?", "Waar bly jy?")
            }

            // Phone number buttons
            findViewById<MaterialButton>(R.id.btn_phone_number_en)?.setOnClickListener {
                navigateToPhraseDetail("What is your phone number?", "Wat is jou foonnommer?")
            }
            findViewById<MaterialButton>(R.id.btn_phone_number_af)?.setOnClickListener {
                navigateToPhraseDetail("What is your phone number?", "Wat is jou foonnommer?")
            }

            // What work buttons
            findViewById<MaterialButton>(R.id.btn_what_work_en)?.setOnClickListener {
                navigateToPhraseDetail("What do you do for work?", "Wat doen jy vir werk?")
            }
            findViewById<MaterialButton>(R.id.btn_what_work_af)?.setOnClickListener {
                navigateToPhraseDetail("What do you do for work?", "Wat doen jy vir werk?")
            }

        } catch (e: Exception) {
            Log.w("CategoryPhrases", "Error setting up personal button listeners: ${e.message}")
        }
    }

    private fun setupTravelButtonClickListeners() {
        try {
            // Train station buttons
            findViewById<MaterialButton>(R.id.btn_train_station_en)?.setOnClickListener {
                navigateToPhraseDetail("Where is the train station?", "Waar is die treinstasie?")
            }
            findViewById<MaterialButton>(R.id.btn_train_station_af)?.setOnClickListener {
                navigateToPhraseDetail("Where is the train station?", "Waar is die treinstasie?")
            }

            // Need taxi buttons
            findViewById<MaterialButton>(R.id.btn_need_taxi_en)?.setOnClickListener {
                navigateToPhraseDetail("I need a taxi", "Ek het 'n taxi nodig")
            }
            findViewById<MaterialButton>(R.id.btn_need_taxi_af)?.setOnClickListener {
                navigateToPhraseDetail("I need a taxi", "Ek het 'n taxi nodig")
            }

            // Buy food buttons
            findViewById<MaterialButton>(R.id.btn_buy_food_en)?.setOnClickListener {
                navigateToPhraseDetail("Where can I buy food?", "Waar kan ek kos koop?")
            }
            findViewById<MaterialButton>(R.id.btn_buy_food_af)?.setOnClickListener {
                navigateToPhraseDetail("Where can I buy food?", "Waar kan ek kos koop?")
            }

            // Hospital nearby buttons
            findViewById<MaterialButton>(R.id.btn_hospital_nearby_en)?.setOnClickListener {
                navigateToPhraseDetail("Is there a hospital nearby?", "Is daar 'n hospitaal naby?")
            }
            findViewById<MaterialButton>(R.id.btn_hospital_nearby_af)?.setOnClickListener {
                navigateToPhraseDetail("Is there a hospital nearby?", "Is daar 'n hospitaal naby?")
            }

            // Nearest ATM buttons
            findViewById<MaterialButton>(R.id.btn_nearest_atm_en)?.setOnClickListener {
                navigateToPhraseDetail("Where is the nearest ATM?", "Waar is die naaste OTM?")
            }
            findViewById<MaterialButton>(R.id.btn_nearest_atm_af)?.setOnClickListener {
                navigateToPhraseDetail("Where is the nearest ATM?", "Waar is die naaste OTM?")
            }

            // I am lost buttons
            findViewById<MaterialButton>(R.id.btn_i_am_lost_en)?.setOnClickListener {
                navigateToPhraseDetail("I am lost. Can you help me?", "Ek is verlore. Kan jy my help?")
            }
            findViewById<MaterialButton>(R.id.btn_i_am_lost_af)?.setOnClickListener {
                navigateToPhraseDetail("I am lost. Can you help me?", "Ek is verlore. Kan jy my help?")
            }

        } catch (e: Exception) {
            Log.w("CategoryPhrases", "Error setting up travel button listeners: ${e.message}")
        }
    }

    private fun setupPolitenessButtonClickListeners() {
        try {
            // Please buttons
            findViewById<MaterialButton>(R.id.btn_please_en)?.setOnClickListener {
                navigateToPhraseDetail("Please", "Asseblief")
            }
            findViewById<MaterialButton>(R.id.btn_please_af)?.setOnClickListener {
                navigateToPhraseDetail("Please", "Asseblief")
            }

            // Thank you buttons
            findViewById<MaterialButton>(R.id.btn_thank_you_en)?.setOnClickListener {
                navigateToPhraseDetail("Thank you", "Dankie")
            }
            findViewById<MaterialButton>(R.id.btn_thank_you_af)?.setOnClickListener {
                navigateToPhraseDetail("Thank you", "Dankie")
            }

            // Excuse me buttons
            findViewById<MaterialButton>(R.id.btn_excuse_me_en)?.setOnClickListener {
                navigateToPhraseDetail("Excuse me", "Verskoon my")
            }
            findViewById<MaterialButton>(R.id.btn_excuse_me_af)?.setOnClickListener {
                navigateToPhraseDetail("Excuse me", "Verskoon my")
            }

            // I'm sorry buttons
            findViewById<MaterialButton>(R.id.btn_sorry_en)?.setOnClickListener {
                navigateToPhraseDetail("I'm sorry", "Ek is jammer")
            }
            findViewById<MaterialButton>(R.id.btn_sorry_af)?.setOnClickListener {
                navigateToPhraseDetail("I'm sorry", "Ek is jammer")
            }

            // You're welcome buttons
            findViewById<MaterialButton>(R.id.btn_welcome_en)?.setOnClickListener {
                navigateToPhraseDetail("You're welcome", "Dis 'n plesier")
            }
            findViewById<MaterialButton>(R.id.btn_welcome_af)?.setOnClickListener {
                navigateToPhraseDetail("You're welcome", "Dis 'n plesier")
            }

            // May I have buttons
            findViewById<MaterialButton>(R.id.btn_may_i_have_en)?.setOnClickListener {
                navigateToPhraseDetail("May I have...", "Mag ek asseblief he...")
            }
            findViewById<MaterialButton>(R.id.btn_may_i_have_af)?.setOnClickListener {
                navigateToPhraseDetail("May I have...", "Mag ek asseblief he...")
            }

        } catch (e: Exception) {
            Log.w("CategoryPhrases", "Error setting up politeness button listeners: ${e.message}")
        }
    }

    private fun navigateToPhraseDetail(englishPhrase: String, afrikaansPhrase: String) {
        try {
            val intent = Intent(this, PhrasesDetailActivity::class.java).apply {
                putExtra("ENGLISH_PHRASE", englishPhrase)
                putExtra("AFRIKAANS_PHRASE", afrikaansPhrase)
                putExtra("CATEGORY", currentCategory)
                putExtra("TEST_MODE", false)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("CategoryPhrases", "Error navigating to phrase detail: ${e.message}")
            Toast.makeText(this, "Error opening phrase details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTestButtonClickListener() {
        btnTestYourself.setOnClickListener {
            Toast.makeText(this, "Starting test for category: $currentCategory", Toast.LENGTH_SHORT).show()
            navigateToTestActivity()
        }
    }

    private fun navigateToTestActivity() {
        try {
            val phraseList = when (currentCategory.toLowerCase().replace(" ", "").replace("&", "")) {
                "polite", "politeandessential", "politeessential", "politeness" -> politePhrases
                "communication", "communicationandinformation", "communicationinformation" -> communicationPhrases
                "personal", "personalinformation" -> personalPhrases
                "travel", "traveldailyneeds", "travelanddailyneeds" -> travelPhrases
                else -> politePhrases
            }

            val intent = Intent(this, PhrasesDetailActivity::class.java).apply {
                putExtra("TEST_MODE", true)
                putExtra("CATEGORY", currentCategory)
                putExtra("PHRASE_LIST", phraseList)
                putExtra("CURRENT_PHRASE_INDEX", 0)
                putExtra("TOTAL_PHRASES", phraseList.size)
                putExtra("CORRECT_ANSWERS", 0)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error starting test: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        closeDrawer()
    }

    private fun closeDrawer() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    override fun onBackPressed() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}