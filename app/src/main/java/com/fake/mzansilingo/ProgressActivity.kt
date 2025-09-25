package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class ProgressActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null

    // UI Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView

    // Progress UI Elements
    private lateinit var tvWordsSpoken: TextView
    private lateinit var tvPhrasesSpoken: TextView
    private lateinit var tvDaysPracticed: TextView

    // Weekly progress circles
    private lateinit var circleDay1: ImageView
    private lateinit var circleDay2: ImageView
    private lateinit var circleDay3: ImageView
    private lateinit var circleDay4: ImageView
    private lateinit var circleDay5: ImageView
    private lateinit var circleDay6: ImageView
    private lateinit var circleDay7: ImageView

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

    // Bottom navigation elements
    private lateinit var btnBottomDict: ImageView
    private lateinit var btnBottomQuotes: ImageView
    private lateinit var btnBottomStats: ImageView

    // Progress data
    private var wordsSpoken = 0
    private var phrasesSpoken = 0
    private var daysPracticed = 0
    private var weeklyProgress = BooleanArray(7) // Mon-Sun
    private var currentWeekStart = ""

    companion object {
        private const val TAG = "ProgressActivity"
        private const val COLLECTION_USER_PROGRESS = "user_progress"

        // Static method to update progress from test activities
        fun updateUserProgress(
            context: android.content.Context,
            wordsSpokenFromTest: Int = 0,
            phrasesSpokenFromTest: Int = 0
        ) {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val userId = auth.currentUser?.uid ?: return

            firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val currentWords = document.getLong("wordsSpoken")?.toInt() ?: 0
                    val currentPhrases = document.getLong("phrasesSpoken")?.toInt() ?: 0

                    val progressData = hashMapOf(
                        "userId" to userId,
                        "wordsSpoken" to (currentWords + wordsSpokenFromTest),
                        "phrasesSpoken" to (currentPhrases + phrasesSpokenFromTest),
                        "lastUpdated" to System.currentTimeMillis()
                    )

                    firestore.collection(COLLECTION_USER_PROGRESS)
                        .document(userId)
                        .set(progressData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Progress updated: +$wordsSpokenFromTest words, +$phrasesSpokenFromTest phrases")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating progress", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error getting current progress", e)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            // User not authenticated, redirect to login
            redirectToLogin()
            return
        }

        initializeViews()
        trackDailyLogin()
        loadProgressData()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Main layout
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)

        // Progress stats
        tvWordsSpoken = findViewById(R.id.tv_words_spoken)
        tvPhrasesSpoken = findViewById(R.id.tv_phrases_spoken)
        tvDaysPracticed = findViewById(R.id.tv_days_practiced)

        // Weekly progress circles
        circleDay1 = findViewById(R.id.circle_day_1)
        circleDay2 = findViewById(R.id.circle_day_2)
        circleDay3 = findViewById(R.id.circle_day_3)
        circleDay4 = findViewById(R.id.circle_day_4)
        circleDay5 = findViewById(R.id.circle_day_5)
        circleDay6 = findViewById(R.id.circle_day_6)
        circleDay7 = findViewById(R.id.circle_day_7)

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

        // Bottom navigation
        btnBottomDict = findViewById(R.id.btn_bottom_dict)
        btnBottomQuotes = findViewById(R.id.btn_bottom_quotes)
        btnBottomStats = findViewById(R.id.btn_bottom_stats)
    }

    private fun setupClickListeners() {
        // Menu toggle
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Navigation drawer click listeners - matching other activities
        navHome.setOnClickListener {
            closeDrawer()
            navigateToHomeActivity()
        }

        navLanguage.setOnClickListener {
            closeDrawer()
            navigateToLanguageSelection()
        }

        navWords.setOnClickListener {
            closeDrawer()
            navigateToWordsActivity()
        }

        navPhrases.setOnClickListener {
            closeDrawer()
            navigateToPhrasesActivity()
        }

        navProgress.setOnClickListener {
            // Already in Progress activity, just close drawer
            closeDrawer()
        }



        navSettings.setOnClickListener {
            closeDrawer()
            navigateToSettings()
        }

        navProfile.setOnClickListener {
            closeDrawer()
            navigateToProfile()
        }

        navBack.setOnClickListener {
            closeDrawer()
            onBackPressed()
        }

        navChat.setOnClickListener {
            closeDrawer()
            navigateToAiChatActivity()
        }

        navDictionary.setOnClickListener {
            closeDrawer()
            navigateToOfflineQuiz()
        }

        // Bottom navigation
        btnBottomDict.setOnClickListener { navigateToOfflineQuiz() }
        btnBottomQuotes.setOnClickListener { navigateToQuotes() }
        btnBottomStats.setOnClickListener {
            // Already in Progress/Stats activity, do nothing or refresh
            Toast.makeText(this, "Already viewing statistics", Toast.LENGTH_SHORT).show()
        }
    }

    // Navigation methods matching other activities
    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
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
            Log.e(TAG, "Navigation error", e)
        }
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun trackDailyLogin() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()

        // Get Monday of current week
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        currentWeekStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Track login for today
        currentUserId?.let { userId ->
            val loginData = hashMapOf(
                "userId" to userId,
                "date" to today,
                "weekStart" to currentWeekStart,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("daily_logins")
                .document("${userId}_$today")
                .set(loginData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Daily login tracked for $today")
                    markTodayAsCompleted()
                    // Reload total days practiced after tracking today's login
                    loadTotalDaysPracticed()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error tracking daily login", e)
                }
        }
    }

    private fun markTodayAsCompleted() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> -1
        }

        if (dayOfWeek >= 0) {
            markDayCompleted(dayOfWeek)
        }
    }

    private fun loadProgressData() {
        currentUserId?.let { userId ->
            // Load test results first to get actual totals
            loadTestResultsSimple()

            // Load total days practiced (separate from weekly progress)
            loadTotalDaysPracticed()

            // Then load weekly progress for the circles display
            loadWeeklyProgress()
        }
    }

    // NEW METHOD: Load total days practiced across all time
    private fun loadTotalDaysPracticed() {
        currentUserId?.let { userId ->
            firestore.collection("daily_logins")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    // Use a Set to ensure unique dates only
                    val uniqueDates = mutableSetOf<String>()

                    for (document in documents) {
                        val loginDate = document.getString("date")
                        if (!loginDate.isNullOrEmpty()) {
                            uniqueDates.add(loginDate)
                        }
                    }

                    daysPracticed = uniqueDates.size

                    Log.d(TAG, "Total days practiced loaded: $daysPracticed")
                    updateProgressDisplay()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error loading total days practiced", e)
                    updateProgressDisplay()
                }
        }
    }

    // UPDATED METHOD: Load test results from Firebase
    private fun loadTestResultsSimple() {
        currentUserId?.let { userId ->
            firestore.collection("test_results")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    var wordCount = 0
                    var phraseCount = 0

                    for (doc in documents) {
                        val testType = doc.getString("testType") ?: ""
                        val correct = doc.getLong("correctAnswers")?.toInt() ?: 0

                        if (testType == "WORD_TEST") {
                            wordCount += correct
                        } else if (testType == "PHRASE_TEST") {
                            phraseCount += correct
                        }
                    }

                    // Update the totals
                    wordsSpoken = wordCount
                    phrasesSpoken = phraseCount

                    Log.d(TAG, "Test results loaded: words=$wordsSpoken, phrases=$phrasesSpoken")

                    // Update display
                    updateProgressDisplay()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error loading test results", e)
                    // Fall back to saved progress if test results fail to load
                    loadSavedProgress()
                }
        }
    }

    // Fallback method to load saved progress
    private fun loadSavedProgress() {
        currentUserId?.let { userId ->
            firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        wordsSpoken = document.getLong("wordsSpoken")?.toInt() ?: 0
                        phrasesSpoken = document.getLong("phrasesSpoken")?.toInt() ?: 0
                        // Don't load daysPracticed from here - it's loaded separately

                        Log.d(TAG, "Saved progress loaded: words=$wordsSpoken, phrases=$phrasesSpoken")
                    }
                    updateProgressDisplay()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error loading saved progress", e)
                    updateProgressDisplay()
                }
        }
    }

    private fun loadWeeklyProgress() {
        currentUserId?.let { userId ->
            // Load this week's daily logins ONLY for the weekly circles display
            firestore.collection("daily_logins")
                .whereEqualTo("userId", userId)
                .whereEqualTo("weekStart", currentWeekStart)
                .get()
                .addOnSuccessListener { documents ->
                    // Reset weekly progress (for circles display only)
                    weeklyProgress = BooleanArray(7)

                    val calendar = Calendar.getInstance()
                    calendar.firstDayOfWeek = Calendar.MONDAY

                    for (document in documents) {
                        val loginDate = document.getString("date")
                        loginDate?.let { date ->
                            try {
                                val loginCalendar = Calendar.getInstance()
                                val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                                if (parsedDate != null) {
                                    loginCalendar.time = parsedDate
                                } else {
                                    loginCalendar.time = Date()
                                }
                                loginCalendar.firstDayOfWeek = Calendar.MONDAY

                                val dayOfWeek = when (loginCalendar.get(Calendar.DAY_OF_WEEK)) {
                                    Calendar.MONDAY -> 0
                                    Calendar.TUESDAY -> 1
                                    Calendar.WEDNESDAY -> 2
                                    Calendar.THURSDAY -> 3
                                    Calendar.FRIDAY -> 4
                                    Calendar.SATURDAY -> 5
                                    Calendar.SUNDAY -> 6
                                    else -> -1
                                }

                                if (dayOfWeek >= 0) {
                                    weeklyProgress[dayOfWeek] = true
                                } else {

                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing login date: $date", e)
                            }
                        }
                    }

                    // DON'T update daysPracticed here - it's handled separately
                    updateProgressDisplay()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error loading weekly progress", e)
                    updateProgressDisplay()
                }
        }
    }

    private fun updateProgressDisplay() {
        runOnUiThread {
            // Update stats text
            tvWordsSpoken.text = "$wordsSpoken WORDS RIGHT"
            tvPhrasesSpoken.text = "$phrasesSpoken PHRASES RIGHT"
            tvDaysPracticed.text = "$daysPracticed DAYS PRACTICE"

            // Update weekly progress circles
            val circles = arrayOf(circleDay1, circleDay2, circleDay3, circleDay4, circleDay5, circleDay6, circleDay7)

            for (i in circles.indices) {
                if (weeklyProgress[i]) {
                    circles[i].setImageResource(R.drawable.ic_check_circle)
                    circles[i].clearColorFilter() // Remove any color filter to show drawable as-is
                } else {
                    circles[i].setImageResource(R.drawable.ic_circle_outline)
                    circles[i].setColorFilter(resources.getColor(android.R.color.white, theme))
                }
            }
        }
    }

    // Progress tracking methods - called from other activities
    fun updateWordsSpoken(newWords: Int) {
        wordsSpoken += newWords
        saveProgressToFirebase()
        updateProgressDisplay()
    }

    fun updatePhrasesSpoken(newPhrases: Int) {
        phrasesSpoken += newPhrases
        saveProgressToFirebase()
        updateProgressDisplay()
    }

    // UPDATED: markDayCompleted method
    fun markDayCompleted(dayIndex: Int) {
        if (dayIndex in 0..6 && !weeklyProgress[dayIndex]) {
            weeklyProgress[dayIndex] = true
            // Don't update daysPracticed here - it will be updated by loadTotalDaysPracticed
            // when trackDailyLogin runs
            saveProgressToFirebase()
            updateProgressDisplay()
        }
    }

    // UPDATED: saveProgressToFirebase method
    private fun saveProgressToFirebase() {
        currentUserId?.let { userId ->
            val progressData = hashMapOf(
                "userId" to userId,
                "wordsSpoken" to wordsSpoken,
                "phrasesSpoken" to phrasesSpoken,
                "daysPracticed" to daysPracticed, // This is now total days, not weekly
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .set(progressData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Progress saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving progress", e)
                    Toast.makeText(this, "Failed to save progress", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun redirectToLogin() {
        // Redirect to your login activity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    // Public method to get current progress for other activities
    fun getCurrentProgress(): ProgressData {
        return ProgressData(wordsSpoken, phrasesSpoken, daysPracticed, weeklyProgress.clone())
    }
}

// Data class to hold progress information (updated without categoriesCompleted)
data class ProgressData(
    val wordsSpoken: Int,
    val phrasesSpoken: Int,
    val daysPracticed: Int,
    val weeklyProgress: BooleanArray
)