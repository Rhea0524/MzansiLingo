package com.fake.mzansilingo

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import android.media.RingtoneManager
import android.net.Uri
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator

class SetGoalsActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    private var progressListener: ListenerRegistration? = null

    // UI Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView

    // Goal setting UI
    private lateinit var tvWordsGoal: TextView
    private lateinit var btnWordsDecrease: Button
    private lateinit var btnWordsIncrease: Button
    private lateinit var tvPhrasesGoal: TextView
    private lateinit var btnPhrasesDecrease: Button
    private lateinit var btnPhrasesIncrease: Button
    private lateinit var btnSaveGoals: Button
    private lateinit var btnResetToDefault: Button

    // Progress display
    private lateinit var tvCurrentProgress: TextView
    private lateinit var tvGoalProgress: TextView

    // Achievement Popup Components
    private lateinit var achievementPopup: CardView
    private lateinit var achievementIcon: ImageView
    private lateinit var achievementTitle: TextView
    private lateinit var achievementMessage: TextView
    private lateinit var achievementStars: List<ImageView>
    private lateinit var achievementConfetti: List<View> // Fixed: Changed from ImageView to View
    private lateinit var achievementBadge: ImageView

    // Navigation drawer items
    private lateinit var navHome: TextView
    private lateinit var navLanguage: TextView
    private lateinit var navWords: TextView
    private lateinit var navPhrases: TextView
    private lateinit var navLeaderboard: TextView

    private lateinit var navSettings: TextView
    private lateinit var navProfile: TextView
    private lateinit var navBack: ImageView
    private lateinit var navChat: ImageView
    private lateinit var navDictionary: ImageView

    // Bottom navigation
    private lateinit var btnBottomDict: ImageView
    private lateinit var btnBottomQuotes: ImageView
    private lateinit var btnBottomStats: ImageView

    // Goal values
    private var dailyWordsGoal = 10 // Default values
    private var dailyPhrasesGoal = 5

    // Current progress (for display)
    private var todayWordsRight = 0
    private var todayPhrasesRight = 0

    // Track if goals were already achieved (to prevent repeated notifications)
    private var wordsGoalAchievedToday = false
    private var phrasesGoalAchievedToday = false
    private var bothGoalsAchievedToday = false

    // Achievement sound effects
    private var achievementSound: MediaPlayer? = null

    companion object {
        private const val TAG = "SetGoalsActivity"
        private const val COLLECTION_USER_GOALS = "user_goals"
        private const val MIN_GOAL_VALUE = 1
        private const val MAX_GOAL_VALUE = 100
        private const val DEFAULT_WORDS_GOAL = 10
        private const val DEFAULT_PHRASES_GOAL = 5

        // Achievement types
        enum class AchievementType {
            WORDS_GOAL,
            PHRASES_GOAL,
            BOTH_GOALS,
            STREAK_ACHIEVEMENT
        }

        // Static method to check if goals are achieved (can be called from test activities)
        fun checkGoalAchievement(
            context: android.content.Context,
            wordsRight: Int,
            phrasesRight: Int,
            callback: (Boolean, Boolean) -> Unit
        ) {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val userId = auth.currentUser?.uid ?: return

            firestore.collection(COLLECTION_USER_GOALS)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val wordsGoal = document.getLong("dailyWordsGoal")?.toInt() ?: DEFAULT_WORDS_GOAL
                    val phrasesGoal = document.getLong("dailyPhrasesGoal")?.toInt() ?: DEFAULT_PHRASES_GOAL

                    val wordsAchieved = wordsRight >= wordsGoal
                    val phrasesAchieved = phrasesRight >= phrasesGoal

                    callback(wordsAchieved, phrasesAchieved)
                }
                .addOnFailureListener {
                    callback(false, false)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_goals)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            redirectToLogin()
            return
        }

        initializeViews()
        initializeAchievementPopup()
        loadCurrentGoals()
        setupRealTimeProgressListener()
        setupClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressListener?.remove()
        achievementSound?.release()
    }

    override fun onPause() {
        super.onPause()
        progressListener?.remove()
    }

    override fun onResume() {
        super.onResume()
        setupRealTimeProgressListener()
    }

    private fun initializeViews() {
        // Main layout
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)

        // Goal setting controls
        tvWordsGoal = findViewById(R.id.tv_words_goal)
        btnWordsDecrease = findViewById(R.id.btn_words_decrease)
        btnWordsIncrease = findViewById(R.id.btn_words_increase)
        tvPhrasesGoal = findViewById(R.id.tv_phrases_goal)
        btnPhrasesDecrease = findViewById(R.id.btn_phrases_decrease)
        btnPhrasesIncrease = findViewById(R.id.btn_phrases_increase)
        btnSaveGoals = findViewById(R.id.btn_save_goals)
        btnResetToDefault = findViewById(R.id.btn_reset_default)

        // Progress display
        tvCurrentProgress = findViewById(R.id.tv_current_progress)
        tvGoalProgress = findViewById(R.id.tv_goal_progress)

        // Navigation drawer items
        navHome = findViewById(R.id.nav_home)
        navLanguage = findViewById(R.id.nav_language)
        navWords = findViewById(R.id.nav_words)
        navPhrases = findViewById(R.id.nav_phrases)
        navLeaderboard = findViewById(R.id.nav_leaderboard)

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

    private fun initializeAchievementPopup() {
        // Find achievement popup views
        achievementPopup = findViewById(R.id.achievement_popup)
        // Fix: Use achievement_badge instead of achievement_icon
        achievementIcon = findViewById(R.id.achievement_badge) // This will serve as our icon
        achievementTitle = findViewById(R.id.achievement_title)
        achievementMessage = findViewById(R.id.achievement_message)
        achievementBadge = findViewById(R.id.achievement_badge)

        // Initialize all 8 stars for animation (matching your XML)
        achievementStars = listOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5),
            findViewById(R.id.star6),
            findViewById(R.id.star7),
            findViewById(R.id.star8)
        )

        // Initialize all 10 confetti pieces (matching your XML)
        achievementConfetti = listOf(
            findViewById<View>(R.id.confetti1),
            findViewById<View>(R.id.confetti2),
            findViewById<View>(R.id.confetti3),
            findViewById<View>(R.id.confetti4),
            findViewById<View>(R.id.confetti5),
            findViewById<View>(R.id.confetti6),
            findViewById<View>(R.id.confetti7),
            findViewById<View>(R.id.confetti8),
            findViewById<View>(R.id.confetti9),
            findViewById<View>(R.id.confetti10)
        )

        // Hide popup initially
        achievementPopup.visibility = View.GONE

        // Set up click to dismiss
        achievementPopup.setOnClickListener {
            hideAchievementPopup()
        }
    }

    private fun showAchievementPopup(type: AchievementType) {
        when (type) {
            AchievementType.WORDS_GOAL -> {
                achievementBadge.setImageResource(R.drawable.ic_book_open)
                achievementTitle.text = "WORDS MASTER!"
                achievementMessage.text = "Daily words goal achieved!\nYou're building your vocabulary!"
                achievementPopup.setCardBackgroundColor(Color.parseColor("#2E7D32")) // Darker green
            }
            AchievementType.PHRASES_GOAL -> {
                achievementBadge.setImageResource(R.drawable.ic_message_circle)
                achievementTitle.text = "PHRASE CHAMPION!"
                achievementMessage.text = "Daily phrases goal achieved!\nYour communication skills are growing!"
                achievementPopup.setCardBackgroundColor(Color.parseColor("#2E7D32")) // Darker green
            }
            AchievementType.BOTH_GOALS -> {
                achievementBadge.setImageResource(R.drawable.ic_trophy_badge) // Changed from rhino to trophy
                achievementTitle.text = "DAILY LEGEND!"
                achievementMessage.text = "INCREDIBLE! Both goals achieved!\nYou're unstoppable today!"
                achievementPopup.setCardBackgroundColor(Color.parseColor("#1B5E20")) // Very dark green for maximum impact
            }
            AchievementType.STREAK_ACHIEVEMENT -> {
                achievementBadge.setImageResource(R.drawable.ic_check)
                achievementTitle.text = "ON FIRE!"
                achievementMessage.text = "Amazing streak!\nKeep the momentum going!"
                achievementPopup.setCardBackgroundColor(Color.parseColor("#2E7D32")) // Darker green
            }
        }

        // Rest of the method stays the same...
        playAchievementSound(type)
        achievementPopup.visibility = View.VISIBLE
        animateAchievementEntry()

        Handler(Looper.getMainLooper()).postDelayed({
            hideAchievementPopup()
        }, 4000)


        // Rest of the method stays the same...
        playAchievementSound(type)
        achievementPopup.visibility = View.VISIBLE
        animateAchievementEntry()

        Handler(Looper.getMainLooper()).postDelayed({
            hideAchievementPopup()
        }, 4000)
    }

    private fun animateAchievementEntry() {
        // Scale and fade in the main popup
        val scaleX = ObjectAnimator.ofFloat(achievementPopup, "scaleX", 0f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(achievementPopup, "scaleY", 0f, 1.2f, 1f)
        val alpha = ObjectAnimator.ofFloat(achievementPopup, "alpha", 0f, 1f)

        val popupAnimSet = AnimatorSet()
        popupAnimSet.playTogether(scaleX, scaleY, alpha)
        popupAnimSet.duration = 600
        popupAnimSet.interpolator = OvershootInterpolator()

        // Animate stars falling from top
        val starAnimators = mutableListOf<Animator>()
        achievementStars.forEachIndexed { index, star ->
            star.visibility = View.VISIBLE
            star.alpha = 0f
            star.translationY = -200f
            star.rotation = 0f

            val starFall = ObjectAnimator.ofFloat(star, "translationY", -200f, 0f)
            val starFade = ObjectAnimator.ofFloat(star, "alpha", 0f, 1f)
            val starRotate = ObjectAnimator.ofFloat(star, "rotation", 0f, 360f)

            val starSet = AnimatorSet()
            starSet.playTogether(starFall, starFade, starRotate)
            starSet.startDelay = (index * 100).toLong() // Stagger the stars
            starSet.duration = 800
            starSet.interpolator = AccelerateDecelerateInterpolator()

            starAnimators.add(starSet)
        }

        // Animate confetti
        val confettiAnimators = mutableListOf<Animator>()
        achievementConfetti.forEachIndexed { index, confetti ->
            confetti.visibility = View.VISIBLE
            confetti.alpha = 1f
            confetti.translationY = 0f
            confetti.translationX = (Math.random() * 200 - 100).toFloat()

            val confettiFall = ObjectAnimator.ofFloat(confetti, "translationY", 0f, 400f)
            val confettiFade = ObjectAnimator.ofFloat(confetti, "alpha", 1f, 0f)
            val confettiRotate = ObjectAnimator.ofFloat(confetti, "rotation", 0f, (Math.random() * 720).toFloat())

            val confettiSet = AnimatorSet()
            confettiSet.playTogether(confettiFall, confettiFade, confettiRotate)
            confettiSet.startDelay = (index * 150).toLong()
            confettiSet.duration = 2000

            confettiAnimators.add(confettiSet)
        }

        // Badge pulse animation - Fixed: Use individual ObjectAnimators with repeat properties
        val badgePulse = ObjectAnimator.ofFloat(achievementBadge, "scaleX", 1f, 1.3f, 1f)
        badgePulse.duration = 1000
        badgePulse.repeatCount = ValueAnimator.INFINITE
        badgePulse.repeatMode = ValueAnimator.REVERSE

        val badgePulseY = ObjectAnimator.ofFloat(achievementBadge, "scaleY", 1f, 1.3f, 1f)
        badgePulseY.duration = 1000
        badgePulseY.repeatCount = ValueAnimator.INFINITE
        badgePulseY.repeatMode = ValueAnimator.REVERSE

        // Start all animations
        popupAnimSet.start()
        starAnimators.forEach { it.start() }
        confettiAnimators.forEach { it.start() }
        badgePulse.start()
        badgePulseY.start()

        // Store badge animators to stop them later
        achievementBadge.tag = listOf(badgePulse, badgePulseY)
    }

    private fun hideAchievementPopup() {
        val fadeOut = ObjectAnimator.ofFloat(achievementPopup, "alpha", 1f, 0f)
        val scaleOut = ObjectAnimator.ofFloat(achievementPopup, "scaleX", 1f, 0f)
        val scaleOutY = ObjectAnimator.ofFloat(achievementPopup, "scaleY", 1f, 0f)

        val hideSet = AnimatorSet()
        hideSet.playTogether(fadeOut, scaleOut, scaleOutY)
        hideSet.duration = 300

        hideSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                achievementPopup.visibility = View.GONE

                // Stop badge animations - Fixed: Handle list of animators
                val badgeAnimators = achievementBadge.tag as? List<ObjectAnimator>
                badgeAnimators?.forEach { animator ->
                    animator.cancel()
                }

                // Hide all animated elements
                achievementStars.forEach { it.visibility = View.GONE }
                achievementConfetti.forEach { it.visibility = View.GONE }
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        hideSet.start()
    }

    private fun playAchievementSound(type: AchievementType) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

            when (type) {
                AchievementType.WORDS_GOAL -> {
                    // Quick ascending celebration
                    toneGen.startTone(ToneGenerator.TONE_DTMF_2, 120)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_5, 120)
                    }, 140)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_8, 200)
                        toneGen.release()
                    }, 280)
                }

                AchievementType.PHRASES_GOAL -> {
                    // Double celebration chime
                    toneGen.startTone(ToneGenerator.TONE_DTMF_6, 150)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_6, 150)
                    }, 200)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_9, 250)
                        toneGen.release()
                    }, 400)
                }

                AchievementType.BOTH_GOALS -> {
                    // EPIC victory fanfare - the big celebration!
                    toneGen.startTone(ToneGenerator.TONE_DTMF_1, 100)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_3, 100)
                    }, 120)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_5, 100)
                    }, 240)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_8, 150)
                    }, 360)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_0, 300) // Triumphant finish
                    }, 520)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_0, 200) // Echo for emphasis
                        toneGen.release()
                    }, 850)
                }

                AchievementType.STREAK_ACHIEVEMENT -> {
                    // Energetic streak sound
                    toneGen.startTone(ToneGenerator.TONE_DTMF_7, 80)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_7, 80)
                    }, 100)
                    Handler(Looper.getMainLooper()).postDelayed({
                        toneGen.startTone(ToneGenerator.TONE_DTMF_9, 200)
                        toneGen.release()
                    }, 200)
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Could not play achievement sound", e)
        }
    }

    private fun setupRealTimeProgressListener() {
        currentUserId?.let { userId ->
            progressListener?.remove()

            progressListener = firestore.collection("test_results")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", getTodayStartTimestamp())
                .whereLessThan("timestamp", getTomorrowStartTimestamp())
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to progress updates", error)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        var wordsRight = 0
                        var phrasesRight = 0

                        for (doc in snapshots.documents) {
                            val testType = doc.getString("testType") ?: ""
                            val correct = doc.getLong("correctAnswers")?.toInt() ?: 0

                            when (testType) {
                                "WORD_TEST" -> wordsRight += correct
                                "PHRASE_TEST" -> phrasesRight += correct
                            }
                        }

                        todayWordsRight = wordsRight
                        todayPhrasesRight = phrasesRight

                        Log.d(TAG, "Real-time progress update: words=$todayWordsRight, phrases=$todayPhrasesRight")

                        runOnUiThread {
                            updateProgressDisplay()
                            checkAndShowGoalAchievement()
                        }
                    }
                }
        }
    }

    private fun checkAndShowGoalAchievement() {
        val wordsAchieved = todayWordsRight >= dailyWordsGoal
        val phrasesAchieved = todayPhrasesRight >= dailyPhrasesGoal

        // IMPORTANT: Check for BOTH goals first (highest priority)
        if (wordsAchieved && phrasesAchieved && !bothGoalsAchievedToday) {
            showAchievementPopup(AchievementType.BOTH_GOALS)
            bothGoalsAchievedToday = true
            // Also set individual flags to prevent showing individual achievements after
            wordsGoalAchievedToday = true
            phrasesGoalAchievedToday = true
        }
        // Only show individual achievements if both goals haven't been achieved
        else if (wordsAchieved && !wordsGoalAchievedToday && !bothGoalsAchievedToday) {
            showAchievementPopup(AchievementType.WORDS_GOAL)
            wordsGoalAchievedToday = true
        }
        else if (phrasesAchieved && !phrasesGoalAchievedToday && !bothGoalsAchievedToday) {
            showAchievementPopup(AchievementType.PHRASES_GOAL)
            phrasesGoalAchievedToday = true
        }
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

        // Goal adjustment buttons
        btnWordsDecrease.setOnClickListener {
            if (dailyWordsGoal > MIN_GOAL_VALUE) {
                dailyWordsGoal--
                updateGoalDisplay()
                resetAchievementFlags()
            }
        }

        btnWordsIncrease.setOnClickListener {
            if (dailyWordsGoal < MAX_GOAL_VALUE) {
                dailyWordsGoal++
                updateGoalDisplay()
                resetAchievementFlags()
            }
        }

        btnPhrasesDecrease.setOnClickListener {
            if (dailyPhrasesGoal > MIN_GOAL_VALUE) {
                dailyPhrasesGoal--
                updateGoalDisplay()
                resetAchievementFlags()
            }
        }

        btnPhrasesIncrease.setOnClickListener {
            if (dailyPhrasesGoal < MAX_GOAL_VALUE) {
                dailyPhrasesGoal++
                updateGoalDisplay()
                resetAchievementFlags()
            }
        }

        btnSaveGoals.setOnClickListener {
            saveGoalsToFirebase()
        }

        btnResetToDefault.setOnClickListener {
            dailyWordsGoal = DEFAULT_WORDS_GOAL
            dailyPhrasesGoal = DEFAULT_PHRASES_GOAL
            updateGoalDisplay()
            resetAchievementFlags()
            Toast.makeText(this, "Goals reset to default values", Toast.LENGTH_SHORT).show()
        }

        // Navigation drawer listeners
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

        navLeaderboard.setOnClickListener {
            closeDrawer()
            navigateToLeaderboardActivity()
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
            finish()
        }

        navChat.setOnClickListener {
            closeDrawer()
            navigateToAiChatActivity()
        }

        navDictionary.setOnClickListener {
            closeDrawer()
            navigateToOfflineQuiz()
        }
    }

    private fun resetAchievementFlags() {
        wordsGoalAchievedToday = false
        phrasesGoalAchievedToday = false
        bothGoalsAchievedToday = false
    }

    private fun loadCurrentGoals() {
        currentUserId?.let { userId ->
            firestore.collection(COLLECTION_USER_GOALS)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        dailyWordsGoal = document.getLong("dailyWordsGoal")?.toInt() ?: DEFAULT_WORDS_GOAL
                        dailyPhrasesGoal = document.getLong("dailyPhrasesGoal")?.toInt() ?: DEFAULT_PHRASES_GOAL
                        Log.d(TAG, "Goals loaded: words=$dailyWordsGoal, phrases=$dailyPhrasesGoal")
                    } else {
                        Log.d(TAG, "No existing goals found, using defaults")
                    }
                    updateGoalDisplay()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error loading goals", e)
                    Toast.makeText(this, "Failed to load goals", Toast.LENGTH_SHORT).show()
                    updateGoalDisplay()
                }
        }
    }

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getTomorrowStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun updateGoalDisplay() {
        tvWordsGoal.text = dailyWordsGoal.toString()
        tvPhrasesGoal.text = dailyPhrasesGoal.toString()
        updateProgressDisplay()
    }

    private fun updateProgressDisplay() {
        tvCurrentProgress.text = "TODAY: $todayWordsRight words, $todayPhrasesRight phrases correct"

        val wordsPercentage = if (dailyWordsGoal > 0) {
            ((todayWordsRight.toFloat() / dailyWordsGoal) * 100).toInt()
        } else 0

        val phrasesPercentage = if (dailyPhrasesGoal > 0) {
            ((todayPhrasesRight.toFloat() / dailyPhrasesGoal) * 100).toInt()
        } else 0

        val progressText = StringBuilder("PROGRESS: Words ${minOf(wordsPercentage, 100)}% | Phrases ${minOf(phrasesPercentage, 100)}%")

        if (todayWordsRight >= dailyWordsGoal && todayPhrasesRight >= dailyPhrasesGoal) {
            progressText.append("\n🎉 DAILY GOALS ACHIEVED! 🎉")
        } else if (todayWordsRight >= dailyWordsGoal) {
            progressText.append("\n✅ Words goal achieved!")
        } else if (todayPhrasesRight >= dailyPhrasesGoal) {
            progressText.append("\n✅ Phrases goal achieved!")
        }

        tvGoalProgress.text = progressText.toString()
    }

    private fun saveGoalsToFirebase() {
        currentUserId?.let { userId ->
            val goalsData = hashMapOf(
                "userId" to userId,
                "dailyWordsGoal" to dailyWordsGoal,
                "dailyPhrasesGoal" to dailyPhrasesGoal,
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_USER_GOALS)
                .document(userId)
                .set(goalsData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Goals saved successfully")
                    Toast.makeText(this, "Goals saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving goals", e)
                    Toast.makeText(this, "Failed to save goals. Please try again.", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Navigation methods (same as original)
    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

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

    private fun navigateToLeaderboardActivity() {
        val intent = Intent(this, LeaderboardActivity::class.java)
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

    private fun navigateToQuotes() {
        val intent = Intent(this, QuotesActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToStatistics() {
        val intent = Intent(this, ProgressActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }



    private fun redirectToLogin() {
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

    fun getCurrentGoals(): GoalsData {
        return GoalsData(dailyWordsGoal, dailyPhrasesGoal)
    }
}

data class GoalsData(
    val dailyWordsGoal: Int,
    val dailyPhrasesGoal: Int
)