package com.fake.mzansilingo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manager class for handling offline quiz data storage and retrieval
 */
class OfflineQuizManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("offline_quiz_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_QUESTIONS_PREFIX = "questions_"
        private const val KEY_TOTAL_ANSWERED = "total_questions_answered"
        private const val KEY_TOTAL_CORRECT = "total_correct_answers"
        private const val KEY_BEST_SCORE = "best_score_"
        private const val KEY_QUIZ_ATTEMPTS = "quiz_attempts_"
    }

    /**
     * Save custom questions for a language
     */
    fun saveQuestions(language: String, questions: List<QuizQuestion>) {
        val json = gson.toJson(questions)
        prefs.edit().putString("$KEY_QUESTIONS_PREFIX$language", json).apply()
    }

    /**
     * Load questions for a specific language
     */
    fun loadQuestions(language: String): List<QuizQuestion>? {
        val json = prefs.getString("$KEY_QUESTIONS_PREFIX$language", null)
        return if (json != null) {
            val type = object : TypeToken<List<QuizQuestion>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    /**
     * Get total questions answered across all quizzes
     */
    fun getTotalQuestionsAnswered(): Int {
        return prefs.getInt(KEY_TOTAL_ANSWERED, 0)
    }

    /**
     * Get total correct answers across all quizzes
     */
    fun getTotalCorrectAnswers(): Int {
        return prefs.getInt(KEY_TOTAL_CORRECT, 0)
    }

    /**
     * Get accuracy percentage
     */
    fun getAccuracyPercentage(): Int {
        val total = getTotalQuestionsAnswered()
        if (total == 0) return 0
        return ((getTotalCorrectAnswers().toFloat() / total) * 100).toInt()
    }

    /**
     * Save quiz result
     */
    fun saveQuizResult(language: String, score: Int, totalQuestions: Int) {
        val editor = prefs.edit()

        // Update totals
        editor.putInt(KEY_TOTAL_ANSWERED, getTotalQuestionsAnswered() + totalQuestions)
        editor.putInt(KEY_TOTAL_CORRECT, getTotalCorrectAnswers() + score)

        // Update best score
        val currentBest = prefs.getInt("$KEY_BEST_SCORE$language", 0)
        if (score > currentBest) {
            editor.putInt("$KEY_BEST_SCORE$language", score)
        }

        // Update attempts
        val attempts = prefs.getInt("$KEY_QUIZ_ATTEMPTS$language", 0)
        editor.putInt("$KEY_QUIZ_ATTEMPTS$language", attempts + 1)

        editor.apply()
    }

    /**
     * Get best score for a language
     */
    fun getBestScore(language: String): Int {
        return prefs.getInt("$KEY_BEST_SCORE$language", 0)
    }

    /**
     * Get number of quiz attempts for a language
     */
    fun getQuizAttempts(language: String): Int {
        return prefs.getInt("$KEY_QUIZ_ATTEMPTS$language", 0)
    }

    /**
     * Reset all quiz statistics
     */
    fun resetStatistics() {
        prefs.edit().clear().apply()
    }

    /**
     * Get quiz statistics for all languages
     */
    fun getAllStatistics(): Map<String, QuizStats> {
        val languages = listOf("afrikaans", "zulu", "xhosa", "sotho", "tswana")
        return languages.associateWith { language ->
            QuizStats(
                language = language,
                bestScore = getBestScore(language),
                attempts = getQuizAttempts(language)
            )
        }
    }
}

data class QuizStats(
    val language: String,
    val bestScore: Int,
    val attempts: Int
)

/**
 * Extension function to add custom questions to the quiz database
 */
fun OfflineQuizManager.addCustomQuestions(language: String, newQuestions: List<QuizQuestion>) {
    val existing = loadQuestions(language)?.toMutableList() ?: mutableListOf()
    existing.addAll(newQuestions)
    saveQuestions(language, existing)
}