package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fake.mzansilingo.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var gamificationManager: GamificationManager
    private lateinit var firestore: FirebaseFirestore
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth, Firestore, and Gamification Manager
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        gamificationManager = GamificationManager(this)

        // Setup language dropdown
        setupLanguageDropdown()

        // Handle Sign Up button
        binding.btnSignUp.setOnClickListener {
            val name = binding.etFullName.text?.toString().orEmpty().trim()
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            val user = binding.etUsername.text?.toString().orEmpty().trim()
            val pass = binding.etPassword.text?.toString().orEmpty()
            val homeLang =
                (binding.tilHomeLang.editText as AutoCompleteTextView).text.toString().trim()

            if (validateInput(name, email, user, pass, homeLang)) {
                createAccount(name, email, user, pass, homeLang)
            }
        }
    }

    private fun setupLanguageDropdown() {
        // Get language list from string resources
        val languages = resources.getStringArray(R.array.signup_languages).toList()

        val langAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        (binding.tilHomeLang.editText as AutoCompleteTextView).setAdapter(langAdapter)
    }

    private fun validateInput(
        name: String,
        email: String,
        user: String,
        pass: String,
        homeLang: String
    ): Boolean {
        when {
            name.isBlank() -> {
                binding.etFullName.error = getString(R.string.error_full_name_required)
                binding.etFullName.requestFocus()
                return false
            }

            email.isBlank() -> {
                binding.etEmail.error = getString(R.string.error_email_required)
                binding.etEmail.requestFocus()
                return false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = getString(R.string.error_valid_email)
                binding.etEmail.requestFocus()
                return false
            }

            user.isBlank() -> {
                binding.etUsername.error = getString(R.string.error_username_required)
                binding.etUsername.requestFocus()
                return false
            }

            pass.isBlank() -> {
                binding.etPassword.error = getString(R.string.error_password_required)
                binding.etPassword.requestFocus()
                return false
            }

            pass.length < 6 -> {
                binding.etPassword.error = getString(R.string.error_password_length)
                binding.etPassword.requestFocus()
                return false
            }

            homeLang.isBlank() -> {
                (binding.tilHomeLang.editText as AutoCompleteTextView).error =
                    getString(R.string.error_select_home_language)
                (binding.tilHomeLang.editText as AutoCompleteTextView).requestFocus()
                return false
            }

            else -> {
                // Clear any previous errors
                binding.etFullName.error = null
                binding.etEmail.error = null
                binding.etUsername.error = null
                binding.etPassword.error = null
                (binding.tilHomeLang.editText as AutoCompleteTextView).error = null
                return true
            }
        }
    }

    private fun createAccount(
        name: String,
        email: String,
        user: String,
        pass: String,
        homeLang: String
    ) {
        if (hasNavigated) return

        binding.btnSignUp.isEnabled = false
        binding.btnSignUp.text = getString(R.string.btn_creating_account)

        Log.d("MainActivity", "Creating account for email: $email")

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = getString(R.string.btn_sign_up)

                if (task.isSuccessful) {
                    Log.d("MainActivity", "✅ Account created successfully")
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        saveUserToFirestore(uid, name, email, user, homeLang)
                    } else {
                        Log.e("MainActivity", "❌ Error: Unable to get user ID")
                        Toast.makeText(
                            this,
                            getString(R.string.error_unable_get_user_id),
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToHomeActivity()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: getString(R.string.error_signup_failed)
                    Log.e("MainActivity", "❌ Account creation failed: $errorMessage", task.exception)

                    when {
                        errorMessage.contains("email address is already in use", ignoreCase = true) -> {
                            binding.etEmail.error = getString(R.string.error_email_already_registered)
                            Toast.makeText(
                                this,
                                getString(R.string.error_email_already_registered_long),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        errorMessage.contains("email address is badly formatted", ignoreCase = true) -> {
                            binding.etEmail.error = getString(R.string.error_invalid_email_format)
                            Toast.makeText(
                                this,
                                getString(R.string.error_valid_email),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        errorMessage.contains("weak password", ignoreCase = true) -> {
                            binding.etPassword.error = getString(R.string.error_weak_password)
                            Toast.makeText(
                                this,
                                getString(R.string.error_weak_password_long),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this,
                                getString(R.string.error_signup_failed_with_message, errorMessage),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = getString(R.string.btn_sign_up)
                Log.e("MainActivity", "❌ Sign up failure listener triggered", exception)
                Toast.makeText(
                    this,
                    getString(R.string.error_signup_failed_with_message, exception.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun saveUserToFirestore(
        uid: String,
        name: String,
        email: String,
        user: String,
        homeLang: String
    ) {
        val firestoreUserData = hashMapOf(
            "userId" to uid,
            "email" to email.trim(),
            "username" to user.trim(),
            "fullName" to name.trim(),
            "homeLanguage" to homeLang.trim(),
            "createdAt" to com.google.firebase.Timestamp.now(),
            "lastActive" to com.google.firebase.Timestamp.now()
        )

        Log.d("MainActivity", ">>> Saving user [$user] with email [$email] to Firestore")

        firestore.collection("users")
            .document(uid)
            .set(firestoreUserData)
            .addOnSuccessListener {
                Log.d("MainActivity", "✅ User saved to Firestore successfully")
                Toast.makeText(this, getString(R.string.msg_account_created_success), Toast.LENGTH_SHORT).show()

                clearForm()
                initializeNewUserGamification()

                navigateToHomeActivity()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "❌ Failed to save user to Firestore", e)
                Toast.makeText(this, getString(R.string.error_firestore_save_failed), Toast.LENGTH_LONG)
                    .show()

                clearForm()
                initializeNewUserGamification()

                navigateToHomeActivity()
            }
    }

    fun ensureUserExistsInFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userEmail = currentUser.email ?: "unknown@app.com"

            Log.d("MainActivity", "Checking if user exists in Firestore: $userEmail")

            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        Log.d("MainActivity", "User doesn't exist in Firestore, creating document")

                        val userData = hashMapOf(
                            "userId" to userId,
                            "email" to userEmail,
                            "username" to userEmail.substringBefore("@"),
                            "fullName" to (currentUser.displayName
                                ?: userEmail.substringBefore("@")),
                            "homeLanguage" to "English",
                            "createdAt" to com.google.firebase.Timestamp.now(),
                            "lastActive" to com.google.firebase.Timestamp.now(),
                            "migratedUser" to true
                        )

                        firestore.collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("MainActivity", "✅ Existing user migrated to Firestore: $userEmail")
                            }
                            .addOnFailureListener { e ->
                                Log.e("MainActivity", "❌ Failed to migrate user to Firestore", e)
                            }
                    } else {
                        Log.d("MainActivity", "User exists in Firestore, updating last active")
                        firestore.collection("users")
                            .document(userId)
                            .update("lastActive", com.google.firebase.Timestamp.now())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "❌ Error checking user in Firestore", e)
                }
        }
    }

    private fun initializeNewUserGamification() {
        if (hasNavigated) return

        try {
            Log.d("MainActivity", "Initializing gamification for new user")

            val timeoutJob = lifecycleScope.launch {
                delay(10000)
                if (!hasNavigated) {
                    Log.w("MainActivity", "⚠️ Gamification timeout, navigating anyway")
                    navigateToHomeActivity()
                }
            }

            gamificationManager.checkDailyLogin(object : GamificationManager.GamificationCallback {
                override fun onStreakDataLoaded(streakData: GamificationManager.StreakData) {
                    timeoutJob.cancel()
                    Log.d("MainActivity", "Streak data loaded for new user")
                    showNewUserWelcome(streakData)
                }

                override fun onStreakUpdated(streakData: GamificationManager.StreakData) {
                    timeoutJob.cancel()
                    Log.d("MainActivity", "Streak updated for new user")
                    showNewUserWelcome(streakData)
                }

                override fun onError(error: String) {
                    timeoutJob.cancel()
                    Log.e("MainActivity", "❌ Gamification error: $error")
                    navigateToHomeActivity()
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error initializing gamification", e)
            navigateToHomeActivity()
        }
    }

    private fun showNewUserWelcome(streakData: GamificationManager.StreakData) {
        if (hasNavigated) return

        try {
            Log.d("MainActivity", "Showing new user welcome dialog (skipped)")
            navigateToHomeActivity()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error showing new user welcome dialog", e)
            navigateToHomeActivity()
        }
    }

    private fun clearForm() {
        try {
            binding.etFullName.text?.clear()
            binding.etEmail.text?.clear()
            binding.etUsername.text?.clear()
            binding.etPassword.text?.clear()
            (binding.tilHomeLang.editText as AutoCompleteTextView).text.clear()

            binding.etFullName.error = null
            binding.etEmail.error = null
            binding.etUsername.error = null
            binding.etPassword.error = null
            (binding.tilHomeLang.editText as AutoCompleteTextView).error = null
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error clearing form", e)
        }
    }

    private fun navigateToHomeActivity() {
        if (hasNavigated) {
            Log.d("MainActivity", "Already navigated, skipping")
            return
        }

        hasNavigated = true

        try {
            Log.d("MainActivity", "Navigating to HomeActivity")

            val homeActivityClass = try {
                Class.forName("com.fake.mzansilingo.HomeActivity")
            } catch (e: ClassNotFoundException) {
                Log.e("MainActivity", "❌ HomeActivity not found!", e)
                Toast.makeText(this, getString(R.string.error_navigate_home), Toast.LENGTH_LONG)
                    .show()
                return
            }

            val intent = Intent(this, homeActivityClass)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("source", "registration")
            intent.putExtra("timestamp", System.currentTimeMillis())

            startActivity(intent)
            finish()

            Log.d("MainActivity", "✅ Navigation to HomeActivity successful")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error navigating to HomeActivity", e)
            Toast.makeText(
                this,
                getString(R.string.error_navigate_home_with_message, e.message ?: ""),
                Toast.LENGTH_LONG
            ).show()
            hasNavigated = false
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh language dropdown when language changes
        setupLanguageDropdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "MainActivity destroyed")
    }
}