package com.fake.mzansilingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gamificationManager: GamificationManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor
    private lateinit var biometricManager: BiometricCredentialManager

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
        firestore = FirebaseFirestore.getInstance()
        gamificationManager = GamificationManager(this)
        biometricManager = BiometricCredentialManager(this)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupBiometric()
        setupLanguageDropdowns()
        setupClickListeners()

        // Only call checkBiometricAvailability once
        checkBiometricAvailability()
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e("LoginActivity", "Biometric authentication error: $errString")
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(this@LoginActivity,
                            "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("LoginActivity", "Biometric authentication succeeded")
                    handleBiometricSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d("LoginActivity", "Biometric authentication failed")
                    Toast.makeText(this@LoginActivity,
                        "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint or face to log in")
            .setNegativeButtonText("Use Password")
            .build()
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)

        Log.d("LoginActivity", "=== BIOMETRIC DEBUG START ===")
        Log.d("LoginActivity", "Biometric check result: $result")
        Log.d("LoginActivity", "Has stored credentials: ${this.biometricManager.hasStoredCredentials()}")
        Log.d("LoginActivity", "Should auto prompt: ${this.biometricManager.shouldAutoPrompt()}")
        Log.d("LoginActivity", "Credential debug info: ${this.biometricManager.getDebugInfo()}")

        when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("LoginActivity", "Biometric SUCCESS - showing button")
                binding.btnBiometricLogin.visibility = android.view.View.VISIBLE
                binding.dividerLayout.visibility = android.view.View.VISIBLE

                // Check stored credentials AFTER making button visible
                checkForStoredCredentials()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("LoginActivity", "No biometric hardware available")
                binding.btnBiometricLogin.visibility = android.view.View.GONE
                binding.dividerLayout.visibility = android.view.View.GONE
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("LoginActivity", "Biometric hardware unavailable")
                binding.btnBiometricLogin.visibility = android.view.View.GONE
                binding.dividerLayout.visibility = android.view.View.GONE
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d("LoginActivity", "No biometric enrolled")
                binding.btnBiometricLogin.visibility = android.view.View.GONE
                binding.dividerLayout.visibility = android.view.View.GONE
            }
            else -> {
                Log.d("LoginActivity", "Other biometric error: $result")
                binding.btnBiometricLogin.visibility = android.view.View.GONE
                binding.dividerLayout.visibility = android.view.View.GONE
            }
        }

        Log.d("LoginActivity", "Final button visibility: ${binding.btnBiometricLogin.visibility}")
        Log.d("LoginActivity", "Final divider visibility: ${binding.dividerLayout.visibility}")
        Log.d("LoginActivity", "=== BIOMETRIC DEBUG END ===")
    }

    private fun checkForStoredCredentials() {
        Log.d("LoginActivity", "Checking for stored credentials...")

        if (biometricManager.hasStoredCredentials()) {
            Log.d("LoginActivity", "Found stored credentials")
            // Keep button visible since we have credentials
            binding.btnBiometricLogin.visibility = android.view.View.VISIBLE
            binding.dividerLayout.visibility = android.view.View.VISIBLE

            if (biometricManager.shouldAutoPrompt()) {
                Log.d("LoginActivity", "Auto-prompting biometric authentication")
                lifecycleScope.launch {
                    delay(500)
                    showBiometricPrompt()
                }
            }
        } else {
            Log.d("LoginActivity", "No stored credentials - but keeping button visible for setup")
            // KEEP the button visible even without stored credentials
            // Users need to be able to set up biometric login after successful login
            binding.btnBiometricLogin.visibility = android.view.View.VISIBLE
            binding.dividerLayout.visibility = android.view.View.VISIBLE

            // Update button text to indicate setup is needed
            binding.btnBiometricLogin.text = "Setup Biometric Login"
        }
    }

    private fun showBiometricPrompt() {
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error showing biometric prompt", e)
            Toast.makeText(this, "Error showing biometric authentication", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBiometricSuccess() {
        try {
            val credentials = biometricManager.getStoredCredentials()
            if (credentials != null) {
                when (credentials.type) {
                    BiometricCredentialManager.CredentialType.EMAIL_PASSWORD -> {
                        if (!credentials.email.isNullOrBlank() && !credentials.password.isNullOrBlank()) {
                            loginWithEmailPassword(credentials.email, credentials.password, false)
                        } else {
                            Toast.makeText(this, "Invalid stored credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                    BiometricCredentialManager.CredentialType.GOOGLE -> {
                        // For Google sign-in, we'll try silent sign-in
                        attemptSilentGoogleSignIn()
                    }
                }
            } else {
                Toast.makeText(this, "Please login first to setup biometric authentication", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error handling biometric success", e)
            Toast.makeText(this, "Error retrieving stored credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptSilentGoogleSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            firebaseAuthWithGoogle(account)
        } else {
            // If no cached account, fall back to regular Google sign-in
            signInWithGoogle()
        }
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
                loginWithEmailPassword(email, password, true)
            }
        }

        // Google Sign-In
        binding.btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        // Biometric Login
        binding.btnBiometricLogin.setOnClickListener {
            if (biometricManager.hasStoredCredentials()) {
                showBiometricPrompt()
            } else {
                Toast.makeText(this, "Please login with email/password or Google first to setup biometric authentication", Toast.LENGTH_LONG).show()
            }
        }

        // Sign up link
        binding.tvSignUpLink.setOnClickListener {
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
                binding.tilEmail.error = null
                binding.tilPassword.error = null
                return true
            }
        }
    }

    // Updated method with biometric credential saving option
    private fun loginWithEmailPassword(email: String, password: String, shouldOfferBiometric: Boolean) {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Signing in..."

        Log.d("LoginActivity", "Attempting login with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
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
                        }
                    }

                    // Offer biometric storage for successful email/password login
                    if (shouldOfferBiometric && canUseBiometric()) {
                        offerBiometricStorage(email, password, BiometricCredentialManager.CredentialType.EMAIL_PASSWORD)
                    } else {
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                        navigateToHomeActivity()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed"
                    Log.e("LoginActivity", "Login failed: $errorMessage", task.exception)
                    handleLoginError(task.exception)
                }
            }
            .addOnFailureListener { exception ->
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"
                Log.e("LoginActivity", "Login failure listener triggered", exception)
                handleLoginError(exception)
            }
    }

    private fun canUseBiometric(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun offerBiometricStorage(email: String, password: String, type: BiometricCredentialManager.CredentialType) {
        if (biometricManager.hasStoredCredentials()) {
            // User already has biometric login set up
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
            navigateToHomeActivity()
            return
        }

        // Show dialog to ask user if they want to enable biometric login
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Enable Biometric Login")
            .setMessage("Would you like to use your fingerprint or face to log in next time?")
            .setPositiveButton("Enable") { _, _ ->
                try {
                    val credentials = when (type) {
                        BiometricCredentialManager.CredentialType.EMAIL_PASSWORD ->
                            BiometricCredentialManager.StoredCredentials(type, email, password)
                        BiometricCredentialManager.CredentialType.GOOGLE ->
                            BiometricCredentialManager.StoredCredentials(type, email, null)
                    }
                    biometricManager.storeCredentials(credentials)

                    // Update button text after successful storage
                    binding.btnBiometricLogin.text = "Login with Biometric"
                    Toast.makeText(this, "Biometric login enabled!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error storing biometric credentials", e)
                    Toast.makeText(this, "Failed to enable biometric login", Toast.LENGTH_SHORT).show()
                }
                navigateToHomeActivity()
            }
            .setNegativeButton("Not Now") { _, _ ->
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                navigateToHomeActivity()
            }
            .show()
    }

    private fun handleLoginError(exception: Exception?) {
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
                            saveGoogleUserToFirestore(user.uid, account)
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error saving Google user info", e)
                        }
                        // Offer biometric for new Google users
                        if (canUseBiometric()) {
                            offerBiometricStorage(account.email ?: "", "", BiometricCredentialManager.CredentialType.GOOGLE)
                        } else {
                            Toast.makeText(this, "Google login successful!", Toast.LENGTH_SHORT).show()
                            navigateToHomeActivity()
                        }
                    } else {
                        try {
                            updateExistingUserData(user.uid, account)
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error updating user data", e)
                        }
                        Toast.makeText(this, "Google login successful!", Toast.LENGTH_SHORT).show()
                        navigateToHomeActivity()
                    }
                }
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

    private fun saveGoogleUserToFirestore(uid: String, account: GoogleSignInAccount) {
        val userInfo = hashMapOf<String, Any>(
            "uid" to uid,
            "displayName" to (account.displayName ?: ""),
            "email" to (account.email ?: ""),
            "photoUrl" to (account.photoUrl?.toString() ?: ""),
            "provider" to "google",
            "createdAt" to Timestamp.now(),
            "lastLoginAt" to Timestamp.now(),
            "isActive" to true
        )

        val homeLang = binding.actvHomeLang.text.toString().trim()
        val learnLang = binding.actvLearnLang.text.toString().trim()
        if (homeLang.isNotBlank() && learnLang.isNotBlank()) {
            userInfo["homeLanguage"] = homeLang
            userInfo["learningLanguage"] = learnLang
            userInfo["languagePreferencesSet"] = true
        } else {
            userInfo["languagePreferencesSet"] = false
        }

        firestore.collection("users")
            .document(uid)
            .set(userInfo)
            .addOnSuccessListener {
                Log.d("LoginActivity", "Google user data saved to Firestore successfully")
                initializeUserStats(uid)
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Failed to save Google user data to Firestore", e)
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateExistingUserData(uid: String, account: GoogleSignInAccount) {
        val updates = hashMapOf<String, Any>(
            "lastLoginAt" to Timestamp.now(),
            "displayName" to (account.displayName ?: ""),
            "photoUrl" to (account.photoUrl?.toString() ?: "")
        )

        val homeLang = binding.actvHomeLang.text.toString().trim()
        val learnLang = binding.actvLearnLang.text.toString().trim()
        if (homeLang.isNotBlank() && learnLang.isNotBlank()) {
            updates["homeLanguage"] = homeLang
            updates["learningLanguage"] = learnLang
            updates["languagePreferencesSet"] = true
            updates["languagePreferencesUpdatedAt"] = Timestamp.now()
        }

        firestore.collection("users")
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                Log.d("LoginActivity", "User data updated in Firestore successfully")
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Failed to update user data in Firestore", e)
            }
    }

    private fun initializeUserStats(uid: String) {
        val initialStats = hashMapOf<String, Any>(
            "totalLessonsCompleted" to 0,
            "currentStreak" to 0,
            "longestStreak" to 0,
            "totalXP" to 0,
            "level" to 1,
            "createdAt" to Timestamp.now(),
            "lastUpdated" to Timestamp.now()
        )

        firestore.collection("userStats")
            .document(uid)
            .set(initialStats)
            .addOnSuccessListener {
                Log.d("LoginActivity", "User stats initialized successfully")
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Failed to initialize user stats", e)
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

    private fun navigateToHomeActivity() {
        try {
            gamificationManager.checkDailyLogin(object : GamificationManager.GamificationCallback {
                override fun onStreakDataLoaded(streakData: GamificationManager.StreakData) {
                    handleStreakData(streakData)
                }

                override fun onStreakUpdated(streakData: GamificationManager.StreakData) {
                    handleStreakData(streakData)
                }

                override fun onError(error: String) {
                    Log.e("LoginActivity", "Gamification error: $error")
                    proceedToHome()
                }
            })
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in navigateToHomeActivity", e)
            proceedToHome()
        }
    }

    private fun handleStreakData(streakData: GamificationManager.StreakData) {
        if (streakData.shouldShowWelcome && streakData.isNewDay) {
            showWelcomeBackDialog(streakData)
        } else {
            proceedToHome()
        }
    }

    private fun showWelcomeBackDialog(streakData: GamificationManager.StreakData) {
        try {
            val dialog = WelcomeBackDialogFragment.newInstance(streakData) {
                proceedToHome()
            }

            lifecycleScope.launch {
                delay(200)
                if (!isFinishing && !isDestroyed) {
                    dialog.show(supportFragmentManager, "welcome_back_dialog")
                }
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error showing welcome dialog", e)
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