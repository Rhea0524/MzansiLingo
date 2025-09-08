package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.fake.mzansilingo.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gamificationManager: GamificationManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google sign in failed", e)
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        gamificationManager = GamificationManager(this)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupLanguageDropdowns()
        setupClickListeners()
    }

    private fun setupLanguageDropdowns() {
        val languages = listOf(
            "isiZulu", "isiXhosa", "Afrikaans", "English",
            "Sepedi", "Setswana", "Sesotho", "Xitsonga",
            "Tshivenda", "isiNdebele", "Other"
        )

        val homeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        binding.actvHomeLang.setAdapter(homeAdapter)

        val learnAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        binding.actvLearnLang.setAdapter(learnAdapter)
    }

    private fun setupClickListeners() {
        // Email/password login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()

            if (validateInput(email, password)) {
                loginWithEmailPassword(email, password)
            }
        }

        // Google Sign-In
        binding.btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        // Sign up link - This should probably go to a SignUp activity, not MainActivity
        binding.tvSignUpLink.setOnClickListener {
            // Consider creating a SignUpActivity instead
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        when {
            email.isBlank() -> {
                binding.tilEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email address"
                binding.etEmail.requestFocus()
                return false
            }
            password.isBlank() -> {
                binding.tilPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                return false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Password must be at least 6 characters"
                binding.etPassword.requestFocus()
                return false
            }
            else -> {
                // Clear any previous errors
                binding.tilEmail.error = null
                binding.tilPassword.error = null
                return true
            }
        }
    }

    private fun loginWithEmailPassword(email: String, password: String) {
        // Clear any previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Signing in..."

        Log.d("LoginActivity", "Attempting login with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Re-enable button regardless of result
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"

                if (task.isSuccessful) {
                    Log.d("LoginActivity", "Login successful")
                    val user = auth.currentUser
                    if (user != null) {
                        try {
                            saveLanguagePreferencesIfSelected(user.uid)
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error saving language preferences", e)
                            // Continue with login even if language preferences fail
                        }
                    }
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    navigateToHomeActivity()
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed"
                    Log.e("LoginActivity", "Login failed: $errorMessage", task.exception)

                    // Handle specific Firebase Auth errors
                    handleLoginError(task.exception)
                }
            }
            .addOnFailureListener { exception ->
                // Additional safety net for failures
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"
                Log.e("LoginActivity", "Login failure listener triggered", exception)
                handleLoginError(exception)
            }
    }

    private fun handleLoginError(exception: Exception?) {
        // Check error message for specific cases
        val errorMessage = exception?.message ?: "Login failed"

        when {
            errorMessage.contains("password is invalid", ignoreCase = true) ||
                    errorMessage.contains("wrong password", ignoreCase = true) -> {
                binding.tilPassword.error = "Incorrect password"
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("no user record", ignoreCase = true) ||
                    errorMessage.contains("user not found", ignoreCase = true) -> {
                binding.tilEmail.error = "No account found with this email"
                Toast.makeText(this, "No account found with this email", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("badly formatted", ignoreCase = true) ||
                    errorMessage.contains("invalid email", ignoreCase = true) -> {
                binding.tilEmail.error = "Invalid email format"
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("disabled", ignoreCase = true) -> {
                Toast.makeText(this, "This account has been disabled", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("too many requests", ignoreCase = true) -> {
                Toast.makeText(this, "Too many failed attempts. Please try again later.", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("network error", ignoreCase = true) ||
                    errorMessage.contains("network", ignoreCase = true) -> {
                Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle: ${account.id}")

        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("LoginActivity", "signInWithCredential:success")
                val user = auth.currentUser
                if (user != null) {
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                    if (isNewUser) {
                        try {
                            saveGoogleUserInfo(user.uid, account)
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error saving Google user info", e)
                            // Continue with login even if saving user info fails
                        }
                    } else {
                        try {
                            saveLanguagePreferencesIfSelected(user.uid)
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error saving language preferences", e)
                            // Continue with login even if language preferences fail
                        }
                    }
                }
                Toast.makeText(this, "Google login successful!", Toast.LENGTH_SHORT).show()
                navigateToHomeActivity()
            } else {
                Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                Toast.makeText(
                    this,
                    "Authentication failed: ${task.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveGoogleUserInfo(uid: String, account: GoogleSignInAccount) {
        val db = FirebaseDatabase.getInstance().reference
        val userInfo = mutableMapOf<String, Any>(
            "displayName" to (account.displayName ?: ""),
            "email" to (account.email ?: ""),
            "photoUrl" to (account.photoUrl?.toString() ?: ""),
            "provider" to "google",
            "createdAt" to System.currentTimeMillis()
        )

        val homeLang = binding.actvHomeLang.text.toString().trim()
        val learnLang = binding.actvLearnLang.text.toString().trim()
        if (homeLang.isNotBlank() && learnLang.isNotBlank()) {
            userInfo["homeLanguage"] = homeLang
            userInfo["learningLanguage"] = learnLang
        }

        db.child("users").child(uid).setValue(userInfo)
            .addOnSuccessListener {
                Log.d("LoginActivity", "User data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Failed to save user data", e)
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveLanguagePreferencesIfSelected(uid: String) {
        val homeLang = binding.actvHomeLang.text.toString().trim()
        val learnLang = binding.actvLearnLang.text.toString().trim()
        if (homeLang.isNotBlank() && learnLang.isNotBlank()) {
            val db = FirebaseDatabase.getInstance().reference
            val preferences = mapOf(
                "homeLanguage" to homeLang,
                "learningLanguage" to learnLang,
                "lastUpdated" to System.currentTimeMillis()
            )
            db.child("users").child(uid).child("preferences").setValue(preferences)
                .addOnSuccessListener {
                    Log.d("LoginActivity", "Language preferences saved")
                }
                .addOnFailureListener { e ->
                    Log.e("LoginActivity", "Failed to save preferences", e)
                }
        }
    }

    // Updated navigateToHomeActivity method with gamification for both new and returning users
    private fun navigateToHomeActivity() {
        try {
            // Check for gamification before navigating
            gamificationManager.checkDailyLogin(object : GamificationManager.GamificationCallback {
                override fun onStreakDataLoaded(streakData: GamificationManager.StreakData) {
                    handleStreakData(streakData)
                }

                override fun onStreakUpdated(streakData: GamificationManager.StreakData) {
                    handleStreakData(streakData)
                }

                override fun onError(error: String) {
                    Log.e("LoginActivity", "Gamification error: $error")
                    // Continue to home even if gamification fails
                    proceedToHome()
                }
            })
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in navigateToHomeActivity", e)
            proceedToHome()
        }
    }

    // Add these new methods to handle gamification
    private fun handleStreakData(streakData: GamificationManager.StreakData) {
        if (streakData.shouldShowWelcome && streakData.isNewDay) {
            // Show welcome back dialog
            showWelcomeBackDialog(streakData)
        } else {
            // Proceed directly to home
            proceedToHome()
        }
    }

    private fun showWelcomeBackDialog(streakData: GamificationManager.StreakData) {
        try {
            val dialog = WelcomeBackDialogFragment.newInstance(streakData) {
                // This callback is called when dialog is dismissed
                proceedToHome()
            }

            // Small delay to ensure smooth transition
            lifecycleScope.launch {
                delay(200)
                if (!isFinishing && !isDestroyed) {
                    dialog.show(supportFragmentManager, "welcome_back_dialog")
                }
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error showing welcome dialog", e)
            // Fallback to direct navigation
            proceedToHome()
        }
    }

    private fun proceedToHome() {
        try {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error navigating to HomeActivity", e)
            Toast.makeText(this, "Error navigating to home screen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d("LoginActivity", "User already logged in, navigating to home")
                navigateToHomeActivity()
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error checking current user", e)
        }
    }
}