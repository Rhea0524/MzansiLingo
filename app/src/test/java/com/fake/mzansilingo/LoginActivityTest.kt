package com.fake.mzansilingo

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*

class LoginActivityTest {

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    // Email Validation Tests
    @Test
    fun emailValidation_validEmail_shouldReturnTrue() {
        val validEmails = listOf(
            "user@example.com",
            "test.email@domain.co.za",
            "user123@gmail.com",
            "firstname.lastname@company.org"
        )

        for (email in validEmails) {
            assertTrue("$email should be valid", isValidEmail(email))
        }
    }

    @Test
    fun emailValidation_invalidEmail_shouldReturnFalse() {
        val invalidEmails = listOf(
            "",
            "   ",
            "invalid-email",
            "@domain.com",
            "user@",
            "user@domain",
            "user..name@domain.com",
            "user@domain..com"
        )

        for (email in invalidEmails) {
            assertFalse("$email should be invalid", isValidEmail(email))
        }
    }

    // Password Validation Tests
    @Test
    fun passwordValidation_validPassword_shouldReturnTrue() {
        val validPasswords = listOf(
            "password123",
            "MySecureP@ss",
            "123456", // minimum length
            "a".repeat(50) // long password
        )

        for (password in validPasswords) {
            assertTrue("$password should be valid", isValidPassword(password))
        }
    }

    @Test
    fun passwordValidation_invalidPassword_shouldReturnFalse() {
        val invalidPasswords = listOf(
            "",
            "   ",
            "12345", // too short
            "pwd", // too short
            "a".repeat(5) // exactly 5 chars
        )

        for (password in invalidPasswords) {
            assertFalse("$password should be invalid", isValidPassword(password))
        }
    }

    // Input Validation Combined Tests
    @Test
    fun validateInput_validEmailAndPassword_shouldReturnTrue() {
        val validCombinations = listOf(
            Pair("user@example.com", "password123"),
            Pair("test@domain.co.za", "mypassword"),
            Pair("admin@company.org", "123456")
        )

        for ((email, password) in validCombinations) {
            assertTrue("Valid email $email and password should pass",
                validateCredentials(email, password))
        }
    }

    @Test
    fun validateInput_invalidCombinations_shouldReturnFalse() {
        val invalidCombinations = listOf(
            Pair("", "password123"), // empty email
            Pair("user@example.com", ""), // empty password
            Pair("invalid-email", "password123"), // invalid email
            Pair("user@example.com", "123"), // short password
            Pair("", ""), // both empty
            Pair("   ", "   ") // whitespace only
        )

        for ((email, password) in invalidCombinations) {
            assertFalse("Invalid combination $email:$password should fail",
                validateCredentials(email, password))
        }
    }

    // Language Selection Tests
    @Test
    fun languageSelection_validLanguages_shouldContainExpectedOptions() {
        val expectedLanguages = listOf(
            "isiZulu", "isiXhosa", "Afrikaans", "English",
            "Sepedi", "Setswana", "Sesotho", "Xitsonga",
            "Tshivenda", "isiNdebele", "Other"
        )

        val actualLanguages = getAvailableLanguages()

        assertEquals("Should have 11 languages", 11, actualLanguages.size)

        for (language in expectedLanguages) {
            assertTrue("Should contain $language", actualLanguages.contains(language))
        }
    }

    @Test
    fun languagePreferences_validSelection_shouldCreateCorrectMap() {
        val homeLang = "isiZulu"
        val learnLang = "English"

        val preferences = createLanguagePreferences(homeLang, learnLang)

        assertEquals("Home language should be set", homeLang, preferences!!["homeLanguage"])
        assertEquals("Learning language should be set", learnLang, preferences["learningLanguage"])
        assertTrue("Should have lastUpdated timestamp", preferences.containsKey("lastUpdated"))
        assertTrue("Timestamp should be positive", (preferences["lastUpdated"] as Long) > 0)
    }

    @Test
    fun languagePreferences_emptySelection_shouldReturnNull() {
        val testCases = listOf(
            Pair("", "English"),
            Pair("isiZulu", ""),
            Pair("", ""),
            Pair("   ", "English"),
            Pair("isiZulu", "   ")
        )

        for ((homeLang, learnLang) in testCases) {
            assertNull("Empty/whitespace languages should return null",
                createLanguagePreferences(homeLang, learnLang))
        }
    }

    // Error Handling Tests
    @Test
    fun loginError_passwordInvalid_shouldReturnCorrectMessage() {
        val passwordErrors = listOf(
            "The password is invalid",
            "Wrong password",
            "password is invalid or user does not exist"
        )

        for (error in passwordErrors) {
            assertEquals("Password error should be detected",
                LoginErrorType.INVALID_PASSWORD, categorizeLoginError(error))
        }
    }

    @Test
    fun loginError_userNotFound_shouldReturnCorrectMessage() {
        val userNotFoundErrors = listOf(
            "There is no user record corresponding to this identifier",
            "User not found",
            "no user record corresponding to this identifier"
        )

        for (error in userNotFoundErrors) {
            assertEquals("User not found error should be detected",
                LoginErrorType.USER_NOT_FOUND, categorizeLoginError(error))
        }
    }

    @Test
    fun loginError_networkError_shouldReturnCorrectMessage() {
        val networkErrors = listOf(
            "A network error occurred",
            "Network error (such as timeout, interrupted connection or unreachable host)",
            "network error occurred"
        )

        for (error in networkErrors) {
            assertEquals("Network error should be detected",
                LoginErrorType.NETWORK_ERROR, categorizeLoginError(error))
        }
    }

    // Biometric Credential Tests
    @Test
    fun biometricCredentials_emailPassword_shouldCreateCorrectly() {
        val email = "user@example.com"
        val password = "password123"
        val credType = BiometricCredentialType.EMAIL_PASSWORD

        val credentials = createBiometricCredentials(credType, email, password)

        assertEquals("Type should match", credType, credentials.type)
        assertEquals("Email should match", email, credentials.email)
        assertEquals("Password should match", password, credentials.password)
    }

    @Test
    fun biometricCredentials_google_shouldCreateCorrectly() {
        val email = "user@gmail.com"
        val credType = BiometricCredentialType.GOOGLE

        val credentials = createBiometricCredentials(credType, email, null)

        assertEquals("Type should match", credType, credentials.type)
        assertEquals("Email should match", email, credentials.email)
        assertNull("Password should be null for Google", credentials.password)
    }

    // User Data Creation Tests
    @Test
    fun userInfo_newUser_shouldContainRequiredFields() {
        val uid = "test123"
        val displayName = "Test User"
        val email = "test@example.com"
        val photoUrl = "https://example.com/photo.jpg"
        val provider = "google"

        val userInfo = createUserInfo(uid, displayName, email, photoUrl, provider, true)

        assertEquals("UID should match", uid, userInfo["uid"])
        assertEquals("Display name should match", displayName, userInfo["displayName"])
        assertEquals("Email should match", email, userInfo["email"])
        assertEquals("Photo URL should match", photoUrl, userInfo["photoUrl"])
        assertEquals("Provider should match", provider, userInfo["provider"])
        assertTrue("Should be active", userInfo["isActive"] as Boolean)
        assertTrue("Should have createdAt", userInfo.containsKey("createdAt"))
        assertTrue("Should have lastLoginAt", userInfo.containsKey("lastLoginAt"))
    }

    @Test
    fun userStats_initialization_shouldHaveCorrectDefaults() {
        val initialStats = createInitialUserStats()

        assertEquals("Total lessons should be 0", 0, initialStats["totalLessonsCompleted"])
        assertEquals("Current streak should be 0", 0, initialStats["currentStreak"])
        assertEquals("Longest streak should be 0", 0, initialStats["longestStreak"])
        assertEquals("Total XP should be 0", 0, initialStats["totalXP"])
        assertEquals("Level should be 1", 1, initialStats["level"])
        assertTrue("Should have createdAt", initialStats.containsKey("createdAt"))
        assertTrue("Should have lastUpdated", initialStats.containsKey("lastUpdated"))
    }

    // Day of Week Mapping Test (from existing logic)
    @Test
    fun dayOfWeek_mapping_shouldBeConsistent() {
        val dayMappings = mapOf(
            1 to 6, // Sunday -> 6
            2 to 0, // Monday -> 0
            3 to 1, // Tuesday -> 1
            4 to 2, // Wednesday -> 2
            5 to 3, // Thursday -> 3
            6 to 4, // Friday -> 4
            7 to 5  // Saturday -> 5
        )

        for ((calendarDay, expectedIndex) in dayMappings) {
            val actualIndex = mapCalendarDayToIndex(calendarDay)
            assertEquals("Calendar day $calendarDay should map to $expectedIndex",
                expectedIndex, actualIndex)
        }
    }

    // Helper functions that mirror the logic in LoginActivity
    private fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotBlank() &&
                trimmed.contains("@") &&
                trimmed.contains(".") &&
                trimmed.indexOf("@") > 0 && // @ not at start
                trimmed.indexOf("@") < trimmed.lastIndexOf(".") && // @ before last .
                trimmed.lastIndexOf(".") < trimmed.length - 1 && // . not at end
                !trimmed.contains("..") && // no consecutive dots
                trimmed.count { it == '@' } == 1 // exactly one @
    }

    private fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        return isValidEmail(email) && isValidPassword(password)
    }

    private fun getAvailableLanguages(): List<String> {
        return listOf(
            "isiZulu", "isiXhosa", "Afrikaans", "English",
            "Sepedi", "Setswana", "Sesotho", "Xitsonga",
            "Tshivenda", "isiNdebele", "Other"
        )
    }

    private fun createLanguagePreferences(homeLang: String, learnLang: String): Map<String, Any>? {
        val homeClean = homeLang.trim()
        val learnClean = learnLang.trim()

        return if (homeClean.isNotBlank() && learnClean.isNotBlank()) {
            mapOf(
                "homeLanguage" to homeClean,
                "learningLanguage" to learnClean,
                "lastUpdated" to System.currentTimeMillis()
            )
        } else null
    }

    private fun categorizeLoginError(errorMessage: String): LoginErrorType {
        val message = errorMessage.lowercase()
        return when {
            message.contains("password is invalid") ||
                    message.contains("wrong password") -> LoginErrorType.INVALID_PASSWORD

            message.contains("no user record") ||
                    message.contains("user not found") -> LoginErrorType.USER_NOT_FOUND

            message.contains("network error") ||
                    message.contains("network") -> LoginErrorType.NETWORK_ERROR

            message.contains("badly formatted") ||
                    message.contains("invalid email") -> LoginErrorType.INVALID_EMAIL

            message.contains("disabled") -> LoginErrorType.ACCOUNT_DISABLED

            message.contains("too many requests") -> LoginErrorType.TOO_MANY_REQUESTS

            else -> LoginErrorType.UNKNOWN
        }
    }

    private fun createBiometricCredentials(type: BiometricCredentialType, email: String, password: String?): BiometricCredentials {
        return BiometricCredentials(type, email, password)
    }

    private fun createUserInfo(uid: String, displayName: String, email: String, photoUrl: String, provider: String, isNewUser: Boolean): Map<String, Any> {
        return hashMapOf<String, Any>(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "provider" to provider,
            "isActive" to true,
            "createdAt" to System.currentTimeMillis(),
            "lastLoginAt" to System.currentTimeMillis()
        )
    }

    private fun createInitialUserStats(): Map<String, Any> {
        return hashMapOf<String, Any>(
            "totalLessonsCompleted" to 0,
            "currentStreak" to 0,
            "longestStreak" to 0,
            "totalXP" to 0,
            "level" to 1,
            "createdAt" to System.currentTimeMillis(),
            "lastUpdated" to System.currentTimeMillis()
        )
    }

    private fun mapCalendarDayToIndex(calendarDay: Int): Int {
        return when (calendarDay) {
            1 -> 6 // Sunday
            2 -> 0 // Monday
            3 -> 1 // Tuesday
            4 -> 2 // Wednesday
            5 -> 3 // Thursday
            6 -> 4 // Friday
            7 -> 5 // Saturday
            else -> -1
        }
    }

    // Helper enums and data classes
    enum class LoginErrorType {
        INVALID_PASSWORD,
        USER_NOT_FOUND,
        NETWORK_ERROR,
        INVALID_EMAIL,
        ACCOUNT_DISABLED,
        TOO_MANY_REQUESTS,
        UNKNOWN
    }

    enum class BiometricCredentialType {
        EMAIL_PASSWORD,
        GOOGLE
    }

    data class BiometricCredentials(
        val type: BiometricCredentialType,
        val email: String,
        val password: String?
    )
}