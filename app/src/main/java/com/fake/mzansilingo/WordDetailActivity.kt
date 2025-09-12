package com.fake.mzansilingo

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import java.util.*

class WordDetailActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    private var userCollectionUserId: String? = null // Store user collection userId

    // TTS variables
    private var mediaPlayer: MediaPlayer? = null
    private val client = OkHttpClient()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var tvWordsTitle: TextView
    private lateinit var tvCategorySubtitle: TextView
    private lateinit var tvWordEnglish: TextView
    private lateinit var imgWord: ImageView
    private lateinit var btnSound: ImageView

    // Multiple choice buttons
    private lateinit var btnOption1: MaterialButton
    private lateinit var btnOption2: MaterialButton
    private lateinit var btnOption3: MaterialButton
    private lateinit var btnOption4: MaterialButton
    private lateinit var btnSubmit: MaterialButton

    // Legacy text input fields (for backward compatibility)
    private var etAnswer1: EditText? = null
    private var etAnswer2: EditText? = null

    // Navigation drawer items
    private lateinit var navHome: TextView
    private lateinit var navLanguage: TextView
    private lateinit var navWords: TextView
    private lateinit var navPhrases: TextView
    private lateinit var navQuotes: TextView
    private lateinit var navProgress: TextView
    private lateinit var navVisibility: TextView
    private lateinit var navSettings: TextView
    private lateinit var navProfile: TextView
    private lateinit var navBack: ImageView
    private lateinit var navChat: ImageView
    private lateinit var navDictionary: ImageView

    // Single word mode variables
    private var englishWord: String = ""
    private var afrikaansWord: String = ""
    private var imageResource: Int = 0
    private var category: String = ""

    // Test mode variables
    private var isTestMode: Boolean = false
    private var wordList: ArrayList<WordItem>? = null
    private var currentWordIndex: Int = 0
    private var totalWords: Int = 0
    private var correctAnswers: Int = 0

    // Multiple choice variables
    private var selectedAnswer: String = ""
    private var correctAnswer: String = ""
    private val multipleChoiceOptions = mutableListOf<String>()
    private var hasAnswered: Boolean = false

    // Test tracking variables
    private var testStartTime: Long = 0
    private var testId: String = ""

    companion object {
        private const val TAG = "WordDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        // Fetch the userId from user collection
        fetchUserCollectionUserId()

        // Check if this is test mode to determine which layout to use
        isTestMode = intent.getBooleanExtra("TEST_MODE", false)

        if (isTestMode) {
            setContentView(R.layout.activity_word_detail)
            // Generate unique test ID for this test session
            testId = UUID.randomUUID().toString()
            testStartTime = System.currentTimeMillis()
        } else {
            setContentView(R.layout.activity_word_detail) // Original layout for single word mode
        }

        // Get data from intent
        extractIntentData()

        initializeViews()
        setupClickListeners()
        setupNavigationDrawer()

        if (isTestMode) {
            setupTestMode()
        } else {
            setupSingleWordMode()
        }
    }

    // FIXED METHOD: Fetch userId with case-insensitive email comparison
    private fun fetchUserCollectionUserId() {
        val authUserId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email?.lowercase() // Normalize to lowercase

        if (authUserId == null || userEmail == null) {
            Log.w(TAG, "No authenticated user or email")
            return
        }

        Log.d(TAG, "Fetching user collection userId for authUserId: $authUserId, email: $userEmail")

        // First, try to find document with exact lowercase email match
        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.first()
                    userCollectionUserId = userDoc.id
                    Log.d(TAG, "✅ Found user with exact email match: $userCollectionUserId")
                } else {
                    // If no exact match, try case-insensitive search by fetching all users and comparing
                    Log.d(TAG, "No exact email match found, trying case-insensitive search...")
                    findUserByCaseInsensitiveEmail(authUserId, userEmail)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "❌ Error in initial email query", e)
                // Fallback to case-insensitive search
                findUserByCaseInsensitiveEmail(authUserId, userEmail)
            }
    }

    // NEW METHOD: Case-insensitive email search
    private fun findUserByCaseInsensitiveEmail(authUserId: String, userEmail: String) {
        Log.d(TAG, "Performing case-insensitive email search for: $userEmail")

        firestore.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                var foundUser = false

                for (document in querySnapshot.documents) {
                    val docEmail = document.getString("email")?.lowercase()
                    if (docEmail == userEmail) {
                        userCollectionUserId = document.id
                        foundUser = true
                        Log.d(TAG, "✅ Found user with case-insensitive match: $userCollectionUserId")
                        Log.d(TAG, "Original email in doc: ${document.getString("email")}")

                        // Update the document to have lowercase email for future consistency
                        updateEmailToLowercase(document.id, userEmail)
                        break
                    }
                }

                if (!foundUser) {
                    Log.w(TAG, "❌ No user found with email: $userEmail")
                    createUserDocumentInFirestore(authUserId, userEmail)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "❌ Error in case-insensitive search", e)
                createUserDocumentInFirestore(authUserId, userEmail)
            }
    }

    // NEW METHOD: Update existing document to have lowercase email
    private fun updateEmailToLowercase(documentId: String, lowercaseEmail: String) {
        firestore.collection("users")
            .document(documentId)
            .update("email", lowercaseEmail)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Updated email to lowercase in document: $documentId")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "⚠️ Could not update email to lowercase", e)
                // This is not critical, the user is still found
            }
    }

    // UPDATED METHOD: Create user document with lowercase email
    private fun createUserDocumentInFirestore(authUserId: String, userEmail: String) {
        Log.d(TAG, "Creating new user document for: $userEmail")

        val userData = hashMapOf(
            "email" to userEmail.lowercase(), // Ensure lowercase
            "authUserId" to authUserId,
            "createdAt" to System.currentTimeMillis()
            // Add other user fields as needed
        )

        firestore.collection("users")
            .add(userData)
            .addOnSuccessListener { documentReference ->
                userCollectionUserId = documentReference.id
                Log.d(TAG, "✅ Created new user document with ID: $userCollectionUserId")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "❌ Error creating user document", e)
                // As absolute fallback, could use hardcoded ID but better to handle properly
                userCollectionUserId = "wIXIVzQuLR584L1t3xQ3" // Only as last resort
                Log.d(TAG, "Using fallback user ID: $userCollectionUserId")
            }
    }

    private fun extractIntentData() {
        category = intent.getStringExtra("CATEGORY") ?: "Emotions"

        if (isTestMode) {
            // Test mode: get word list and progress info
            wordList = intent.getSerializableExtra("WORD_LIST") as? ArrayList<WordItem>
            currentWordIndex = intent.getIntExtra("CURRENT_WORD_INDEX", 0)
            totalWords = intent.getIntExtra("TOTAL_WORDS", 0)
            correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)

            println("Test mode activated. Total words: $totalWords, Current index: $currentWordIndex")
        } else {
            // Single word mode: get individual word data
            englishWord = intent.getStringExtra("ENGLISH_WORD") ?: ""
            afrikaansWord = intent.getStringExtra("AFRIKAANS_WORD") ?: ""
            imageResource = intent.getIntExtra("IMAGE_RESOURCE", R.drawable.rhino_happy)

            println("Single word mode: $englishWord = $afrikaansWord")
        }
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)
        tvWordsTitle = findViewById(R.id.tv_words_title)
        tvCategorySubtitle = findViewById(R.id.tv_category_subtitle)
        tvWordEnglish = findViewById(R.id.tv_word_english)
        imgWord = findViewById(R.id.img_word)
        btnSound = findViewById(R.id.btn_sound)
        btnSubmit = findViewById(R.id.btn_submit)

        // Multiple choice buttons (always present in the layout)
        btnOption1 = findViewById(R.id.btn_option1)
        btnOption2 = findViewById(R.id.btn_option2)
        btnOption3 = findViewById(R.id.btn_option3)
        btnOption4 = findViewById(R.id.btn_option4)

        // Navigation drawer items
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

        // Optional navigation items that may not be present in all layouts
        try {
            navQuotes = findViewById(R.id.nav_quotes)
        } catch (e: Exception) {
            Log.d(TAG, "nav_quotes not found in layout")
        }

        try {
            navDictionary = findViewById(R.id.nav_dictionary)
        } catch (e: Exception) {
            Log.d(TAG, "nav_dictionary not found in layout")
        }
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        btnSound.setOnClickListener {
            if (isTestMode) {
                // Hide sound button in test mode to prevent giving away answers
                Toast.makeText(this, "Sound disabled during test", Toast.LENGTH_SHORT).show()
            } else {
                // Only allow sound in single word mode
                if (afrikaansWord.isNotEmpty()) {
                    speakWord(afrikaansWord)
                } else {
                    Toast.makeText(this, "No word to pronounce", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (isTestMode) {
            // Multiple choice click listeners
            btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString(), btnOption1) }
            btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString(), btnOption2) }
            btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString(), btnOption3) }
            btnOption4.setOnClickListener { selectAnswer(btnOption4.text.toString(), btnOption4) }

            btnSubmit.setOnClickListener { checkMultipleChoiceAnswer() }
        } else {
            btnSubmit.setOnClickListener { checkSingleWordAnswer() }
        }
    }

    // Method to speak Afrikaans word using Google Translate TTS API
    private fun speakWord(text: String) {
        try {
            // Show loading indicator
            Toast.makeText(this, "Loading pronunciation...", Toast.LENGTH_SHORT).show()

            // Google Translate TTS URL
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val ttsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&tl=af&client=tw-ob&q=$encodedText"

            val request = Request.Builder()
                .url(ttsUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this@WordDetailActivity, "Pronunciation unavailable", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.let { body ->
                            try {
                                // Release previous MediaPlayer if exists
                                mediaPlayer?.release()

                                // Create temporary file
                                val tempFile = createTempFile("tts_audio", ".mp3", cacheDir)
                                tempFile.writeBytes(body.bytes())

                                Handler(Looper.getMainLooper()).post {
                                    playAudioFile(tempFile.absolutePath)
                                }

                            } catch (e: Exception) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(this@WordDetailActivity, "Audio playback error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this@WordDetailActivity, "Pronunciation service unavailable", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to play the downloaded audio file
    private fun playAudioFile(filePath: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener { mp ->
                    mp.start()
                    Toast.makeText(this@WordDetailActivity, "Playing pronunciation", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@WordDetailActivity, "Audio playback failed", Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Media player error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigationDrawer() {
        // Navigation drawer item listeners - matching HomeActivity functionality
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

        // Bottom navigation icons
        navBack.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            onBackPressed()
        }

        navChat.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }

        // Optional navigation items (if present)
        try {
            navQuotes.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                navigateToQuotes()
            }
        } catch (e: Exception) {
            Log.d(TAG, "nav_quotes click listener not set - view not found")
        }

        try {
            navDictionary.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.END)
                navigateToOfflineQuiz()
            }
        } catch (e: Exception) {
            Log.d(TAG, "nav_dictionary click listener not set - view not found")
        }
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

    private fun navigateToOfflineQuiz() {
        val intent = Intent(this, OfflineActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToQuotes() {
        val intent = Intent(this, QuotesActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun selectAnswer(answer: String, selectedButton: MaterialButton) {
        if (hasAnswered) return // Prevent changing selection after answering

        selectedAnswer = answer

        // Reset all buttons to default state
        resetButtonStates()

        // Highlight selected button
        selectedButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(android.R.color.holo_blue_light, null)
        )
        selectedButton.setTextColor(resources.getColor(android.R.color.white, null))

        // Show submit button
        btnSubmit.visibility = android.view.View.VISIBLE
    }

    private fun resetButtonStates() {
        val defaultColor = resources.getColor(android.R.color.white, null)
        val defaultTextColor = resources.getColor(R.color.mz_navy_dark, null)

        btnOption1.backgroundTintList = android.content.res.ColorStateList.valueOf(defaultColor)
        btnOption2.backgroundTintList = android.content.res.ColorStateList.valueOf(defaultColor)
        btnOption3.backgroundTintList = android.content.res.ColorStateList.valueOf(defaultColor)
        btnOption4.backgroundTintList = android.content.res.ColorStateList.valueOf(defaultColor)

        btnOption1.setTextColor(defaultTextColor)
        btnOption2.setTextColor(defaultTextColor)
        btnOption3.setTextColor(defaultTextColor)
        btnOption4.setTextColor(defaultTextColor)
    }

    private fun setupTestMode() {
        if (wordList == null || wordList!!.isEmpty()) {
            Toast.makeText(this, "Error: No words to test", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        tvWordsTitle.text = "TEST MODE / TOETS MODUS"
        loadCurrentTestWord()
    }

    private fun setupSingleWordMode() {
        // Hide multiple choice buttons for single word mode
        btnOption1.visibility = android.view.View.GONE
        btnOption2.visibility = android.view.View.GONE
        btnOption3.visibility = android.view.View.GONE
        btnOption4.visibility = android.view.View.GONE

        setupWordDisplay(englishWord, afrikaansWord, imageResource)

        // Show submit button for single word mode
        btnSubmit.visibility = android.view.View.VISIBLE
        btnSubmit.text = "Check Answer"
    }

    private fun loadCurrentTestWord() {
        if (wordList != null && currentWordIndex < wordList!!.size) {
            val currentWord = wordList!![currentWordIndex]
            englishWord = currentWord.english
            afrikaansWord = currentWord.afrikaans
            imageResource = currentWord.imageResource

            setupWordDisplay(englishWord, afrikaansWord, imageResource)
            generateMultipleChoiceOptions()
            hasAnswered = false
            println("Loaded word ${currentWordIndex + 1}/$totalWords: $englishWord = $afrikaansWord")
        }
    }

    private fun setupWordDisplay(english: String, afrikaans: String, imgRes: Int) {
        val categoryAfrikaans = when (category.lowercase()) {
            "emotions" -> "Emosies"
            "animals" -> "Diere"
            "colors", "colours" -> "Kleure"
            "food" -> "Kos"
            else -> "Emosies"
        }

        tvCategorySubtitle.text = "$category / $categoryAfrikaans"
        tvWordEnglish.text = english
        imgWord.setImageResource(imgRes)
        correctAnswer = afrikaans
    }

    private fun generateMultipleChoiceOptions() {
        if (!isTestMode || wordList == null) return

        multipleChoiceOptions.clear()
        multipleChoiceOptions.add(correctAnswer) // Add correct answer

        // Add 3 random wrong answers from the word list
        val wrongAnswers = wordList!!
            .filter { it.afrikaans != correctAnswer }
            .map { it.afrikaans }
            .shuffled()
            .take(3)

        multipleChoiceOptions.addAll(wrongAnswers)
        multipleChoiceOptions.shuffle() // Randomize the order

        // Set button texts
        if (multipleChoiceOptions.size >= 4) {
            btnOption1.text = multipleChoiceOptions[0]
            btnOption2.text = multipleChoiceOptions[1]
            btnOption3.text = multipleChoiceOptions[2]
            btnOption4.text = multipleChoiceOptions[3]
        }

        // Reset button states and hide submit button
        resetButtonStates()
        btnSubmit.visibility = android.view.View.GONE
        selectedAnswer = ""
    }

    private fun checkMultipleChoiceAnswer() {
        if (selectedAnswer.isEmpty() || hasAnswered) return

        hasAnswered = true
        val isCorrect = selectedAnswer == correctAnswer

        // Show correct answer and user's choice
        showAnswerFeedback(isCorrect)

        if (isCorrect) {
            correctAnswers++
        }

        // Update submit button to "Next" or "Finish"
        btnSubmit.text = if (currentWordIndex < totalWords - 1) "Next" else "Finish Test"
        btnSubmit.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(android.R.color.holo_blue_dark, null)
        )

        btnSubmit.setOnClickListener {
            if (currentWordIndex < totalWords - 1) {
                moveToNextWord()
            } else {
                finishTest()
            }
        }
    }

    private fun checkSingleWordAnswer() {
        // For single word mode, just show the correct answer
        // Since we don't have text input fields in this layout
        val message = "The answer is: $englishWord = $afrikaansWord"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Track single word as spoken for progress
        ProgressActivity.updateUserProgress(this, wordsSpokenFromTest = 1, phrasesSpokenFromTest = 0)
    }

    private fun showAnswerFeedback(isCorrect: Boolean) {
        // Highlight correct and incorrect answers
        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)

        buttons.forEach { button ->
            when {
                button.text.toString() == correctAnswer -> {
                    // Highlight correct answer in green
                    button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        resources.getColor(android.R.color.holo_green_light, null)
                    )
                    button.setTextColor(resources.getColor(android.R.color.white, null))
                }
                button.text.toString() == selectedAnswer && !isCorrect -> {
                    // Highlight incorrect selection in red
                    button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        resources.getColor(android.R.color.holo_red_light, null)
                    )
                    button.setTextColor(resources.getColor(android.R.color.white, null))
                }
            }
        }

        val feedbackMessage = if (isCorrect) {
            "Correct! Well done!"
        } else {
            "Incorrect. The correct answer is: $correctAnswer"
        }

        Toast.makeText(this, feedbackMessage, Toast.LENGTH_SHORT).show()
    }

    private fun moveToNextWord() {
        currentWordIndex++
        loadCurrentTestWord()

        // Reset submit button
        btnSubmit.text = "Submit"
        btnSubmit.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(android.R.color.holo_green_light, null)
        )
        btnSubmit.setOnClickListener { checkMultipleChoiceAnswer() }
    }

    private fun finishTest() {
        // Show final score
        val percentage = (correctAnswers.toFloat() / totalWords * 100).toInt()
        val scoreMessage = "Test Complete!\nScore: $correctAnswers/$totalWords ($percentage%)"

        Toast.makeText(this, scoreMessage, Toast.LENGTH_LONG).show()

        // Save test results to Firebase
        saveTestResultsToFirebase()

        // Update progress with the number of words completed in the test
        // Only count correct answers as "words spoken"
        ProgressActivity.updateUserProgress(this, wordsSpokenFromTest = correctAnswers, phrasesSpokenFromTest = 0)

        // Navigate to home page
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // IMPROVED METHOD: Save test results with proper userId handling
    private fun saveTestResultsToFirebase() {
        // Wait for userCollectionUserId to be fetched before saving
        if (userCollectionUserId == null) {
            Log.w(TAG, "⚠️ userCollectionUserId not ready, retrying in 1 second...")
            Handler(Looper.getMainLooper()).postDelayed({
                saveTestResultsToFirebase()
            }, 1000)
            return
        }

        val testEndTime = System.currentTimeMillis()
        val testDuration = testEndTime - testStartTime

        val testResultData = hashMapOf(
            "testId" to testId,
            "userId" to userCollectionUserId!!, // Use the Firestore users collection document ID
            "authUserId" to currentUserId, // Also store the Firebase Auth ID for reference
            "testType" to "WORD_TEST",
            "category" to category,
            "totalQuestions" to totalWords,
            "correctAnswers" to correctAnswers,
            "incorrectAnswers" to (totalWords - correctAnswers),
            "scorePercentage" to ((correctAnswers.toFloat() / totalWords * 100).toInt()),
            "testDuration" to testDuration,
            "startTime" to testStartTime,
            "endTime" to testEndTime,
            "timestamp" to System.currentTimeMillis()
        )

        Log.d(TAG, "💾 Saving test result with:")
        Log.d(TAG, "- Firestore users collection ID (userId): $userCollectionUserId")
        Log.d(TAG, "- Firebase Auth ID (authUserId): $currentUserId")

        firestore.collection("test_results")
            .document(testId)
            .set(testResultData)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Test results saved successfully!")
                Log.d(TAG, "Score: $correctAnswers/$totalWords correct")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "❌ Error saving test results", e)
            }
    }

    // Deprecated method - replaced by specific navigation methods
    @Deprecated("Use specific navigation methods instead")
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
        closeDrawer()
    }

    private fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.END)
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
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

// Data class for word items (if not already defined elsewhere)
data class WordItem(
    val english: String,
    val afrikaans: String,
    val imageResource: Int
) : java.io.Serializable