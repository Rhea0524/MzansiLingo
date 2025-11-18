package com.fake.mzansilingo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileOutputStream

class LeaderboardActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnShare: ImageView // NEW: Share button
    private lateinit var tvLanguage: TextView
    private lateinit var tvTitle: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersList = mutableListOf<LeaderboardUser>()
    private var currentLanguage = "afrikaans"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        currentLanguage = intent.getStringExtra("LANGUAGE") ?: "afrikaans"

        initializeViews()
        setupUI()
        setupRecyclerView()
        loadLeaderboardData()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentHomeLanguage = prefs.getString("home_language", "English") ?: "English"

        val currentLocale = resources.configuration.locales[0].language
        val expectedLocale = when (currentHomeLanguage) {
            "English" -> "en"
            "isiZulu" -> "zu"
            else -> "en"
        }

        if (currentLocale != expectedLocale) {
            recreate()
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recycler_leaderboard)
        progressBar = findViewById(R.id.progress_bar)
        tvNoData = findViewById(R.id.tv_no_data)
        btnBack = findViewById(R.id.btn_back)
        btnShare = findViewById(R.id.btn_share) // NEW: Initialize share button
        tvLanguage = findViewById(R.id.tv_language)
        tvTitle = findViewById(R.id.tv_title)
    }

    private fun setupUI() {
        tvLanguage.text = getString(R.string.language_header_afrikaans)
        tvTitle.text = getString(R.string.leaderboard_title)
        tvNoData.text = getString(R.string.leaderboard_no_data)

        btnBack.setOnClickListener {
            finish()
        }

        // NEW: Share button click listener
        btnShare.setOnClickListener {
            captureAndShare()
        }
    }

    // NEW: Screenshot sharing functionality
    private fun captureAndShare() {
        // Get the root view (the entire constraint layout)
        val rootView = findViewById<View>(android.R.id.content)

        // Temporarily hide the share button for cleaner screenshot
        btnShare.visibility = View.GONE

        // Small delay to ensure button is hidden
        rootView.postDelayed({
            val bitmap = captureViewAsBitmap(rootView)

            // Show the button again
            btnShare.visibility = View.VISIBLE

            if (bitmap != null) {
                val uri = saveBitmapToCache(bitmap)
                if (uri != null) {
                    shareImage(uri)
                } else {
                    Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
            }
        }, 100)
    }

    private fun captureViewAsBitmap(view: View): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            view.draw(canvas)

            bitmap
        } catch (e: Exception) {
            Log.e("LeaderboardActivity", "Error capturing screenshot", e)
            null
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            // Create cache directory
            val imagesDir = File(cacheDir, "images")
            imagesDir.mkdirs()

            // Create file with timestamp
            val file = File(imagesDir, "leaderboard_${System.currentTimeMillis()}.png")

            // Write bitmap to file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Get URI using FileProvider
            FileProvider.getUriForFile(
                this,
                "com.fake.mzansilingo.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("LeaderboardActivity", "Error saving screenshot", e)
            null
        }
    }

    private fun shareImage(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Check out the Mzansi Lingo leaderboard! 🏆🦏")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Leaderboard via"))
    }

    // Optional: Share to specific apps
    private fun shareToWhatsApp(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Check out the Mzansi Lingo leaderboard! 🏆🦏")
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareToInstagram(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.instagram.android")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Instagram not installed", Toast.LENGTH_SHORT).show()
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

                documents.forEach { userDoc ->
                    val firebaseAuthUid = getUserIdFromDocument(userDoc)
                    val username = getUsernameFromDocument(userDoc)
                    val email = userDoc.getString("email") ?: "unknown@app.com"
                    val fullName = userDoc.getString("fullName") ?: userDoc.getString("displayName") ?: username
                    val homeLanguage = userDoc.getString("homeLanguage") ?: "Unknown"
                    val provider = userDoc.getString("provider") ?: "email"

                    Log.d("LeaderboardActivity", "Processing user: $username (Doc ID: ${userDoc.id}, userId: $firebaseAuthUid, Provider: $provider, Email: $email)")

                    if (firebaseAuthUid != null) {
                        getUserProgressLikeProgressActivity(firebaseAuthUid, username, email, fullName, homeLanguage) { leaderboardUser ->
                            processedUsers++

                            if (leaderboardUser != null) {
                                usersList.add(leaderboardUser)
                                Log.d("LeaderboardActivity", "Added user: ${leaderboardUser.username} with score ${leaderboardUser.totalScore}")
                            }

                            if (processedUsers >= totalUsers) {
                                finishProcessing()
                            }
                        }
                    } else {
                        Log.w("LeaderboardActivity", "No valid userId found for user: $username")
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
                Toast.makeText(this, getString(R.string.error_loading_leaderboard), Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUserIdFromDocument(userDoc: com.google.firebase.firestore.DocumentSnapshot): String? {
        val uid = userDoc.getString("uid")
        if (!uid.isNullOrBlank()) {
            Log.d("LeaderboardActivity", "Found uid field: $uid")
            return uid
        }

        val userId = userDoc.getString("userId")
        if (!userId.isNullOrBlank()) {
            Log.d("LeaderboardActivity", "Found userId field: $userId")
            return userId
        }

        val docId = userDoc.id
        if (docId.isNotBlank()) {
            Log.d("LeaderboardActivity", "Using document ID as userId: $docId")
            return docId
        }

        Log.w("LeaderboardActivity", "No valid userId found in document")
        return null
    }

    private fun getUsernameFromDocument(userDoc: com.google.firebase.firestore.DocumentSnapshot): String {
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
            return email.substringBefore("@")
        }

        return "Unknown User"
    }

    private fun getUserProgressLikeProgressActivity(
        userId: String,
        username: String,
        email: String,
        fullName: String,
        homeLanguage: String,
        callback: (LeaderboardUser?) -> Unit
    ) {
        Log.d("LeaderboardActivity", "Getting progress for user: $username (userId: $userId)")

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
                    val correct = doc.getLong("correctAnswers")?.toInt() ?: 0
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

                val totalScore = wordCount + phraseCount

                Log.d("LeaderboardActivity", "FINAL CALCULATION for $username: words=$wordCount + phrases=$phraseCount = total=$totalScore")

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
                        rank = 0
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

    private fun getDaysPracticedForUser(userId: String, callback: (Int) -> Unit) {
        firestore.collection("daily_logins")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
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

        Log.d("LeaderboardActivity", "Users before sorting:")
        usersList.forEachIndexed { index, user ->
            Log.d("LeaderboardActivity", "[$index] ${user.username} - Score: ${user.totalScore} (${user.wordsCorrect} words + ${user.phrasesCorrect} phrases)")
        }

        usersList.sortByDescending { it.totalScore }
        usersList.forEachIndexed { index, user ->
            user.rank = index + 1
        }

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