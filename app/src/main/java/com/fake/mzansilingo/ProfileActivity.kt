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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSave: MaterialButton
    private lateinit var btnMenu: ImageView

    // Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            // User not logged in, redirect to login
            redirectToLogin()
            return
        }

        initViews()
        setupPasswordField()
        setupLanguageSpinner()
        setupClickListeners()
        loadUserDataFromFirebase()
    }

    private fun redirectToLogin() {
        Toast.makeText(this, "Please log in to access your profile", Toast.LENGTH_SHORT).show()
        // Uncomment when you have LoginActivity
        // val intent = Intent(this, LoginActivity::class.java)
        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // startActivity(intent)
        finish()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        etFullName = findViewById(R.id.et_full_name)
        etEmail = findViewById(R.id.et_email)
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        spinnerLanguage = findViewById(R.id.spinner_language)
        btnSave = findViewById(R.id.btn_save)
        btnMenu = findViewById(R.id.btn_menu)
    }

    private fun setupPasswordField() {
        // Make password field non-editable
        etPassword.isEnabled = false
        etPassword.isFocusable = false
        etPassword.isFocusableInTouchMode = false

        // Set a placeholder to indicate passwords can't be changed here
        etPassword.hint = "Password cannot be changed from profile"
        etPassword.setText("") // Keep it empty for security

        // Set input type (though it won't be editable)
        etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        etPassword.setBackgroundResource(R.drawable.profile_input_disabled_background)
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf(
            "English", "Afrikaans", "Zulu", "Xhosa",
            "Sotho", "Tswana", "Tsonga", "Venda",
            "Ndebele", "Swati", "Pedi"
        )

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
        // Home navigation
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToHome()
        }

        // Language navigation
        findViewById<TextView>(R.id.nav_language).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToLanguageSelection()
        }

        // Words navigation
        findViewById<TextView>(R.id.nav_words).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToWordsActivity()
        }

        // Phrases navigation
        findViewById<TextView>(R.id.nav_phrases).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToPhrasesActivity()
        }

        // Progress navigation
        findViewById<TextView>(R.id.nav_progress).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProgressActivity()
        }



        // Settings navigation
        findViewById<TextView>(R.id.nav_settings).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToSettings()
        }

        // Profile navigation (current page - just close drawer)
        findViewById<TextView>(R.id.nav_profile).setOnClickListener {
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

    // Navigation methods matching HomeActivity functionality
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

            // Load email from Firebase Auth
            val email = user.email ?: ""
            etEmail.setText(email)
            Log.d("ProfileActivity", "Email loaded: $email")

            // Load display name from Firebase Auth (if available)
            val displayName = user.displayName ?: ""
            etFullName.setText(displayName)
            Log.d("ProfileActivity", "Display name loaded: $displayName")

            // Load additional data from SharedPreferences
            loadAdditionalDataFromPrefs()

            // Password field is disabled, so we don't need to set anything
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

        // Set language spinner selection
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

        // Validate input (removed password validation since it's not editable)
        when {
            fullName.isEmpty() -> {
                etFullName.error = "Full name is required"
                etFullName.requestFocus()
                return
            }
            email.isEmpty() -> {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return
            }
            !isValidEmail(email) -> {
                etEmail.error = "Please enter a valid email"
                etEmail.requestFocus()
                return
            }
            username.isEmpty() -> {
                etUsername.error = "Username is required"
                etUsername.requestFocus()
                return
            }
        }

        // Show saving state
        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        auth.currentUser?.let { user ->
            // Update display name in Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    Log.d("ProfileActivity", "Display name updated successfully")

                    // Update email if changed
                    val currentEmail = user.email
                    if (currentEmail != email) {
                        updateEmailInAuth(email, username, language)
                    } else {
                        // If email didn't change, just save additional data
                        saveAdditionalDataToPrefs(username, language)
                        showSaveSuccess()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileActivity", "Error updating profile", exception)
                    Toast.makeText(this, "Error updating profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                    resetSaveButton()
                }
        }
    }

    private fun updateEmailInAuth(newEmail: String, username: String, language: String) {
        auth.currentUser?.updateEmail(newEmail)
            ?.addOnSuccessListener {
                Log.d("ProfileActivity", "Email updated successfully")

                // Save additional data
                saveAdditionalDataToPrefs(username, language)
                showSaveSuccess()
            }
            ?.addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Error updating email", exception)
                Toast.makeText(this, "Error updating email: ${exception.message}. You may need to re-authenticate.", Toast.LENGTH_LONG).show()

                // Still save other data even if email update failed
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
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
        resetSaveButton()
    }

    private fun resetSaveButton() {
        btnSave.isEnabled = true
        btnSave.text = "Save"
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}