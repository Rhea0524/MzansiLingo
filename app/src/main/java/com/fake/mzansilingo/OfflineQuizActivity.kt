package com.fake.mzansilingo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class QuizQuestion(
    val word: String,
    val translation: String,
    val options: List<String>,
    val correctAnswer: Int,
    val category: String = "general"
)

data class OfflineQuizResult(
    val language: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timestamp: Long,
    val synced: Boolean = false
)

class OfflineQuizActivity : AppCompatActivity() {

    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvWord: TextView
    private lateinit var tvProgress: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnOption4: Button
    private lateinit var cardOption1: CardView
    private lateinit var cardOption2: CardView
    private lateinit var cardOption3: CardView
    private lateinit var cardOption4: CardView
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar

    private var questions = mutableListOf<QuizQuestion>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswer = -1
    private lateinit var language: String

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Network change receiver
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isOnline()) {
                Log.d("OfflineQuiz", "Network connected! Attempting to sync...")
                syncPendingResults()
            }
        }
    }

    // Store original card color
    private val originalCardColor = Color.parseColor("#9FA8DA")
    private val correctColor = Color.parseColor("#4CAF50")
    private val wrongColor = Color.parseColor("#F44336")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_quiz)

        language = intent.getStringExtra("LANGUAGE") ?: "afrikaans"

        initializeViews()
        loadQuestions()
        displayQuestion()
        setupClickListeners()

        // Register network receiver
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        // Try to sync any pending results when activity starts
        syncPendingResults()
    }

    private fun initializeViews() {
        tvQuestionNumber = findViewById(R.id.tv_question_number)
        tvWord = findViewById(R.id.tv_word)
        tvProgress = findViewById(R.id.tv_progress)
        btnOption1 = findViewById(R.id.btn_option1)
        btnOption2 = findViewById(R.id.btn_option2)
        btnOption3 = findViewById(R.id.btn_option3)
        btnOption4 = findViewById(R.id.btn_option4)
        cardOption1 = findViewById(R.id.card_option1)
        cardOption2 = findViewById(R.id.card_option2)
        cardOption3 = findViewById(R.id.card_option3)
        cardOption4 = findViewById(R.id.card_option4)
        btnNext = findViewById(R.id.btn_next)
        progressBar = findViewById(R.id.progress_bar)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            showExitDialog()
        }

        findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            showExitDialog()
        }
    }

    private fun loadQuestions() {
        val prefs = getSharedPreferences("offline_quiz_prefs", Context.MODE_PRIVATE)
        val savedQuestions = prefs.getString("questions_$language", null)

        questions = if (savedQuestions != null) {
            val type = object : TypeToken<List<QuizQuestion>>() {}.type
            Gson().fromJson(savedQuestions, type)
        } else {
            getDefaultQuestions(language)
        }

        questions.shuffle()
        questions = questions.take(10).toMutableList()
    }

    private fun getDefaultQuestions(lang: String): MutableList<QuizQuestion> {
        return when(lang.lowercase()) {
            "afrikaans" -> mutableListOf(
                QuizQuestion("Hello", "Hallo", listOf("Hallo", "Totsiens", "Dankie", "Asseblief"), 0),
                QuizQuestion("Goodbye", "Totsiens", listOf("Hallo", "Totsiens", "Môre", "Aand"), 1),
                QuizQuestion("Thank you", "Dankie", listOf("Asseblief", "Dankie", "Ja", "Nee"), 1),
                QuizQuestion("Please", "Asseblief", listOf("Asseblief", "Dankie", "Verskoon", "Jammer"), 0),
                QuizQuestion("Yes", "Ja", listOf("Nee", "Miskien", "Ja", "Nooit"), 2),
                QuizQuestion("No", "Nee", listOf("Ja", "Nee", "Miskien", "Altyd"), 1),
                QuizQuestion("Good morning", "Goeie môre", listOf("Goeie aand", "Goeie môre", "Goeie middag", "Goednag"), 1),
                QuizQuestion("Water", "Water", listOf("Melk", "Sap", "Water", "Tee"), 2),
                QuizQuestion("Food", "Kos", listOf("Drank", "Kos", "Brood", "Vleis"), 1),
                QuizQuestion("House", "Huis", listOf("Huis", "Kar", "Tuin", "Kamer"), 0),
                QuizQuestion("Dog", "Hond", listOf("Kat", "Perd", "Hond", "Voël"), 2),
                QuizQuestion("Cat", "Kat", listOf("Kat", "Hond", "Muis", "Vis"), 0),
                QuizQuestion("Love", "Liefde", listOf("Haat", "Vrees", "Liefde", "Vreugde"), 2),
                QuizQuestion("Friend", "Vriend", listOf("Vyand", "Vriend", "Familie", "Vreemdeling"), 1),
                QuizQuestion("Beautiful", "Mooi", listOf("Lelik", "Mooi", "Groot", "Klein"), 1)
            )
            "zulu" -> mutableListOf(
                QuizQuestion("Hello", "Sawubona", listOf("Sawubona", "Hamba kahle", "Ngiyabonga", "Ngicela"), 0),
                QuizQuestion("Goodbye", "Hamba kahle", listOf("Sawubona", "Hamba kahle", "Yebo", "Cha"), 1),
                QuizQuestion("Thank you", "Ngiyabonga", listOf("Ngicela", "Ngiyabonga", "Yebo", "Cha"), 1),
                QuizQuestion("Please", "Ngicela", listOf("Ngicela", "Ngiyabonga", "Uxolo", "Yebo"), 0),
                QuizQuestion("Yes", "Yebo", listOf("Cha", "Mhlawumbe", "Yebo", "Akukho"), 2),
                QuizQuestion("No", "Cha", listOf("Yebo", "Cha", "Mhlawumbe", "Njalo"), 1),
                QuizQuestion("Good morning", "Sawubona ekuseni", listOf("Sawubona ntambama", "Sawubona ekuseni", "Sawubona emini", "Lala kahle"), 1),
                QuizQuestion("Water", "Amanzi", listOf("Ubisi", "Ijusi", "Amanzi", "Itiye"), 2),
                QuizQuestion("Food", "Ukudla", listOf("Ukuphuza", "Ukudla", "Isinkwa", "Inyama"), 1),
                QuizQuestion("House", "Indlu", listOf("Indlu", "Imoto", "Ingadi", "Igumbi"), 0)
            )
            "xhosa" -> mutableListOf(
                QuizQuestion("Hello", "Molo", listOf("Molo", "Hamba kakuhle", "Enkosi", "Nceda"), 0),
                QuizQuestion("Goodbye", "Hamba kakuhle", listOf("Molo", "Hamba kakuhle", "Ewe", "Hayi"), 1),
                QuizQuestion("Thank you", "Enkosi", listOf("Nceda", "Enkosi", "Ewe", "Hayi"), 1),
                QuizQuestion("Please", "Nceda", listOf("Nceda", "Enkosi", "Uxolo", "Ewe"), 0),
                QuizQuestion("Yes", "Ewe", listOf("Hayi", "Mhlawumbe", "Ewe", "Akukho"), 2),
                QuizQuestion("No", "Hayi", listOf("Ewe", "Hayi", "Mhlawumbe", "Soloko"), 1),
                QuizQuestion("Good morning", "Molo kusasa", listOf("Molo ngokuhlwa", "Molo kusasa", "Molo emini", "Ulale kakuhle"), 1),
                QuizQuestion("Water", "Amanzi", listOf("Ubisi", "Ijusi", "Amanzi", "Iti"), 2),
                QuizQuestion("Food", "Ukutya", listOf("Ukusela", "Ukutya", "Isonka", "Inyama"), 1),
                QuizQuestion("House", "Indlu", listOf("Indlu", "Imoto", "Igadi", "Igumbi"), 0)
            )
            else -> getDefaultQuestions("afrikaans")
        }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex >= questions.size) {
            showResults()
            return
        }

        val question = questions[currentQuestionIndex]
        tvQuestionNumber.text = "Question ${currentQuestionIndex + 1}/${questions.size}"
        tvWord.text = question.word
        tvProgress.text = "$score correct"
        progressBar.progress = ((currentQuestionIndex.toFloat() / questions.size) * 100).toInt()

        btnOption1.text = question.options[0]
        btnOption2.text = question.options[1]
        btnOption3.text = question.options[2]
        btnOption4.text = question.options[3]

        resetCards()
        selectedAnswer = -1
        btnNext.isEnabled = false
    }

    private fun setupClickListeners() {
        // Set click listeners on buttons instead of cards
        btnOption1.setOnClickListener { selectAnswer(0, cardOption1, btnOption1) }
        btnOption2.setOnClickListener { selectAnswer(1, cardOption2, btnOption2) }
        btnOption3.setOnClickListener { selectAnswer(2, cardOption3, btnOption3) }
        btnOption4.setOnClickListener { selectAnswer(3, cardOption4, btnOption4) }

        btnNext.setOnClickListener {
            currentQuestionIndex++
            displayQuestion()
        }
    }

    private fun selectAnswer(answerIndex: Int, card: CardView, button: Button) {
        if (selectedAnswer != -1) return

        selectedAnswer = answerIndex
        val question = questions[currentQuestionIndex]

        // Disable all buttons after selection
        disableAllButtons()

        if (answerIndex == question.correctAnswer) {
            // Correct answer - show green with checkmark
            card.setCardBackgroundColor(correctColor)
            button.text = "✓ ${question.options[answerIndex]}"
            score++
            Toast.makeText(this, "Correct! ✓", Toast.LENGTH_SHORT).show()
        } else {
            // Wrong answer - show red with X
            card.setCardBackgroundColor(wrongColor)
            button.text = "✗ ${question.options[answerIndex]}"
            // Also highlight the correct answer
            highlightCorrectAnswer(question.correctAnswer)
            Toast.makeText(this, "Wrong! The correct answer was: ${question.options[question.correctAnswer]}", Toast.LENGTH_LONG).show()
        }

        btnNext.isEnabled = true
        saveProgress()
    }

    private fun highlightCorrectAnswer(correctIndex: Int) {
        val correctCard: CardView
        val correctButton: Button

        when(correctIndex) {
            0 -> {
                correctCard = cardOption1
                correctButton = btnOption1
            }
            1 -> {
                correctCard = cardOption2
                correctButton = btnOption2
            }
            2 -> {
                correctCard = cardOption3
                correctButton = btnOption3
            }
            3 -> {
                correctCard = cardOption4
                correctButton = btnOption4
            }
            else -> return
        }

        // Show correct answer in green with checkmark
        correctCard.setCardBackgroundColor(correctColor)
        val question = questions[currentQuestionIndex]
        correctButton.text = "✓ ${question.options[correctIndex]}"
    }

    private fun disableAllButtons() {
        btnOption1.isEnabled = false
        btnOption2.isEnabled = false
        btnOption3.isEnabled = false
        btnOption4.isEnabled = false
    }

    private fun resetCards() {
        val cards = listOf(cardOption1, cardOption2, cardOption3, cardOption4)
        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)

        cards.forEach { card ->
            card.setCardBackgroundColor(originalCardColor)
        }

        buttons.forEachIndexed { index, btn ->
            btn.isEnabled = true
            // Reset to original option text
            val question = questions[currentQuestionIndex]
            btn.text = question.options[index]
            btn.setTextColor(Color.WHITE)
        }
    }

    private fun saveProgress() {
        val prefs = getSharedPreferences("offline_quiz_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("total_questions_answered",
            prefs.getInt("total_questions_answered", 0) + 1)
        editor.putInt("total_correct_answers",
            prefs.getInt("total_correct_answers", 0) + (if (selectedAnswer == questions[currentQuestionIndex].correctAnswer) 1 else 0))
        editor.apply()
    }

    private fun showResults() {
        val percentage = (score.toFloat() / questions.size * 100).toInt()

        val message = when {
            percentage >= 90 -> "Excellent! 🌟\n\nYou scored $score out of ${questions.size}\n\nAccuracy: $percentage%"
            percentage >= 70 -> "Great job! 👍\n\nYou scored $score out of ${questions.size}\n\nAccuracy: $percentage%"
            percentage >= 50 -> "Good effort! 💪\n\nYou scored $score out of ${questions.size}\n\nAccuracy: $percentage%"
            else -> "Keep practicing! 📚\n\nYou scored $score out of ${questions.size}\n\nAccuracy: $percentage%"
        }

        // Save the quiz result locally
        saveQuizResult(score, questions.size)

        AlertDialog.Builder(this)
            .setTitle("Quiz Complete!")
            .setMessage(message)
            .setPositiveButton("Try Again") { _, _ ->
                currentQuestionIndex = 0
                score = 0
                questions.shuffle()
                displayQuestion()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // Save quiz result locally and try to sync
    private fun saveQuizResult(correctAnswers: Int, totalQuestions: Int) {
        val result = OfflineQuizResult(
            language = language,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            timestamp = System.currentTimeMillis(),
            synced = false
        )

        // Save to SharedPreferences
        val prefs = getSharedPreferences("offline_quiz_results", Context.MODE_PRIVATE)
        val resultsJson = prefs.getString("pending_results", "[]")
        val type = object : TypeToken<MutableList<OfflineQuizResult>>() {}.type
        val results: MutableList<OfflineQuizResult> = Gson().fromJson(resultsJson, type)

        results.add(result)

        val editor = prefs.edit()
        editor.putString("pending_results", Gson().toJson(results))
        editor.apply()

        Log.d("OfflineQuiz", "Saved offline result: $correctAnswers/$totalQuestions for $language")

        // Start the sync service to handle syncing in the background
        QuizSyncService.startService(this)

        // Also try to sync immediately if online
        syncPendingResults()
    }

    // Check if device is online
    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Sync all pending results to Firebase
    private fun syncPendingResults() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("OfflineQuiz", "No user logged in, cannot sync")
            return
        }

        if (!isOnline()) {
            Log.d("OfflineQuiz", "Device is offline, will sync later")
            return
        }

        val prefs = getSharedPreferences("offline_quiz_results", Context.MODE_PRIVATE)
        val resultsJson = prefs.getString("pending_results", "[]")
        val type = object : TypeToken<MutableList<OfflineQuizResult>>() {}.type
        val results: MutableList<OfflineQuizResult> = Gson().fromJson(resultsJson, type)

        if (results.isEmpty()) {
            Log.d("OfflineQuiz", "No pending results to sync")
            return
        }

        Log.d("OfflineQuiz", "Syncing ${results.size} pending results to Firebase")

        val userId = currentUser.uid
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var syncedCount = 0
        val unsyncedResults = results.filter { !it.synced }

        unsyncedResults.forEach { result ->
            // Create test result document matching LeaderboardActivity's expectations
            val testResult = hashMapOf(
                "userId" to userId,
                "testType" to "WORD_TEST", // Treating quiz as word test
                "correctAnswers" to result.correctAnswers.toLong(),
                "totalQuestions" to result.totalQuestions.toLong(),
                "score" to result.correctAnswers.toLong(), // Also set score field
                "language" to result.language,
                "timestamp" to com.google.firebase.Timestamp(Date(result.timestamp)),
                "date" to dateFormat.format(Date(result.timestamp)),
                "source" to "offline_quiz"
            )

            firestore.collection("test_results")
                .add(testResult)
                .addOnSuccessListener { documentReference ->
                    Log.d("OfflineQuiz", "Synced result ${documentReference.id} - $result")
                    syncedCount++

                    // Remove synced results from the list
                    val remainingResults = results.filter { it != result }

                    // Update shared preferences
                    val editor = prefs.edit()
                    editor.putString("pending_results", Gson().toJson(remainingResults))
                    editor.apply()

                    // Also log to daily_logins
                    logDailyLogin(userId, dateFormat.format(Date(result.timestamp)))

                    if (syncedCount == unsyncedResults.size) {
                        Toast.makeText(this, "Quiz results synced successfully! ✓", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("OfflineQuiz", "Error syncing result", e)
                }
        }
    }

    // Log daily login like other activities do
    private fun logDailyLogin(userId: String, date: String) {
        val loginData = hashMapOf(
            "userId" to userId,
            "date" to date,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("daily_logins")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No login today, add it
                    firestore.collection("daily_logins")
                        .add(loginData)
                        .addOnSuccessListener {
                            Log.d("OfflineQuiz", "Daily login logged for $date")
                        }
                        .addOnFailureListener { e ->
                            Log.e("OfflineQuiz", "Error logging daily login", e)
                        }
                } else {
                    Log.d("OfflineQuiz", "Daily login already exists for $date")
                }
            }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit Quiz?")
            .setMessage("Your progress will be lost. Are you sure?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    override fun onResume() {
        super.onResume()
        // Try to sync when app comes to foreground
        syncPendingResults()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister network receiver
        try {
            unregisterReceiver(networkReceiver)
        } catch (e: Exception) {
            Log.e("OfflineQuiz", "Error unregistering receiver", e)
        }
    }
}