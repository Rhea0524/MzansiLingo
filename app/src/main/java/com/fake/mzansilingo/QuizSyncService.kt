package com.fake.mzansilingo

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Background service to sync offline quiz results when network becomes available
 */
class QuizSyncService : Service() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isOnline(context)) {
                Log.d("QuizSyncService", "Network connected! Syncing pending results...")
                syncPendingResults(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Register network receiver
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
        Log.d("QuizSyncService", "Service started and receiver registered")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Try to sync immediately
        syncPendingResults(this)
        return START_STICKY // Restart service if killed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(networkReceiver)
        } catch (e: Exception) {
            Log.e("QuizSyncService", "Error unregistering receiver", e)
        }
    }

    private fun isOnline(context: Context?): Boolean {
        val ctx = context ?: return false
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun syncPendingResults(context: Context?) {
        val ctx = context ?: return
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("QuizSyncService", "No user logged in, cannot sync")
            stopSelf() // Stop service if no user
            return
        }

        if (!isOnline(ctx)) {
            Log.d("QuizSyncService", "Device is offline, waiting for connection")
            return
        }

        val prefs = ctx.getSharedPreferences("offline_quiz_results", Context.MODE_PRIVATE)
        val resultsJson = prefs.getString("pending_results", "[]")
        val type = object : TypeToken<MutableList<OfflineQuizResult>>() {}.type
        val results: MutableList<OfflineQuizResult> = Gson().fromJson(resultsJson, type)

        if (results.isEmpty()) {
            Log.d("QuizSyncService", "No pending results to sync, stopping service")
            stopSelf() // Stop service when nothing to sync
            return
        }

        Log.d("QuizSyncService", "Syncing ${results.size} pending results to Firebase")

        val userId = currentUser.uid
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var syncedCount = 0
        val unsyncedResults = results.filter { !it.synced }

        unsyncedResults.forEach { result ->
            val testResult = hashMapOf(
                "userId" to userId,
                "testType" to "WORD_TEST",
                "correctAnswers" to result.correctAnswers.toLong(),
                "totalQuestions" to result.totalQuestions.toLong(),
                "score" to result.correctAnswers.toLong(),
                "language" to result.language,
                "timestamp" to com.google.firebase.Timestamp(Date(result.timestamp)),
                "date" to dateFormat.format(Date(result.timestamp)),
                "source" to "offline_quiz"
            )

            firestore.collection("test_results")
                .add(testResult)
                .addOnSuccessListener { documentReference ->
                    Log.d("QuizSyncService", "Synced result ${documentReference.id}")
                    syncedCount++

                    // Remove synced results
                    val remainingResults = results.filter { it != result }

                    val editor = prefs.edit()
                    editor.putString("pending_results", Gson().toJson(remainingResults))
                    editor.apply()

                    // Log daily login
                    logDailyLogin(userId, dateFormat.format(Date(result.timestamp)))

                    if (syncedCount == unsyncedResults.size) {
                        Log.d("QuizSyncService", "All results synced successfully!")
                        stopSelf() // Stop service when all synced
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QuizSyncService", "Error syncing result", e)
                }
        }
    }

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
                    firestore.collection("daily_logins")
                        .add(loginData)
                        .addOnSuccessListener {
                            Log.d("QuizSyncService", "Daily login logged for $date")
                        }
                }
            }
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, QuizSyncService::class.java)
            context.startService(intent)
        }
    }
}