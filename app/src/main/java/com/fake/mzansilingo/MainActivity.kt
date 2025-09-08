package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.fake.mzansilingo.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var gamificationManager: GamificationManager
    private var hasNavigated = false // Prevent multiple navigation attempts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Gamification Manager
        auth = FirebaseAuth.getInstance()
        gamificationManager = GamificationManager(this)

        // Setup language dropdown
        val languages = listOf(
            "isiZulu", "isiXhosa", "Afrikaans", "English",
            "Sepedi", "Setswana", "Sesotho", "Xitsonga",
            "Tshivenda", "isiNdebele", "Other"
        )
        val langAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        (binding.tilHomeLang.editText as AutoCompleteTextView).setAdapter(langAdapter)

        // Handle Sign Up button
        binding.btnSignUp.setOnClickListener {
            val name = binding.etFullName.text?.toString().orEmpty().trim()
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            val user = binding.etUsername.text?.toString().orEmpty().trim()
            val pass = binding.etPassword.text?.toString().orEmpty()
            val homeLang = (binding.tilHomeLang.editText as AutoCompleteTextView).text.toString().trim()

            if (validateInput(name, email, user, pass, homeLang)) {
                createAccount(name, email, user, pass, homeLang)
            }
        }
    }

    private fun validateInput(name: String, email: String, user: String, pass: String, homeLang: String): Boolean {
        when {
            name.isBlank() -> {
                binding.etFullName.error = "Full name is required"
                binding.etFullName.requestFocus()
                return false
            }
            email.isBlank() -> {
                binding.etEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Please enter a valid email address"
                binding.etEmail.requestFocus()
                return false
            }
            user.isBlank() -> {
                binding.etUsername.error = "Username is required"
                binding.etUsername.requestFocus()
                return false
            }
            pass.isBlank() -> {
                binding.etPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                return false
            }
            pass.length < 6 -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                binding.etPassword.requestFocus()
                return false
            }
            homeLang.isBlank() -> {
                (binding.tilHomeLang.editText as AutoCompleteTextView).error = "Please select your home language"
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

    private fun createAccount(name: String, email: String, user: String, pass: String, homeLang: String) {
        if (hasNavigated) return // Prevent multiple attempts

        binding.btnSignUp.isEnabled = false
        binding.btnSignUp.text = "Creating Account..."

        Log.d("MainActivity", "Creating account for email: $email")

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "Sign Up"

                if (task.isSuccessful) {
                    Log.d("MainActivity", "Account created successfully")
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        saveUserData(uid, name, email, user, homeLang)
                    } else {
                        Log.e("MainActivity", "Error: Unable to get user ID")
                        Toast.makeText(this, "Error: Unable to get user ID", Toast.LENGTH_SHORT).show()
                        navigateToHomeActivity()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Sign Up failed"
                    Log.e("MainActivity", "Account creation failed: $errorMessage", task.exception)

                    when {
                        errorMessage.contains("email address is already in use", ignoreCase = true) -> {
                            binding.etEmail.error = "This email is already registered"
                            Toast.makeText(this, "Email already registered. Try logging in instead.", Toast.LENGTH_LONG).show()
                        }
                        errorMessage.contains("email address is badly formatted", ignoreCase = true) -> {
                            binding.etEmail.error = "Invalid email format"
                            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show()
                        }
                        errorMessage.contains("weak password", ignoreCase = true) -> {
                            binding.etPassword.error = "Password is too weak"
                            Toast.makeText(this, "Please choose a stronger password", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "Sign Up failed: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "Sign Up"
                Log.e("MainActivity", "Sign up failure listener triggered", exception)
                Toast.makeText(this, "Sign up failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserData(uid: String, name: String, email: String, user: String, homeLang: String) {
        val db = FirebaseDatabase.getInstance().reference
        val userInfo = mapOf(
            "fullName" to name,
            "username" to user,
            "homeLanguage" to homeLang,
            "email" to email,
            "createdAt" to System.currentTimeMillis(),
            "isNewUser" to true
        )

        Log.d("MainActivity", "Saving user data for UID: $uid")

        db.child("users").child(uid).setValue(userInfo)
            .addOnSuccessListener {
                Log.d("MainActivity", "User data saved successfully")
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                // Clear form after successful registration
                clearForm()
                // Initialize gamification for new user
                initializeNewUserGamification()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to save user data", e)
                Toast.makeText(this, "Failed to save user data, but account created", Toast.LENGTH_SHORT).show()
                // Even if user data save fails, still navigate (user is created in auth)
                navigateToHomeActivity()
            }
    }

    private fun initializeNewUserGamification() {
        if (hasNavigated) return // Prevent if already navigating

        try {
            Log.d("MainActivity", "Initializing gamification for new user")

            // Set a timeout for gamification initialization
            val timeoutJob = lifecycleScope.launch {
                delay(10000) // 10 second timeout
                if (!hasNavigated) {
                    Log.w("MainActivity", "Gamification initialization timeout, navigating anyway")
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
                    Log.e("MainActivity", "Gamification initialization error: $error")
                    // Continue to home even if gamification fails
                    navigateToHomeActivity()
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing gamification", e)
            navigateToHomeActivity()
        }
    }

    private fun showNewUserWelcome(streakData: GamificationManager.StreakData) {
        if (hasNavigated) return // Prevent if already navigating

        try {
            Log.d("MainActivity", "Showing new user welcome dialog")

            // Skip dialog for now and go directly to home
            // TODO: Re-enable dialog once navigation is fixed
            Log.d("MainActivity", "Skipping welcome dialog, navigating directly")
            navigateToHomeActivity()

            /* COMMENTED OUT UNTIL NAVIGATION IS FIXED
            val dialog = WelcomeBackDialogFragment.newInstance(streakData) {
                // This callback is called when dialog is dismissed
                Log.d("MainActivity", "Welcome dialog dismissed, navigating to home")
                navigateToHomeActivity()
            }

            // Small delay to ensure smooth transition
            lifecycleScope.launch {
                delay(300)
                if (!isFinishing && !isDestroyed && !hasNavigated) {
                    try {
                        dialog.show(supportFragmentManager, "new_user_welcome_dialog")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error showing dialog", e)
                        navigateToHomeActivity()
                    }
                } else {
                    Log.d("MainActivity", "Activity finishing or already navigated, skipping dialog")
                    if (!hasNavigated) {
                        navigateToHomeActivity()
                    }
                }
            }
            */
        } catch (e: Exception) {
            Log.e("MainActivity", "Error showing new user welcome dialog", e)
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

            // Clear any errors
            binding.etFullName.error = null
            binding.etEmail.error = null
            binding.etUsername.error = null
            binding.etPassword.error = null
            (binding.tilHomeLang.editText as AutoCompleteTextView).error = null
        } catch (e: Exception) {
            Log.e("MainActivity", "Error clearing form", e)
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

            // Check if HomeActivity exists
            val homeActivityClass = try {
                Class.forName("com.fake.mzansilingo.HomeActivity")
            } catch (e: ClassNotFoundException) {
                Log.e("MainActivity", "HomeActivity class not found!", e)
                Toast.makeText(this, "Navigation error: Home screen not found", Toast.LENGTH_LONG).show()
                return
            }

            val intent = Intent(this, homeActivityClass)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Add extra data to help with debugging
            intent.putExtra("source", "registration")
            intent.putExtra("timestamp", System.currentTimeMillis())

            startActivity(intent)
            finish()

            Log.d("MainActivity", "Navigation to HomeActivity initiated successfully")

        } catch (e: Exception) {
            Log.e("MainActivity", "Error navigating to HomeActivity", e)
            Toast.makeText(this, "Error navigating to home screen: ${e.message}", Toast.LENGTH_LONG).show()
            hasNavigated = false // Reset flag on error
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "MainActivity destroyed")
    }
}