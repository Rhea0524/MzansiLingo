package com.fake.mzansilingo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView
    private lateinit var btnBack: ImageView
    private lateinit var tvLanguage: TextView
    private lateinit var tvTitle: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersList = mutableListOf<LeaderboardUser>()
    private var currentLanguage = "afrikaans"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Get language from intent
        currentLanguage = intent.getStringExtra("LANGUAGE") ?: "afrikaans"

        initializeViews()
        setupUI()
        setupRecyclerView()
        loadLeaderboardData()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recycler_leaderboard)
        progressBar = findViewById(R.id.progress_bar)
        tvNoData = findViewById(R.id.tv_no_data)
        btnBack = findViewById(R.id.btn_back)
        tvLanguage = findViewById(R.id.tv_language)
        tvTitle = findViewById(R.id.tv_title)
    }

    private fun setupUI() {
        // Set up language-specific UI
        when (currentLanguage.lowercase()) {
            "afrikaans" -> {
                tvLanguage.text = "AFRIKAANS"
                tvTitle.text = "LEADERBOARD / RANGLYS"
                tvNoData.text = "Geen data beskikbaar nie / No data available"
            }
            "spanish" -> {
                tvLanguage.text = "ESPAÑOL"
                tvTitle.text = "TABLA DE POSICIONES"
                tvNoData.text = "No hay datos disponibles"
            }
            else -> {
                tvLanguage.text = "ENGLISH"
                tvTitle.text = "LEADERBOARD"
                tvNoData.text = "No data available"
            }
        }

        // Back button click listener
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter(usersList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = leaderboardAdapter
    }

    private fun loadLeaderboardData() {
        showLoading(true)
        Log.d("LeaderboardActivity", "=== STARTING LEADERBOARD LOAD FROM USERS COLLECTION ===")

        usersList.clear()

        // Get all users from the users collection
        firestore.collection("users")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("LeaderboardActivity", "Found ${documents.size()} users in users collection")

                if (documents.isEmpty()) {
                    Log.d("LeaderboardActivity", "No users found in users collection!")
                    showLoading(false)
                    showNoData(true)
                    return@addOnSuccessListener
                }

                var processedUsers = 0
                val totalUsers = documents.size()

                // Process each user document
                documents.forEach { userDoc ->
                    // FIXED: Get userId correctly for both Google and email/password users
                    val firebaseAuthUid = getUserIdFromDocument(userDoc)
                    val username = getUsernameFromDocument(userDoc)
                    val email = userDoc.getString("email") ?: "unknown@app.com"
                    val fullName = userDoc.getString("fullName") ?: userDoc.getString("displayName") ?: username
                    val homeLanguage = userDoc.getString("homeLanguage") ?: "Unknown"
                    val provider = userDoc.getString("provider") ?: "email"

                    Log.d("LeaderboardActivity", "Processing user: $username (Doc ID: ${userDoc.id}, userId: $firebaseAuthUid, Provider: $provider, Email: $email)")

                    // Use the correct userId to query test_results
                    if (firebaseAuthUid != null) {
                        getUserProgressLikeProgressActivity(firebaseAuthUid, username, email, fullName, homeLanguage) { leaderboardUser ->
                            processedUsers++

                            if (leaderboardUser != null) {
                                usersList.add(leaderboardUser)
                                Log.d("LeaderboardActivity", "Added user: ${leaderboardUser.username} with score ${leaderboardUser.totalScore}")
                            }

                            // When all users are processed
                            if (processedUsers >= totalUsers) {
                                finishProcessing()
                            }
                        }
                    } else {
                        Log.w("LeaderboardActivity", "No valid userId found for user: $username")
                        // Still count this as processed even if no userId
                        processedUsers++
                        if (processedUsers >= totalUsers) {
                            finishProcessing()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LeaderboardActivity", "Error getting users collection", exception)
                showLoading(false)
                showNoData(true)
                Toast.makeText(this, "Error loading leaderboard data", Toast.LENGTH_SHORT).show()
            }
    }

    // NEW METHOD: Extract userId correctly for both Google and email/password users
    private fun getUserIdFromDocument(userDoc: com.google.firebase.firestore.DocumentSnapshot): String? {
        // For Google users, try the 'uid' field first, then document ID
        val uid = userDoc.getString("uid")
        if (!uid.isNullOrBlank()) {
            Log.d("LeaderboardActivity", "Found uid field: $uid")
            return uid
        }

        // For email/password users, try 'userId' field
        val userId = userDoc.getString("userId")
        if (!userId.isNullOrBlank()) {
            Log.d("LeaderboardActivity", "Found userId field: $userId")
            return userId
        }

        // Fallback to document ID (should work for Google users)
        val docId = userDoc.id
        if (docId.isNotBlank()) {
            Log.d("LeaderboardActivity", "Using document ID as userId: $docId")
            return docId
        }

        Log.w("LeaderboardActivity", "No valid userId found in document")
        return null
    }

    // NEW METHOD: Extract username correctly for both Google and email/password users
    private fun getUsernameFromDocument(userDoc: com.google.firebase.firestore.DocumentSnapshot): String {
        // Try different username fields in order of preference
        val username = userDoc.getString("username")
        if (!username.isNullOrBlank()) {
            return username
        }

        val displayName = userDoc.getString("displayName")
        if (!displayName.isNullOrBlank()) {
            return displayName
        }

        val fullName = userDoc.getString("fullName")
        if (!fullName.isNullOrBlank()) {
            return fullName
        }

        val email = userDoc.getString("email")
        if (!email.isNullOrBlank()) {
            // Extract username from email
            return email.substringBefore("@")
        }

        return "Unknown User"
    }

    // METHOD: Matches exactly how ProgressActivity calculates scores
    private fun getUserProgressLikeProgressActivity(
        userId: String,
        username: String,
        email: String,
        fullName: String,
        homeLanguage: String,
        callback: (LeaderboardUser?) -> Unit
    ) {
        Log.d("LeaderboardActivity", "Getting progress for user: $username (userId: $userId) - MATCHING ProgressActivity method")

        // Query test_results exactly like ProgressActivity does in loadTestResultsSimple()
        firestore.collection("test_results")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                var wordCount = 0
                var phraseCount = 0
                var wordsAttempted = 0
                var phrasesAttempted = 0

                Log.d("LeaderboardActivity", "Found ${documents.size()} test results for user: $username")

                for (doc in documents) {
                    val testType = doc.getString("testType") ?: ""
                    val correct = doc.getLong("correctAnswers")?.toInt() ?: 0 // Use correctAnswers, not score!
                    val totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0

                    Log.d("LeaderboardActivity", "Test result for $username: testType=$testType, correctAnswers=$correct, totalQuestions=$totalQuestions")

                    if (testType == "WORD_TEST") {
                        wordCount += correct
                        wordsAttempted += totalQuestions
                    } else if (testType == "PHRASE_TEST") {
                        phraseCount += correct
                        phrasesAttempted += totalQuestions
                    }
                }

                // Calculate total score exactly like ProgressActivity: wordsSpoken + phrasesSpoken
                val totalScore = wordCount + phraseCount

                Log.d("LeaderboardActivity", "FINAL CALCULATION for $username: words=$wordCount + phrases=$phraseCount = total=$totalScore")

                // Also get total days practiced like ProgressActivity does
                getDaysPracticedForUser(userId) { daysPracticed ->
                    val leaderboardUser = LeaderboardUser(
                        userId = userId,
                        username = username,
                        email = email,
                        fullName = fullName,
                        homeLanguage = homeLanguage,
                        totalScore = totalScore,
                        wordsCorrect = wordCount,
                        phrasesCorrect = phraseCount,
                        wordsAttempted = wordsAttempted,
                        phrasesAttempted = phrasesAttempted,
                        daysPracticed = daysPracticed,
                        rank = 0 // Will be set later when sorting
                    )

                    Log.d("LeaderboardActivity", "Created LeaderboardUser for $username with total score: $totalScore")
                    callback(leaderboardUser)
                }
            }
            .addOnFailureListener { e ->
                Log.w("LeaderboardActivity", "Error loading test results for user $username", e)
                callback(null)
            }
    }

    // Get days practiced exactly like ProgressActivity
    private fun getDaysPracticedForUser(userId: String, callback: (Int) -> Unit) {
        firestore.collection("daily_logins")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                // Use a Set to ensure unique dates only - exactly like ProgressActivity
                val uniqueDates = mutableSetOf<String>()

                for (document in documents) {
                    val loginDate = document.getString("date")
                    if (!loginDate.isNullOrEmpty()) {
                        uniqueDates.add(loginDate)
                    }
                }

                val daysPracticed = uniqueDates.size
                Log.d("LeaderboardActivity", "Days practiced for user $userId: $daysPracticed")
                callback(daysPracticed)
            }
            .addOnFailureListener { e ->
                Log.w("LeaderboardActivity", "Error loading days practiced for user $userId", e)
                callback(0)
            }
    }

    private fun finishProcessing() {
        Log.d("LeaderboardActivity", "=== FINISH PROCESSING DEBUG ===")
        Log.d("LeaderboardActivity", "All users processed. Total in list: ${usersList.size}")

        // Debug: Log ALL users before sorting
        Log.d("LeaderboardActivity", "Users before sorting:")
        usersList.forEachIndexed { index, user ->
            Log.d("LeaderboardActivity", "[$index] ${user.username} - Score: ${user.totalScore} (${user.wordsCorrect} words + ${user.phrasesCorrect} phrases)")
        }

        // Sort by total score (highest first) and assign ranks
        usersList.sortByDescending { it.totalScore }
        usersList.forEachIndexed { index, user ->
            user.rank = index + 1
        }

        // Debug: Log ALL users after sorting
        Log.d("LeaderboardActivity", "Users after sorting:")
        usersList.forEachIndexed { index, user ->
            Log.d("LeaderboardActivity", "Rank ${user.rank}: ${user.username} - Score: ${user.totalScore}")
        }

        runOnUiThread {
            Log.d("LeaderboardActivity", "Updating UI on main thread...")
            showLoading(false)

            if (usersList.isEmpty()) {
                Log.d("LeaderboardActivity", "No users with progress data found - showing no data")
                showNoData(true)
            } else {
                Log.d("LeaderboardActivity", "Found ${usersList.size} users - updating adapter")
                showNoData(false)
                leaderboardAdapter.notifyDataSetChanged()
                Log.d("LeaderboardActivity", "Adapter notified of data change")

                // Force a complete refresh of the adapter
                recyclerView.adapter = leaderboardAdapter
                Log.d("LeaderboardActivity", "Adapter re-assigned to RecyclerView")
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showNoData(show: Boolean) {
        tvNoData.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    fun refreshLeaderboard() {
        loadLeaderboardData()
    }
}

// Updated data class for leaderboard users with additional fields
data class LeaderboardUser(
    val userId: String,
    val username: String,
    val email: String,
    val fullName: String,
    val homeLanguage: String,
    val wordsCorrect: Int,
    val phrasesCorrect: Int,
    val wordsAttempted: Int,
    val phrasesAttempted: Int,
    val daysPracticed: Int,
    val totalScore: Int,
    var rank: Int
) {
    fun getAccuracy(): Float {
        val totalAttempted = wordsAttempted + phrasesAttempted
        return if (totalAttempted > 0) {
            ((wordsCorrect + phrasesCorrect).toFloat() / totalAttempted.toFloat()) * 100f
        } else {
            0f
        }
    }

    fun getDisplayName(): String {
        return if (fullName.isNotBlank() && fullName != username) {
            fullName
        } else {
            username
        }
    }
}