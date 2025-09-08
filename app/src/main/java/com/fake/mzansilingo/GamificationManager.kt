package com.fake.mzansilingo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class GamificationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gamification_prefs", Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    companion object {
        private const val PREF_LAST_LOGIN_DATE = "last_login_date"
        private const val PREF_CURRENT_STREAK = "current_streak"
        private const val PREF_BEST_STREAK = "best_streak"
        private const val PREF_TOTAL_DAYS = "total_days"
        private const val PREF_SHOW_WELCOME_BACK = "show_welcome_back"
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

    data class StreakData(
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val totalDays: Int = 0,
        val lastLoginDate: String = "",
        val isNewDay: Boolean = false,
        val shouldShowWelcome: Boolean = false
    )

    interface GamificationCallback {
        fun onStreakDataLoaded(streakData: StreakData)
        fun onStreakUpdated(streakData: StreakData)
        fun onError(error: String)
    }

    fun checkDailyLogin(callback: GamificationCallback) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback.onError("User not authenticated")
            return
        }

        val today = getCurrentDate()
        val lastLoginDate = prefs.getString(PREF_LAST_LOGIN_DATE, "") ?: ""

        Log.d("GamificationManager", "Today: $today, Last login: $lastLoginDate")

        if (lastLoginDate != today) {
            // New day login
            updateStreakForNewDay(today, lastLoginDate, callback)
        } else {
            // Same day login
            val streakData = getCurrentStreakData()
            callback.onStreakDataLoaded(streakData.copy(shouldShowWelcome = false))
        }
    }

    private fun updateStreakForNewDay(today: String, lastLoginDate: String, callback: GamificationCallback) {
        val currentStreak = prefs.getInt(PREF_CURRENT_STREAK, 0)
        val bestStreak = prefs.getInt(PREF_BEST_STREAK, 0)
        val totalDays = prefs.getInt(PREF_TOTAL_DAYS, 0)

        val newStreak = if (isConsecutiveDay(lastLoginDate, today)) {
            currentStreak + 1
        } else if (lastLoginDate.isEmpty()) {
            1 // First time user
        } else {
            1 // Streak broken, start over
        }

        val newBestStreak = maxOf(bestStreak, newStreak)
        val newTotalDays = totalDays + 1

        // Save locally
        prefs.edit().apply {
            putString(PREF_LAST_LOGIN_DATE, today)
            putInt(PREF_CURRENT_STREAK, newStreak)
            putInt(PREF_BEST_STREAK, newBestStreak)
            putInt(PREF_TOTAL_DAYS, newTotalDays)
            putBoolean(PREF_SHOW_WELCOME_BACK, true)
            apply()
        }

        // Save to Firebase
        saveStreakToFirebase(newStreak, newBestStreak, newTotalDays, today)

        val streakData = StreakData(
            currentStreak = newStreak,
            bestStreak = newBestStreak,
            totalDays = newTotalDays,
            lastLoginDate = today,
            isNewDay = true,
            shouldShowWelcome = true
        )

        callback.onStreakUpdated(streakData)
    }

    private fun saveStreakToFirebase(currentStreak: Int, bestStreak: Int, totalDays: Int, date: String) {
        val currentUser = auth.currentUser ?: return

        val streakData = mapOf(
            "currentStreak" to currentStreak,
            "bestStreak" to bestStreak,
            "totalDays" to totalDays,
            "lastLoginDate" to date,
            "updatedAt" to System.currentTimeMillis()
        )

        database.child("users").child(currentUser.uid).child("gamification").setValue(streakData)
            .addOnSuccessListener {
                Log.d("GamificationManager", "Streak data saved to Firebase")
            }
            .addOnFailureListener { e ->
                Log.e("GamificationManager", "Failed to save streak data", e)
            }
    }

    fun loadStreakFromFirebase(callback: GamificationCallback) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback.onError("User not authenticated")
            return
        }

        database.child("users").child(currentUser.uid).child("gamification")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val currentStreak = snapshot.child("currentStreak").getValue(Int::class.java) ?: 0
                        val bestStreak = snapshot.child("bestStreak").getValue(Int::class.java) ?: 0
                        val totalDays = snapshot.child("totalDays").getValue(Int::class.java) ?: 0
                        val lastLoginDate = snapshot.child("lastLoginDate").getValue(String::class.java) ?: ""

                        // Update local preferences
                        prefs.edit().apply {
                            putInt(PREF_CURRENT_STREAK, currentStreak)
                            putInt(PREF_BEST_STREAK, bestStreak)
                            putInt(PREF_TOTAL_DAYS, totalDays)
                            putString(PREF_LAST_LOGIN_DATE, lastLoginDate)
                            apply()
                        }

                        val streakData = StreakData(
                            currentStreak = currentStreak,
                            bestStreak = bestStreak,
                            totalDays = totalDays,
                            lastLoginDate = lastLoginDate,
                            shouldShowWelcome = false
                        )

                        callback.onStreakDataLoaded(streakData)
                    } else {
                        // No data in Firebase, use local data
                        val streakData = getCurrentStreakData()
                        callback.onStreakDataLoaded(streakData)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GamificationManager", "Failed to load streak data", error.toException())
                    // Fallback to local data
                    val streakData = getCurrentStreakData()
                    callback.onStreakDataLoaded(streakData)
                }
            })
    }

    private fun getCurrentStreakData(): StreakData {
        return StreakData(
            currentStreak = prefs.getInt(PREF_CURRENT_STREAK, 0),
            bestStreak = prefs.getInt(PREF_BEST_STREAK, 0),
            totalDays = prefs.getInt(PREF_TOTAL_DAYS, 0),
            lastLoginDate = prefs.getString(PREF_LAST_LOGIN_DATE, "") ?: "",
            shouldShowWelcome = prefs.getBoolean(PREF_SHOW_WELCOME_BACK, false)
        )
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date())
    }

    private fun isConsecutiveDay(lastDate: String, currentDate: String): Boolean {
        if (lastDate.isEmpty()) return false

        try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val lastDateObj = sdf.parse(lastDate) ?: return false
            val currentDateObj = sdf.parse(currentDate) ?: return false

            val diffInMillis = currentDateObj.time - lastDateObj.time
            val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

            return diffInDays == 1L
        } catch (e: Exception) {
            Log.e("GamificationManager", "Error parsing dates", e)
            return false
        }
    }

    fun markWelcomeShown() {
        prefs.edit().putBoolean(PREF_SHOW_WELCOME_BACK, false).apply()
    }

    fun getMotivationalMessage(streak: Int): String {
        return when {
            streak == 1 -> "Great start! You've begun your learning journey!"
            streak in 2..6 -> "Well done! You now have a $streak day streak!"
            streak in 7..13 -> "Fantastic! You're building a great habit - $streak days strong!"
            streak in 14..29 -> "Amazing! Your dedication is showing - $streak day streak!"
            streak in 30..99 -> "Incredible! You're a language learning champion - $streak days!"
            streak >= 100 -> "Legendary! You're unstoppable - $streak day streak!"
            else -> "Welcome back to your language learning journey!"
        }
    }

    fun getStreakStars(streak: Int): Int {
        return minOf(5, maxOf(1, (streak + 2) / 3)) // 1-5 stars based on streak
    }
}