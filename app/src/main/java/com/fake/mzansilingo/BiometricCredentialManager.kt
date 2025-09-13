package com.fake.mzansilingo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Manager class for securely storing and retrieving user credentials for biometric authentication
 */
class BiometricCredentialManager(private val context: Context) {

    private val sharedPrefsFile = "biometric_credentials"
    private val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Preference keys
    private val KEY_HAS_CREDENTIALS = "has_stored_credentials"
    private val KEY_CREDENTIAL_TYPE = "credential_type"
    private val KEY_EMAIL = "stored_email"
    private val KEY_PASSWORD = "stored_password"
    private val KEY_AUTO_PROMPT = "auto_prompt_enabled"
    private val KEY_SETUP_DATE = "setup_date"

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                sharedPrefsFile,
                keyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error creating encrypted preferences", e)
            // Fallback to regular SharedPreferences (less secure but functional)
            context.getSharedPreferences("${sharedPrefsFile}_fallback", Context.MODE_PRIVATE)
        }
    }

    /**
     * Data class to represent stored credentials
     */
    data class StoredCredentials(
        val type: CredentialType,
        val email: String? = null,
        val password: String? = null
    )

    /**
     * Enum for credential types
     */
    enum class CredentialType(val value: String) {
        EMAIL_PASSWORD("email_password"),
        GOOGLE("google");

        companion object {
            fun fromString(value: String): CredentialType {
                return values().find { it.value == value } ?: EMAIL_PASSWORD
            }
        }
    }

    /**
     * Store user credentials securely
     */
    fun storeCredentials(credentials: StoredCredentials) {
        try {
            encryptedPrefs.edit().apply {
                putBoolean(KEY_HAS_CREDENTIALS, true)
                putString(KEY_CREDENTIAL_TYPE, credentials.type.value)
                putString(KEY_EMAIL, credentials.email ?: "")
                putString(KEY_PASSWORD, credentials.password ?: "")
                putBoolean(KEY_AUTO_PROMPT, true) // Enable auto-prompt by default
                putLong(KEY_SETUP_DATE, System.currentTimeMillis())
                apply()
            }
            Log.d("BiometricCredentialManager", "Credentials stored successfully")
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error storing credentials", e)
            throw e
        }
    }

    /**
     * Retrieve stored credentials
     */
    fun getStoredCredentials(): StoredCredentials? {
        return try {
            if (!hasStoredCredentials()) {
                Log.d("BiometricCredentialManager", "No stored credentials found")
                return null
            }

            val typeString = encryptedPrefs.getString(KEY_CREDENTIAL_TYPE, CredentialType.EMAIL_PASSWORD.value) ?: CredentialType.EMAIL_PASSWORD.value
            val type = CredentialType.fromString(typeString)
            val email = encryptedPrefs.getString(KEY_EMAIL, "")
            val password = encryptedPrefs.getString(KEY_PASSWORD, "")

            StoredCredentials(
                type = type,
                email = email?.takeIf { it.isNotBlank() },
                password = password?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error retrieving credentials", e)
            null
        }
    }

    /**
     * Check if credentials are stored
     */
    fun hasStoredCredentials(): Boolean {
        return try {
            encryptedPrefs.getBoolean(KEY_HAS_CREDENTIALS, false)
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error checking for stored credentials", e)
            false
        }
    }

    /**
     * Check if auto-prompt is enabled
     */
    fun shouldAutoPrompt(): Boolean {
        return try {
            encryptedPrefs.getBoolean(KEY_AUTO_PROMPT, false)
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error checking auto-prompt setting", e)
            false
        }
    }

    /**
     * Enable or disable auto-prompt
     */
    fun setAutoPrompt(enabled: Boolean) {
        try {
            encryptedPrefs.edit().apply {
                putBoolean(KEY_AUTO_PROMPT, enabled)
                apply()
            }
            Log.d("BiometricCredentialManager", "Auto-prompt set to: $enabled")
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error setting auto-prompt", e)
        }
    }

    /**
     * Get when biometric login was set up
     */
    fun getSetupDate(): Long {
        return try {
            encryptedPrefs.getLong(KEY_SETUP_DATE, 0L)
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error getting setup date", e)
            0L
        }
    }

    /**
     * Clear all stored credentials
     */
    fun clearCredentials() {
        try {
            encryptedPrefs.edit().clear().apply()
            Log.d("BiometricCredentialManager", "All credentials cleared")
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error clearing credentials", e)
        }
    }

    /**
     * Update stored email (useful for profile updates)
     */
    fun updateEmail(newEmail: String) {
        try {
            if (hasStoredCredentials()) {
                encryptedPrefs.edit().apply {
                    putString(KEY_EMAIL, newEmail)
                    apply()
                }
                Log.d("BiometricCredentialManager", "Email updated successfully")
            }
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error updating email", e)
        }
    }

    /**
     * Update stored password (useful for password changes)
     */
    fun updatePassword(newPassword: String) {
        try {
            if (hasStoredCredentials()) {
                val credentials = getStoredCredentials()
                if (credentials?.type == CredentialType.EMAIL_PASSWORD) {
                    encryptedPrefs.edit().apply {
                        putString(KEY_PASSWORD, newPassword)
                        apply()
                    }
                    Log.d("BiometricCredentialManager", "Password updated successfully")
                }
            }
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error updating password", e)
        }
    }

    /**
     * Get credential type without retrieving full credentials
     */
    fun getCredentialType(): CredentialType? {
        return try {
            if (hasStoredCredentials()) {
                val typeString = encryptedPrefs.getString(KEY_CREDENTIAL_TYPE, null)
                typeString?.let { CredentialType.fromString(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error getting credential type", e)
            null
        }
    }

    /**
     * Validate that stored credentials are still valid format
     */
    fun validateStoredCredentials(): Boolean {
        return try {
            val credentials = getStoredCredentials()
            when (credentials?.type) {
                CredentialType.EMAIL_PASSWORD -> {
                    !credentials.email.isNullOrBlank() && !credentials.password.isNullOrBlank()
                }
                CredentialType.GOOGLE -> {
                    !credentials.email.isNullOrBlank()
                }
                null -> false
            }
        } catch (e: Exception) {
            Log.e("BiometricCredentialManager", "Error validating credentials", e)
            false
        }
    }

    /**
     * Get debug info (without sensitive data)
     */
    fun getDebugInfo(): String {
        return try {
            val hasCredentials = hasStoredCredentials()
            val type = getCredentialType()
            val autoPrompt = shouldAutoPrompt()
            val setupDate = getSetupDate()

            "Has credentials: $hasCredentials, Type: $type, Auto-prompt: $autoPrompt, Setup: $setupDate"
        } catch (e: Exception) {
            "Error getting debug info: ${e.message}"
        }
    }
}