package com.fake.mzansilingo

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
        private const val COLLECTION_USER_GOALS = "user_goals"
        private const val DEFAULT_WORDS_GOAL = 10
        private const val DEFAULT_PHRASES_GOAL = 5

        // Default messages for when Firebase isn't available
        private val defaultMorningMessages = listOf(
            "Good morning! Ready to start learning?",
            "Rise and shine! Time for language practice",
            "Morning motivation: Every word counts!",
            "Start your day with new vocabulary!"
        )

        private val defaultEveningMessages = listOf(
            "Evening practice time! Wind down with learning",
            "End your day with language skills",
            "Consistent practice leads to fluency!",
            "Quick evening review session?"
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: "morning"
        Log.d(TAG, "Received notification broadcast: $type")

        when (type) {
            "morning" -> checkProgressAndShowMorningNotification(context)
            "evening" -> checkProgressAndShowEveningNotification(context)
            "test" -> showSimpleNotification(context, "Test Notification", "This is a test notification!")
            else -> showSimpleNotification(context, "MzansiLingo Reminder", "Time to practice your language skills!")
        }
    }

    private fun checkProgressAndShowMorningNotification(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            showSimpleNotification(context, "Good Morning!", defaultMorningMessages.random())
            return
        }

        // Get user's goals first
        firestore.collection(COLLECTION_USER_GOALS)
            .document(userId)
            .get()
            .addOnSuccessListener { goalsDoc ->
                val wordsGoal = goalsDoc.getLong("dailyWordsGoal")?.toInt() ?: DEFAULT_WORDS_GOAL
                val phrasesGoal = goalsDoc.getLong("dailyPhrasesGoal")?.toInt() ?: DEFAULT_PHRASES_GOAL

                // Then check today's progress
                checkTodayProgressAndNotify(context, userId, wordsGoal, phrasesGoal, true)
            }
            .addOnFailureListener {
                showSimpleNotification(context, "Good Morning!", defaultMorningMessages.random())
            }
    }

    private fun checkProgressAndShowEveningNotification(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            showSimpleNotification(context, "Evening Check-in", defaultEveningMessages.random())
            return
        }

        // Get user's goals first
        firestore.collection(COLLECTION_USER_GOALS)
            .document(userId)
            .get()
            .addOnSuccessListener { goalsDoc ->
                val wordsGoal = goalsDoc.getLong("dailyWordsGoal")?.toInt() ?: DEFAULT_WORDS_GOAL
                val phrasesGoal = goalsDoc.getLong("dailyPhrasesGoal")?.toInt() ?: DEFAULT_PHRASES_GOAL

                // Then check today's progress
                checkTodayProgressAndNotify(context, userId, wordsGoal, phrasesGoal, false)
            }
            .addOnFailureListener {
                showSimpleNotification(context, "Evening Reminder", defaultEveningMessages.random())
            }
    }

    private fun checkTodayProgressAndNotify(
        context: Context,
        userId: String,
        wordsGoal: Int,
        phrasesGoal: Int,
        isMorning: Boolean
    ) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("test_results")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", getTodayStartTimestamp())
            .whereLessThan("timestamp", getTomorrowStartTimestamp())
            .get()
            .addOnSuccessListener { snapshots ->
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

                // Determine notification based on progress and time
                if (isMorning) {
                    showMorningNotification(context, wordsRight, phrasesRight, wordsGoal, phrasesGoal)
                } else {
                    showEveningNotification(context, wordsRight, phrasesRight, wordsGoal, phrasesGoal)
                }
            }
            .addOnFailureListener {
                val fallbackMessage = if (isMorning) {
                    defaultMorningMessages.random()
                } else {
                    defaultEveningMessages.random()
                }
                showSimpleNotification(context,
                    if (isMorning) "Good Morning!" else "Evening Reminder",
                    fallbackMessage)
            }
    }

    private fun showMorningNotification(
        context: Context,
        wordsRight: Int,
        phrasesRight: Int,
        wordsGoal: Int,
        phrasesGoal: Int
    ) {
        val wordsAchieved = wordsRight >= wordsGoal
        val phrasesAchieved = phrasesRight >= phrasesGoal

        val (title, message) = when {
            wordsAchieved && phrasesAchieved -> {
                "Amazing Progress!" to "You've already achieved both daily goals! Keep up the fantastic work!"
            }
            wordsAchieved -> {
                "Words Goal Complete!" to "Great job on words! Try some phrases to complete your daily goals."
            }
            phrasesAchieved -> {
                "Phrases Goal Complete!" to "Excellent phrase work! Practice some words to complete your goals."
            }
            else -> {
                "Good Morning, Learner!" to "Ready to master ${wordsGoal - wordsRight} words and ${phrasesGoal - phrasesRight} phrases today?"
            }
        }

        showNotification(context, title, message)
    }

    private fun showEveningNotification(
        context: Context,
        wordsRight: Int,
        phrasesRight: Int,
        wordsGoal: Int,
        phrasesGoal: Int
    ) {
        val wordsAchieved = wordsRight >= wordsGoal
        val phrasesAchieved = phrasesRight >= phrasesGoal
        val wordsProgress = if (wordsGoal > 0) (wordsRight.toFloat() / wordsGoal * 100).toInt() else 100
        val phrasesProgress = if (phrasesGoal > 0) (phrasesRight.toFloat() / phrasesGoal * 100).toInt() else 100

        val (title, message) = when {
            wordsAchieved && phrasesAchieved -> {
                "Daily Champion!" to "Incredible! You've mastered both goals today. You're unstoppable!"
            }

            wordsProgress >= 80 && phrasesProgress >= 80 -> {
                "So Close!" to "You're almost there! Just a few more to complete both goals."
            }

            wordsAchieved -> {
                "Words Mastered!" to "Great work on words! Need ${phrasesGoal - phrasesRight} more phrases to complete the day."
            }

            phrasesAchieved -> {
                "Phrases Complete!" to "Awesome phrase work! Need ${wordsGoal - wordsRight} more words for a perfect day."
            }

            wordsProgress >= 50 || phrasesProgress >= 50 -> {
                "You're Halfway There!" to "Great progress today! Don't let your efforts go to waste - finish strong!"
            }

            wordsRight > 0 || phrasesRight > 0 -> {
                "Good Start!" to "You've made some progress today. A few more questions to reach your goals?"
            }

            else -> {
                "Don't Miss Out!" to "You haven't practiced today yet. Even 5 minutes can make a difference!"
            }
        }

        showNotification(context, title, message)
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

    private fun showSimpleNotification(context: Context, title: String, message: String) {
        showNotification(context, title, message)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open the app when notification is tapped
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "MZANSILINGO_REMINDERS")
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}