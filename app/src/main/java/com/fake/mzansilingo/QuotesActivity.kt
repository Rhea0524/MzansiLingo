package com.fake.mzansilingo

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder

class QuotesActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // TTS variables (added from WordDetailActivity)
    private var mediaPlayer: MediaPlayer? = null
    private val client = OkHttpClient()

    // Quote pairs (English and Afrikaans)
    private val quotes = listOf(
        Pair("The journey of a thousand miles begins with a single step",
            "Die reis van 'n duisend myl begin met 'n enkele tree"),
        Pair("Believe you can and you're halfway there",
            "Glo jy kan en jy is halfpad daar"),
        Pair("Success is not final, failure is not fatal",
            "Sukses is nie finaal nie, mislukking is nie fataal nie"),
        Pair("The only way to do great work is to love what you do",
            "Die enigste manier om groot werk te doen is om lief te hê wat jy doen"),
        Pair("Education is the most powerful weapon which you can use to change the world",
            "Onderwys is die kragtigste wapen wat jy kan gebruik om die wêreld te verander"),
        Pair("It does not matter how slowly you go as long as you do not stop",
            "Dit maak nie saak hoe stadig jy gaan nie, solank jy nie stop nie")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "=== QuotesActivity onCreate started ===")
        super.onCreate(savedInstanceState)
        Log.d(TAG, "super.onCreate completed")

        try {
            Log.d(TAG, "About to call setContentView")
            setContentView(R.layout.activity_quotes)
            Log.d(TAG, "setContentView completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in setContentView", e)
            Toast.makeText(this, "Layout error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            Log.d(TAG, "About to call initializeViews")
            initializeViews()
            Log.d(TAG, "initializeViews completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeViews", e)
            Toast.makeText(this, "View initialization error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            Log.d(TAG, "About to call setupQuoteButtons")
            setupQuoteButtons()
            Log.d(TAG, "setupQuoteButtons completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupQuoteButtons", e)
            Toast.makeText(this, "Button setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        try {
            Log.d(TAG, "About to call setupNavigationDrawer")
            setupNavigationDrawer()
            Log.d(TAG, "setupNavigationDrawer completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupNavigationDrawer", e)
            Toast.makeText(this, "Navigation setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        Log.d(TAG, "=== QuotesActivity onCreate finished ===")
    }

    override fun onResume() {
        super.onResume()
        // Refresh the activity if language changed
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = prefs.getString("home_language", "English") ?: "English"

        // Check if the locale matches the saved language
        val currentLocale = resources.configuration.locales[0].language
        val expectedLocale = when (currentLanguage) {
            "English" -> "en"
            "isiZulu" -> "zu"
            else -> "en"
        }

        if (currentLocale != expectedLocale) {
            recreate() // Recreate activity to apply new language
        }
    }

    private fun initializeViews() {
        Log.d(TAG, "Finding drawer_layout")
        drawerLayout = findViewById(R.id.drawer_layout)
        Log.d(TAG, "drawer_layout found successfully")

        Log.d(TAG, "Finding btn_menu")
        val menuButton = findViewById<ImageView>(R.id.btn_menu)
        Log.d(TAG, "btn_menu found successfully")

        // Menu button
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
        Log.d(TAG, "Menu button click listener set")
    }

    private fun setupQuoteButtons() {
        Log.d(TAG, "Setting up quote buttons")

        // Journey quote - Show quotes and play TTS when clicked
        findViewById<CardView>(R.id.card_journey_en).setOnClickListener {
            showQuoteToast(quotes[0].first, getString(R.string.language_english))
            speakQuote(quotes[0].first, "en")
        }
        findViewById<CardView>(R.id.card_journey_af).setOnClickListener {
            showQuoteToast(quotes[0].second, getString(R.string.language_afrikaans))
            speakQuote(quotes[0].second, "af")
        }

        // Believe quote
        findViewById<CardView>(R.id.card_believe_en).setOnClickListener {
            showQuoteToast(quotes[1].first, getString(R.string.language_english))
            speakQuote(quotes[1].first, "en")
        }
        findViewById<CardView>(R.id.card_believe_af).setOnClickListener {
            showQuoteToast(quotes[1].second, getString(R.string.language_afrikaans))
            speakQuote(quotes[1].second, "af")
        }

        // Success quote
        findViewById<CardView>(R.id.card_success_en).setOnClickListener {
            showQuoteToast(quotes[2].first, getString(R.string.language_english))
            speakQuote(quotes[2].first, "en")
        }
        findViewById<CardView>(R.id.card_success_af).setOnClickListener {
            showQuoteToast(quotes[2].second, getString(R.string.language_afrikaans))
            speakQuote(quotes[2].second, "af")
        }

        // Great work quote
        findViewById<CardView>(R.id.card_great_work_en).setOnClickListener {
            showQuoteToast(quotes[3].first, getString(R.string.language_english))
            speakQuote(quotes[3].first, "en")
        }
        findViewById<CardView>(R.id.card_great_work_af).setOnClickListener {
            showQuoteToast(quotes[3].second, getString(R.string.language_afrikaans))
            speakQuote(quotes[3].second, "af")
        }

        // Education quote
        findViewById<CardView>(R.id.card_education_en).setOnClickListener {
            showQuoteToast(quotes[4].first, getString(R.string.language_english))
            speakQuote(quotes[4].first, "en")
        }
        findViewById<CardView>(R.id.card_education_af).setOnClickListener {
            showQuoteToast(quotes[4].second, getString(R.string.language_afrikaans))
            speakQuote(quotes[4].second, "af")
        }

        // Slowly quote
        findViewById<CardView>(R.id.card_slowly_en).setOnClickListener {
            showQuoteToast(quotes[5].first, getString(R.string.language_english))
            speakQuote(quotes[5].first, "en")
        }
        findViewById<CardView>(R.id.card_slowly_af).setOnClickListener {
            showQuoteToast(quotes[5].second, getString(R.string.language_afrikaans))
            speakQuote(quotes[5].second, "af")
        }

        Log.d(TAG, "All quote buttons set up successfully")
    }

    // Method to speak quote using Google Translate TTS API (adapted from WordDetailActivity)
    private fun speakQuote(text: String, language: String) {
        try {
            // Show loading indicator
            Toast.makeText(this, getString(R.string.loading_pronunciation), Toast.LENGTH_SHORT).show()

            // Google Translate TTS URL with language parameter
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val ttsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&tl=$language&client=tw-ob&q=$encodedText"

            val request = Request.Builder()
                .url(ttsUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this@QuotesActivity, getString(R.string.pronunciation_unavailable), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.let { body ->
                            try {
                                // Release previous MediaPlayer if exists
                                mediaPlayer?.release()

                                // Create temporary file
                                val tempFile = createTempFile("tts_quote", ".mp3", cacheDir)
                                tempFile.writeBytes(body.bytes())

                                Handler(Looper.getMainLooper()).post {
                                    playAudioFile(tempFile.absolutePath)
                                }

                            } catch (e: Exception) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(this@QuotesActivity, getString(R.string.audio_playback_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this@QuotesActivity, getString(R.string.pronunciation_service_unavailable), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_prefix) + " ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to play the downloaded audio file (adapted from WordDetailActivity)
    private fun playAudioFile(filePath: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener { mp ->
                    mp.start()
                    Toast.makeText(this@QuotesActivity, getString(R.string.playing_quote), Toast.LENGTH_SHORT).show()
                }
                setOnCompletionListener { mp ->
                    mp.release()
                    // Clean up temp file
                    try {
                        java.io.File(filePath).delete()
                    } catch (e: Exception) {
                        Log.d(TAG, "Could not delete temp file: ${e.message}")
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    mp.release()
                    Toast.makeText(this@QuotesActivity, getString(R.string.audio_playback_failed), Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.media_player_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showQuoteToast(quote: String, language: String) {
        Toast.makeText(this, "$language: $quote", Toast.LENGTH_LONG).show()
        Log.d(TAG, "Showed $language quote: $quote")
    }

    private fun setupNavigationDrawer() {
        Log.d(TAG, "Setting up navigation drawer")

        // Navigation drawer item listeners - matching HomeActivity functionality
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

        findViewById<TextView>(R.id.nav_quotes).setOnClickListener {
            // Already on quotes page, just close drawer
            drawerLayout.closeDrawer(GravityCompat.END)
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

        // Bottom navigation icons
        findViewById<ImageView>(R.id.nav_back).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            onBackPressed()
        }

        findViewById<ImageView>(R.id.nav_chat).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }

        // Dictionary button (if present)
        try {
            findViewById<ImageView>(R.id.nav_dictionary)?.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                navigateToOfflineQuiz()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Dictionary button not found - this is expected in some layouts")
        }

        Log.d(TAG, "Navigation drawer set up successfully")
    }

    // Navigation methods matching HomeActivity functionality
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

    private fun navigateToOfflineQuiz() {
        val intent = Intent(this, OfflineActivity::class.java)
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

    override fun onDestroy() {
        super.onDestroy()
        // Clean up MediaPlayer to prevent memory leaks
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val TAG = "QuotesActivity"

        // Helper function to get random quote
        fun getRandomQuote(): Pair<String, String> {
            val quotes = listOf(
                Pair("The journey of a thousand miles begins with a single step",
                    "Die reis van 'n duisend myl begin met 'n enkele tree"),
                Pair("Believe you can and you're halfway there",
                    "Glo jy kan en jy is halfpad daar"),
                Pair("Success is not final, failure is not fatal",
                    "Sukses is nie finaal nie, mislukking is nie fataal nie"),
                Pair("The only way to do great work is to love what you do",
                    "Die enigste manier om groot werk te doen is om lief te hê wat jy doen"),
                Pair("Education is the most powerful weapon which you can use to change the world",
                    "Onderwys is die kragtigste wapen wat jy kan gebruik om die wêreld te verander"),
                Pair("It does not matter how slowly you go as long as you do not stop",
                    "Dit maak nie saak hoe stadig jy gaan nie, solank jy nie stop nie")
            )
            return quotes.random()
        }

        // Helper function to get all quotes
        fun getAllQuotes(): List<Pair<String, String>> {
            return listOf(
                Pair("The journey of a thousand miles begins with a single step",
                    "Die reis van 'n duisend myl begin met 'n enkele tree"),
                Pair("Believe you can and you're halfway there",
                    "Glo jy kan en jy is halfpad daar"),
                Pair("Success is not final, failure is not fatal",
                    "Sukses is nie finaal nie, mislukking is nie fataal nie"),
                Pair("The only way to do great work is to love what you do",
                    "Die enigste manier om groot werk te doen is om lief te hê wat jy doen"),
                Pair("Education is the most powerful weapon which you can use to change the world",
                    "Onderwys is die kragtigste wapen wat jy kan gebruik om die wêreld te verander"),
                Pair("It does not matter how slowly you go as long as you do not stop",
                    "Dit maak nie saak hoe stadig jy gaan nie, solank jy nie stop nie")
            )
        }
    }
}