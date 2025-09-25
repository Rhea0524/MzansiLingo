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

class PhrasesDetailActivity : AppCompatActivity() {

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
    private lateinit var tvPhrasesTitle: TextView
    private lateinit var tvCategorySubtitle: TextView
    private lateinit var tvPhraseEnglish: TextView
    private lateinit var tvPhraseAfrikaans: TextView
    private lateinit var btnSound: ImageView

    // Multiple choice buttons for test mode
    private lateinit var btnOption1: MaterialButton
    private lateinit var btnOption2: MaterialButton
    private lateinit var btnOption3: MaterialButton
    private lateinit var btnOption4: MaterialButton
    private lateinit var btnSubmit: MaterialButton

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

    // Single phrase mode variables
    private var englishPhrase: String = ""
    private var afrikaansPhrase: String = ""
    private var category: String = ""

    // Test mode variables
    private var isTestMode: Boolean = false
    private var phraseList: ArrayList<PhraseItem>? = null
    private var currentPhraseIndex: Int = 0
    private var totalPhrases: Int = 0
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
        private const val TAG = "PhrasesDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        // Fetch the userId from user collection
        fetchUserCollectionUserId()

        // Check if this is test mode to determine behavior
        isTestMode = intent.getBooleanExtra("TEST_MODE", false)

        if (isTestMode) {
            // Generate unique test ID for this test session
            testId = UUID.randomUUID().toString()
            testStartTime = System.currentTimeMillis()
        }

        // Use the same layout but modify behavior based on mode
        setContentView(R.layout.activity_phrase_detail)

        // Get data from intent
        extractIntentData()

        initializeViews()
        setupDrawer()
        setupClickListeners()

        if (isTestMode) {
            setupTestMode()
        } else {
            setupSinglePhraseMode()
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
        category = intent.getStringExtra("CATEGORY") ?: "Communication"

        if (isTestMode) {
            // Test mode: get phrase list and progress info
            phraseList = intent.getSerializableExtra("PHRASE_LIST") as? ArrayList<PhraseItem>
            currentPhraseIndex = intent.getIntExtra("CURRENT_PHRASE_INDEX", 0)
            totalPhrases = intent.getIntExtra("TOTAL_PHRASES", 0)
            correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)

            Log.d(TAG, "Test mode activated. Total phrases: $totalPhrases, Current index: $currentPhraseIndex")
        } else {
            // Single phrase mode: get individual phrase data
            englishPhrase = intent.getStringExtra("ENGLISH_PHRASE") ?: ""
            afrikaansPhrase = intent.getStringExtra("AFRIKAANS_PHRASE") ?: ""

            Log.d(TAG, "Single phrase mode: $englishPhrase = $afrikaansPhrase")
        }
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)
        tvPhrasesTitle = findViewById(R.id.tv_phrases_title)
        tvCategorySubtitle = findViewById(R.id.tv_category_subtitle)
        tvPhraseEnglish = findViewById(R.id.tv_phrase_english)
        tvPhraseAfrikaans = findViewById(R.id.tv_phrase_afrikaans)
        btnSound = findViewById(R.id.btn_sound)

        // Multiple choice buttons (only visible in test mode)
        btnOption1 = findViewById(R.id.btn_option1)
        btnOption2 = findViewById(R.id.btn_option2)
        btnOption3 = findViewById(R.id.btn_option3)
        btnOption4 = findViewById(R.id.btn_option4)
        btnSubmit = findViewById(R.id.btn_submit)

        // Navigation drawer items
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
        navHome.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToActivity(HomeActivity::class.java)
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

    private fun setupClickListeners() {
        btnSound.setOnClickListener {
            if (isTestMode) {
                // Hide sound button in test mode to prevent giving away answers
                Toast.makeText(this, "Sound disabled during test", Toast.LENGTH_SHORT).show()
            } else {
                // Only allow sound in single phrase mode
                if (afrikaansPhrase.isNotEmpty()) {
                    speakPhrase(afrikaansPhrase)
                } else {
                    Toast.makeText(this, "No phrase to pronounce", Toast.LENGTH_SHORT).show()
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
            btnSubmit.setOnClickListener { checkSinglePhraseAnswer() }
        }
    }

    // Method to speak Afrikaans phrase using Google Translate TTS API
    private fun speakPhrase(text: String) {
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
                        Toast.makeText(this@PhrasesDetailActivity, "Pronunciation unavailable", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this@PhrasesDetailActivity, "Audio playback error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this@PhrasesDetailActivity, "Pronunciation service unavailable", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@PhrasesDetailActivity, "Playing pronunciation", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@PhrasesDetailActivity, "Audio playback failed", Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Media player error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
        if (phraseList == null || phraseList!!.isEmpty()) {
            Toast.makeText(this, "Error: No phrases to test", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        tvPhrasesTitle.text = "PHRASE TEST / FRASE TOETS"

        // Show multiple choice buttons for test mode
        btnOption1.visibility = android.view.View.VISIBLE
        btnOption2.visibility = android.view.View.VISIBLE
        btnOption3.visibility = android.view.View.VISIBLE
        btnOption4.visibility = android.view.View.VISIBLE

        loadCurrentTestPhrase()
    }

    private fun setupSinglePhraseMode() {
        // Hide multiple choice buttons for single phrase mode
        btnOption1.visibility = android.view.View.GONE
        btnOption2.visibility = android.view.View.GONE
        btnOption3.visibility = android.view.View.GONE
        btnOption4.visibility = android.view.View.GONE

        setupPhraseDisplay(englishPhrase, afrikaansPhrase)

        // Show submit button for single phrase mode with different text
        btnSubmit.visibility = android.view.View.VISIBLE
        btnSubmit.text = "Practice Phrase"
    }

    private fun loadCurrentTestPhrase() {
        if (phraseList != null && currentPhraseIndex < phraseList!!.size) {
            val currentPhrase = phraseList!![currentPhraseIndex]
            englishPhrase = currentPhrase.english
            afrikaansPhrase = currentPhrase.afrikaans

            setupPhraseDisplay(englishPhrase, afrikaansPhrase)
            generateMultipleChoiceOptions()
            hasAnswered = false
            Log.d(TAG, "Loaded phrase ${currentPhraseIndex + 1}/$totalPhrases: $englishPhrase = $afrikaansPhrase")
        }
    }

    private fun setupPhraseDisplay(english: String, afrikaans: String) {
        val categoryAfrikaans = when (category.lowercase().replace(" ", "").replace("&", "")) {
            "communicationandinformation", "communication" -> "Kommunikasie & Inligting"
            "personalinformation", "personal" -> "Persoonlike Inligting"
            "traveldailyneeds", "travel" -> "Reis & Daaglikse Behoeftes"
            "politeandessential", "polite" -> "Beleefdheid & Noodsaaklike"
            else -> "Frases"
        }

        tvCategorySubtitle.text = "$category / $categoryAfrikaans"
        tvPhraseEnglish.text = english

        if (isTestMode) {
            // In test mode, hide the Afrikaans translation initially
            tvPhraseAfrikaans.visibility = android.view.View.GONE
        } else {
            // In single phrase mode, show both languages
            tvPhraseAfrikaans.text = afrikaans
            tvPhraseAfrikaans.visibility = android.view.View.VISIBLE
        }

        correctAnswer = afrikaans
    }

    private fun generateMultipleChoiceOptions() {
        if (!isTestMode || phraseList == null) return

        multipleChoiceOptions.clear()
        multipleChoiceOptions.add(correctAnswer) // Add correct answer

        // Add 3 random wrong answers from the phrase list
        val wrongAnswers = phraseList!!
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

        // Show the correct Afrikaans translation
        tvPhraseAfrikaans.text = correctAnswer
        tvPhraseAfrikaans.visibility = android.view.View.VISIBLE

        // Show answer feedback
        showAnswerFeedback(isCorrect)

        if (isCorrect) {
            correctAnswers++
        }

        // Update submit button to "Next" or "Finish"
        btnSubmit.text = if (currentPhraseIndex < totalPhrases - 1) "Next" else "Finish Test"
        btnSubmit.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(android.R.color.holo_blue_dark, null)
        )

        btnSubmit.setOnClickListener {
            if (currentPhraseIndex < totalPhrases - 1) {
                moveToNextPhrase()
            } else {
                finishTest()
            }
        }
    }

    private fun checkSinglePhraseAnswer() {
        // For single phrase mode, mark phrase as practiced
        val message = "Great! You practiced: $englishPhrase = $afrikaansPhrase"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Track single phrase as spoken for progress
        ProgressActivity.updateUserProgress(this, wordsSpokenFromTest = 0, phrasesSpokenFromTest = 1)
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

    private fun moveToNextPhrase() {
        currentPhraseIndex++
        loadCurrentTestPhrase()

        // Reset submit button
        btnSubmit.text = "Submit"
        btnSubmit.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(android.R.color.holo_green_light, null)
        )
        btnSubmit.setOnClickListener { checkMultipleChoiceAnswer() }
    }

    private fun finishTest() {
        // Show final score
        val percentage = (correctAnswers.toFloat() / totalPhrases * 100).toInt()
        val scoreMessage = "Phrase Test Complete!\nScore: $correctAnswers/$totalPhrases ($percentage%)"

        Toast.makeText(this, scoreMessage, Toast.LENGTH_LONG).show()

        // Save test results to Firebase
        saveTestResultsToFirebase()

        // Update progress with the number of phrases completed in the test
        // Only count correct answers as "phrases spoken"
        ProgressActivity.updateUserProgress(this, wordsSpokenFromTest = 0, phrasesSpokenFromTest = correctAnswers)

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
            "testType" to "PHRASE_TEST",
            "category" to category,
            "totalQuestions" to totalPhrases,
            "correctAnswers" to correctAnswers,
            "incorrectAnswers" to (totalPhrases - correctAnswers),
            "scorePercentage" to ((correctAnswers.toFloat() / totalPhrases * 100).toInt()),
            "testDuration" to testDuration, // Duration in milliseconds
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
                Log.d(TAG, "✅ Phrase test results saved successfully!")
                Log.d(TAG, "Score: $correctAnswers/$totalPhrases correct")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "❌ Error saving phrase test results", e)
                // Don't show error to user as they've already seen their score
            }
    }

    // Navigation methods - similar to HomeActivity
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
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