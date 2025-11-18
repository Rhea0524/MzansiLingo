package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvProfileTitle: TextView
    private lateinit var tvLabelFullName: TextView
    private lateinit var tvLabelEmail: TextView
    private lateinit var tvLabelUsername: TextView
    private lateinit var tvLabelPassword: TextView
    private lateinit var tvLabelHomeLanguage: TextView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSave: MaterialButton
    private lateinit var btnMenu: ImageView

    // Navigation drawer items
    private lateinit var navHome: TextView
    private lateinit var navLanguage: TextView
    private lateinit var navWords: TextView
    private lateinit var navPhrases: TextView
    private lateinit var navProgress: TextView
    private lateinit var navSettings: TextView
    private lateinit var navProfile: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            redirectToLogin()
            return
        }

        initViews()
        setupUI()
        setupPasswordField()
        setupLanguageSpinner()
        setupClickListeners()
        loadUserDataFromFirebase()
    }

    private fun redirectToLogin() {
        Toast.makeText(this, getString(R.string.profile_login_required), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        tvProfileTitle = findViewById(R.id.tv_profile_title)

        // Initialize label TextViews
        tvLabelFullName = findViewById(R.id.tv_label_full_name)
        tvLabelEmail = findViewById(R.id.tv_label_email)
        tvLabelUsername = findViewById(R.id.tv_label_username)
        tvLabelPassword = findViewById(R.id.tv_label_password)
        tvLabelHomeLanguage = findViewById(R.id.tv_label_home_language)

        // Initialize input fields
        etFullName = findViewById(R.id.et_full_name)
        etEmail = findViewById(R.id.et_email)
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        spinnerLanguage = findViewById(R.id.spinner_language)
        btnSave = findViewById(R.id.btn_save)
        btnMenu = findViewById(R.id.btn_menu)

        // Navigation drawer items
        navHome = findViewById(R.id.nav_home)
        navLanguage = findViewById(R.id.nav_language)
        navWords = findViewById(R.id.nav_words)
        navPhrases = findViewById(R.id.nav_phrases)
        navProgress = findViewById(R.id.nav_progress)
        navSettings = findViewById(R.id.nav_settings)
        navProfile = findViewById(R.id.nav_profile)
    }

    private fun setupUI() {
        // Set title using string resources
        tvProfileTitle.text = getString(R.string.profile_title)

        // Set label texts using string resources
        tvLabelFullName.text = getString(R.string.profile_label_full_name)
        tvLabelEmail.text = getString(R.string.profile_label_email)
        tvLabelUsername.text = getString(R.string.profile_label_username)
        tvLabelPassword.text = getString(R.string.profile_label_password)
        tvLabelHomeLanguage.text = getString(R.string.profile_label_home_language)

        // Set button text using string resources
        btnSave.text = getString(R.string.btn_save_profile)

        // Set EditText hints using string resources
        etFullName.hint = getString(R.string.profile_hint_full_name)
        etEmail.hint = getString(R.string.profile_hint_email)
        etUsername.hint = getString(R.string.profile_hint_username)
        etPassword.hint = getString(R.string.profile_hint_password)

        // Set navigation drawer texts using string resources
        navHome.text = getString(R.string.nav_home).uppercase()
        navLanguage.text = getString(R.string.nav_language).uppercase()
        navWords.text = getString(R.string.nav_words).uppercase()
        navPhrases.text = getString(R.string.nav_phrases).uppercase()
        navProgress.text = getString(R.string.nav_progress).uppercase()
        navSettings.text = getString(R.string.nav_settings).uppercase()
        navProfile.text = getString(R.string.nav_profile).uppercase()
    }

    private fun setupPasswordField() {
        // Make password field non-editable
        etPassword.isEnabled = false
        etPassword.isFocusable = false
        etPassword.isFocusableInTouchMode = false

        // Set a placeholder to indicate passwords can't be changed here
        etPassword.hint = getString(R.string.profile_password_disabled)
        etPassword.setText("") // Keep it empty for security

        // Set input type (though it won't be editable)
        etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        etPassword.setBackgroundResource(R.drawable.profile_input_disabled_background)
    }

    private fun setupLanguageSpinner() {
        val languages = resources.getStringArray(R.array.profile_languages)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter
    }

    private fun setupClickListeners() {
        // Menu button - open drawer
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Save button
        btnSave.setOnClickListener { saveUserProfile() }

        // Navigation drawer items
        setupNavigation()
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToHome()
        }

        navLanguage.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToLanguageSelection()
        }

        navWords.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToWordsActivity()
        }

        navPhrases.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToPhrasesActivity()
        }

        navProgress.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProgressActivity()
        }

        navSettings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToSettings()
        }

        navProfile.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Bottom navigation icons
        findViewById<ImageView>(R.id.nav_dictionary).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToOfflineQuiz()
        }

        findViewById<ImageView>(R.id.nav_back).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            onBackPressed()
        }

        findViewById<ImageView>(R.id.nav_chat).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToAiChatActivity()
        }
    }

    // Navigation methods
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLanguageSelection() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
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

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAiChatActivity() {
        val intent = Intent(this, AiChatActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun navigateToOfflineQuiz() {
        val intent = Intent(this, OfflineActivity::class.java)
        intent.putExtra("LANGUAGE", "afrikaans")
        startActivity(intent)
    }

    private fun loadUserDataFromFirebase() {
        auth.currentUser?.let { user ->
            Log.d("ProfileActivity", "Loading user data for: ${user.uid}")

            val email = user.email ?: ""
            etEmail.setText(email)
            Log.d("ProfileActivity", "Email loaded: $email")

            val displayName = user.displayName ?: ""
            etFullName.setText(displayName)
            Log.d("ProfileActivity", "Display name loaded: $displayName")

            loadAdditionalDataFromPrefs()

            Log.d("ProfileActivity", "User data loading completed")
        } ?: run {
            Log.e("ProfileActivity", "No current user found")
        }
    }

    private fun loadAdditionalDataFromPrefs() {
        val userId = auth.currentUser?.uid ?: return
        val prefs = getSharedPreferences("UserProfile_$userId", MODE_PRIVATE)

        val savedUsername = prefs.getString("username", "") ?: ""
        val savedLanguage = prefs.getString("homeLanguage", "English") ?: "English"

        Log.d("ProfileActivity", "Loading from prefs - Username: $savedUsername, Language: $savedLanguage")

        etUsername.setText(savedUsername)

        val adapter = spinnerLanguage.adapter as? ArrayAdapter<String>
        adapter?.let { spinnerAdapter ->
            val position = spinnerAdapter.getPosition(savedLanguage)
            if (position >= 0) {
                spinnerLanguage.setSelection(position)
                Log.d("ProfileActivity", "Language spinner set to position: $position ($savedLanguage)")
            }
        }
    }

    private fun saveUserProfile() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val language = spinnerLanguage.selectedItem.toString()

        Log.d("ProfileActivity", "Saving profile - Name: $fullName, Email: $email, Username: $username")

        // Validate input
        when {
            fullName.isEmpty() -> {
                etFullName.error = getString(R.string.error_full_name_required)
                etFullName.requestFocus()
                return
            }
            email.isEmpty() -> {
                etEmail.error = getString(R.string.error_email_required)
                etEmail.requestFocus()
                return
            }
            !isValidEmail(email) -> {
                etEmail.error = getString(R.string.error_valid_email)
                etEmail.requestFocus()
                return
            }
            username.isEmpty() -> {
                etUsername.error = getString(R.string.error_username_required)
                etUsername.requestFocus()
                return
            }
        }

        // Show saving state
        btnSave.isEnabled = false
        btnSave.text = getString(R.string.profile_saving)

        auth.currentUser?.let { user ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    Log.d("ProfileActivity", "Display name updated successfully")

                    val currentEmail = user.email
                    if (currentEmail != email) {
                        updateEmailInAuth(email, username, language)
                    } else {
                        saveAdditionalDataToPrefs(username, language)
                        showSaveSuccess()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileActivity", "Error updating profile", exception)
                    Toast.makeText(this, getString(R.string.profile_error_updating, exception.message), Toast.LENGTH_SHORT).show()
                    resetSaveButton()
                }
        }
    }

    private fun updateEmailInAuth(newEmail: String, username: String, language: String) {
        auth.currentUser?.updateEmail(newEmail)
            ?.addOnSuccessListener {
                Log.d("ProfileActivity", "Email updated successfully")
                saveAdditionalDataToPrefs(username, language)
                showSaveSuccess()
            }
            ?.addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Error updating email", exception)
                Toast.makeText(this, getString(R.string.profile_error_email_update, exception.message), Toast.LENGTH_LONG).show()
                saveAdditionalDataToPrefs(username, language)
                resetSaveButton()
            }
    }

    private fun saveAdditionalDataToPrefs(username: String, language: String) {
        val userId = auth.currentUser?.uid ?: return
        val prefs = getSharedPreferences("UserProfile_$userId", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("username", username)
            putString("homeLanguage", language)
            apply()
        }
        Log.d("ProfileActivity", "Additional data saved to preferences - Username: $username, Language: $language")
    }

    private fun showSaveSuccess() {
        Toast.makeText(this, getString(R.string.profile_saved_success), Toast.LENGTH_SHORT).show()
        resetSaveButton()
    }

    private fun resetSaveButton() {
        btnSave.isEnabled = true
        btnSave.text = getString(R.string.btn_save_profile)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the activity if language changed
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = prefs.getString("home_language", "English") ?: "English"

        val currentLocale = resources.configuration.locales[0].language
        val expectedLocale = when (currentLanguage) {
            "English" -> "en"
            "isiZulu" -> "zu"
            else -> "en"
        }

        if (currentLocale != expectedLocale) {
            recreate()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}