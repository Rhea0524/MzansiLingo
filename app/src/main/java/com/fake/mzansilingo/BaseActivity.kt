package com.fake.mzansilingo

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

/**
 * Base Activity that applies language settings to all activities
 * All activities in the app should extend this class
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Language is already applied via attachBaseContext
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("home_language", "English") ?: "English"
        super.attachBaseContext(updateLocale(newBase, savedLanguage))
    }

    private fun updateLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "English" -> Locale("en")
            "isiZulu" -> Locale("zu")

            else -> Locale("en")
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Call this method after changing language to refresh the current activity
     */
    protected fun applyLanguageChange() {
        recreate()
    }
}