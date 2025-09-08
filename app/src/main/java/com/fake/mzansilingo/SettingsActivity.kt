package com.fake.mzansilingo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class SettingsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // Navigation drawer items
    private lateinit var navHome: TextView
    private lateinit var navLanguage: TextView
    private lateinit var navWords: TextView
    private lateinit var navPhrases: TextView
    private lateinit var navProgress: TextView
    private lateinit var navVisibility: TextView
    private lateinit var navSettings: TextView
    private lateinit var navProfile: TextView
    private lateinit var navBack: ImageView
    private lateinit var navChat: ImageView
    private lateinit var navDictionary: ImageView

    companion object {
        private const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupViews()
        setupClickListeners()
        setupNavigationDrawer()
    }

    private fun setupViews() {
        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize navigation drawer items
        navHome = findViewById(R.id.nav_home)
        navLanguage = findViewById(R.id.nav_language)
        navWords = findViewById(R.id.nav_words)
        navPhrases = findViewById(R.id.nav_phrases)
        navProgress = findViewById(R.id.nav_progress)
        navVisibility = findViewById(R.id.nav_visibility)
        navSettings = findViewById(R.id.nav_settings)
        navProfile = findViewById(R.id.nav_profile)
        navBack = findViewById(R.id.nav_back)
        navChat = findViewById(R.id.nav_chat)
        navDictionary = findViewById(R.id.nav_dictionary)
    }

    private fun setupClickListeners() {
        // Menu button to open drawer
        findViewById<ImageView>(R.id.btn_menu).setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Settings buttons
        findViewById<MaterialButton>(R.id.btn_user_profile).setOnClickListener {
            navigateToProfile()
        }

        findViewById<MaterialButton>(R.id.btn_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_privacy_security).setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_help_support).setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_visibility_modes).setOnClickListener {
            navigateToVisibilityModes()
        }

        findViewById<MaterialButton>(R.id.btn_about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_log_out).setOnClickListener {
            // Handle logout
            handleLogout()
        }
    }

    private fun setupNavigationDrawer() {
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
            closeDrawer()
            navigateToProgressActivity()
        }

        navVisibility.setOnClickListener {
            closeDrawer()
            navigateToVisibilityModes()
        }

        navSettings.setOnClickListener {
            // Already in Settings, just close drawer
            closeDrawer()
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

    private fun navigateToProgressActivity() {
        val intent = Intent(this, ProgressActivity::class.java)
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

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToOfflineQuiz() {
        val intent = Intent(this, OfflineActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun handleLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                try {
                    // Sign out from Firebase
                    FirebaseAuth.getInstance().signOut()

                    // Also clear Google sign-in client if you use it
                    GoogleSignIn.getClient(
                        this,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                    ).signOut()

                    // Clear user session/preferences
                    val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        clear()
                        apply()
                    }

                    // Navigate to login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                    Log.d(TAG, "User logged out successfully")

                } catch (e: Exception) {
                    Log.e(TAG, "Error during logout", e)
                    Toast.makeText(this, "Error during logout: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}