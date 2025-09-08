package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import java.io.Serializable

class CategoryWordsActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var tvWordsTitle: TextView
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
    private lateinit var navDictionary: ImageView

    // Word collections for each category
    private val colorWords = arrayListOf(
        WordItem("Red", "Rooi", R.drawable.color_red),
        WordItem("Blue", "Blou", R.drawable.color_blue),
        WordItem("Green", "Groen", R.drawable.color_green),
        WordItem("Yellow", "Geel", R.drawable.color_yellow),
        WordItem("Purple", "Pers", R.drawable.color_purple),
        WordItem("Pink", "Pienk", R.drawable.color_pink)
    )

    private val emotionWords = arrayListOf(
        WordItem("Happy", "Gelukkig", R.drawable.rhino_happy),
        WordItem("Sad", "Hartseer", R.drawable.rhino_sad),
        WordItem("Fear", "Vrees", R.drawable.rhino_fear),
        WordItem("Shock", "Skok", R.drawable.rhino_shock),
        WordItem("Anger", "Woede", R.drawable.rhino_anger),
        WordItem("Silly", "Snaaks", R.drawable.rhino_silly)
    )

    private val animalWords = arrayListOf(
        WordItem("Lion", "Leeu", R.drawable.lion),
        WordItem("Elephant", "Olifant", R.drawable.elephant),
        WordItem("Giraffe", "Kameelperd", R.drawable.giraffe),
        WordItem("Zebra", "Zebra", R.drawable.zebra),
        WordItem("Hippo", "Seekoei", R.drawable.hippo)
    )

    private val foodWords = arrayListOf(
        WordItem("Apple", "Appel", R.drawable.apple),
        WordItem("Banana", "Piesang", R.drawable.banana),
        WordItem("Bread", "Brood", R.drawable.bread),
        WordItem("Milk", "Melk", R.drawable.milk),
        WordItem("Meat", "Vleis", R.drawable.meat),
        WordItem("Rice", "Rys", R.drawable.rice)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the category from intent
        currentCategory = intent.getStringExtra("CATEGORY") ?: "Emotions"

        // Set the appropriate layout based on category
        setContentLayout()

        initializeCommonViews()
        setupCommonClickListeners()
        setupCategorySpecificClickListeners()
        setupTestButtonClickListener()
        setupBottomNavigationListeners()
    }

    private fun setContentLayout() {
        when (currentCategory.toLowerCase()) {
            "animals" -> setContentView(R.layout.category_animals_layout)
            "colors", "colours" -> setContentView(R.layout.activity_category_colors)
            "food" -> setContentView(R.layout.category_food_layout)
            "emotions" -> setContentView(R.layout.activity_category_words)
            else -> setContentView(R.layout.activity_category_words) // Default to emotions
        }
    }

    private fun initializeCommonViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)
        tvWordsTitle = findViewById(R.id.tv_words_title)
        tvCategorySubtitle = findViewById(R.id.tv_category_subtitle)
        btnTestYourself = findViewById(R.id.btnTestYourself)

        // Navigation drawer items (these should be consistent across all layouts)
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

        // Update title based on category
        tvCategorySubtitle.text = currentCategory

        // Debug: Check if button was found
        if (::btnTestYourself.isInitialized) {
            println("Test button found and initialized")
        } else {
            println("Test button NOT found!")
        }
    }

    private fun setupCommonClickListeners() {
        // Menu button
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Navigation drawer click listeners with complete functionality
        navHome.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToHome()
        }

        navLanguage.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToLanguageSelection()
        }

        navWords.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToWordsActivity()
        }

        navPhrases.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToPhrasesActivity()
        }

        navProgress.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProgressActivity()
        }

        navVisibility.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToVisibilityModes()
        }

        navSettings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToSettings()
        }

        navProfile.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProfile()
        }

        navBack.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            onBackPressed()
        }

        navChat.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }

        navDictionary.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToOfflineQuiz()
        }
    }

    private fun setupBottomNavigationListeners() {
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
            Log.e("CategoryWordsActivity", "Navigation error", e)
        }
    }

    private fun navigateToStatistics() {
        val intent = Intent(this, ProgressActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun setupTestButtonClickListener() {
        btnTestYourself.setOnClickListener {
            // Add debug toast to confirm button click
            Toast.makeText(this, "Starting test for category: $currentCategory", Toast.LENGTH_SHORT).show()

            // Navigate to WordDetailActivity for testing all words in the category
            navigateToTestActivity()
        }
    }

    private fun navigateToTestActivity() {
        try {
            // Get the appropriate word list based on category
            val wordList = when (currentCategory.toLowerCase()) {
                "colors", "colours" -> colorWords
                "emotions" -> emotionWords
                "animals" -> animalWords
                "food" -> foodWords
                else -> emotionWords // Default to emotions
            }

            // Create intent with all words for testing
            val intent = Intent(this, WordDetailActivity::class.java).apply {
                putExtra("TEST_MODE", true)
                putExtra("CATEGORY", currentCategory)
                putExtra("WORD_LIST", wordList)
                putExtra("CURRENT_WORD_INDEX", 0)
                putExtra("TOTAL_WORDS", wordList.size)
                putExtra("CORRECT_ANSWERS", 0)
            }
            startActivity(intent)

            println("Successfully started test activity for category: $currentCategory with ${wordList.size} words")
        } catch (e: Exception) {
            println("Error starting WordDetailActivity: ${e.message}")
            Toast.makeText(this, "Error starting test: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupCategorySpecificClickListeners() {
        when (currentCategory.toLowerCase()) {
            "emotions" -> setupEmotionClickListeners()
            "animals" -> setupAnimalClickListeners()
            "colors", "colours" -> setupColorClickListeners()
            "food" -> setupFoodClickListeners()
        }
    }

    private fun setupEmotionClickListeners() {
        try {
            findViewById<CardView>(R.id.card_happy)?.setOnClickListener {
                navigateToWordDetail("Happy", "Gelukkig", R.drawable.rhino_happy)
            }
            findViewById<CardView>(R.id.card_sad)?.setOnClickListener {
                navigateToWordDetail("Sad", "Hartseer", R.drawable.rhino_sad)
            }
            findViewById<CardView>(R.id.card_fear)?.setOnClickListener {
                navigateToWordDetail("Fear", "Vrees", R.drawable.rhino_fear)
            }
            findViewById<CardView>(R.id.card_shock)?.setOnClickListener {
                navigateToWordDetail("Shock", "Skok", R.drawable.rhino_shock)
            }
            findViewById<CardView>(R.id.card_anger)?.setOnClickListener {
                navigateToWordDetail("Anger", "Woede", R.drawable.rhino_anger)
            }
            findViewById<CardView>(R.id.card_silly)?.setOnClickListener {
                navigateToWordDetail("Silly", "Snaaks", R.drawable.rhino_silly)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAnimalClickListeners() {
        try {
            findViewById<CardView>(R.id.card_lion)?.setOnClickListener {
                navigateToWordDetail("Lion", "Leeu", R.drawable.lion)
            }
            findViewById<CardView>(R.id.card_elephant)?.setOnClickListener {
                navigateToWordDetail("Elephant", "Olifant", R.drawable.elephant)
            }
            findViewById<CardView>(R.id.card_giraffe)?.setOnClickListener {
                navigateToWordDetail("Giraffe", "Kameelperd", R.drawable.giraffe)
            }
            findViewById<CardView>(R.id.card_zebra)?.setOnClickListener {
                navigateToWordDetail("Zebra", "Zebra", R.drawable.zebra)
            }
            findViewById<CardView>(R.id.card_hippo)?.setOnClickListener {
                navigateToWordDetail("Hippo", "Seekoei", R.drawable.hippo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupColorClickListeners() {
        try {
            findViewById<CardView>(R.id.card_red)?.setOnClickListener {
                navigateToWordDetail("Red", "Rooi", R.drawable.color_red)
            }
            findViewById<CardView>(R.id.card_blue)?.setOnClickListener {
                navigateToWordDetail("Blue", "Blou", R.drawable.color_blue)
            }
            findViewById<CardView>(R.id.card_green)?.setOnClickListener {
                navigateToWordDetail("Green", "Groen", R.drawable.color_green)
            }
            findViewById<CardView>(R.id.card_yellow)?.setOnClickListener {
                navigateToWordDetail("Yellow", "Geel", R.drawable.color_yellow)
            }
            findViewById<CardView>(R.id.card_purple)?.setOnClickListener {
                navigateToWordDetail("Purple", "Pers", R.drawable.color_purple)
            }
            findViewById<CardView>(R.id.card_pink)?.setOnClickListener {
                navigateToWordDetail("Pink", "Pienk", R.drawable.color_pink)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupFoodClickListeners() {
        try {
            findViewById<CardView>(R.id.card_apple)?.setOnClickListener {
                navigateToWordDetail("Apple", "Appel", R.drawable.apple)
            }
            findViewById<CardView>(R.id.card_banana)?.setOnClickListener {
                navigateToWordDetail("Banana", "Piesang", R.drawable.banana)
            }
            findViewById<CardView>(R.id.card_bread)?.setOnClickListener {
                navigateToWordDetail("Bread", "Brood", R.drawable.bread)
            }
            findViewById<CardView>(R.id.card_milk)?.setOnClickListener {
                navigateToWordDetail("Milk", "Melk", R.drawable.milk)
            }
            findViewById<CardView>(R.id.card_meat)?.setOnClickListener {
                navigateToWordDetail("Meat", "Vleis", R.drawable.meat)
            }
            findViewById<CardView>(R.id.card_rice)?.setOnClickListener {
                navigateToWordDetail("Rice", "Rys", R.drawable.rice)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateToWordDetail(englishWord: String, afrikaansWord: String, imageResource: Int) {
        val intent = Intent(this, WordDetailActivity::class.java).apply {
            putExtra("ENGLISH_WORD", englishWord)
            putExtra("AFRIKAANS_WORD", afrikaansWord)
            putExtra("IMAGE_RESOURCE", imageResource)
            putExtra("CATEGORY", currentCategory)
            putExtra("TEST_MODE", false)
        }
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
}