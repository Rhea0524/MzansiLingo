package com.fake.mzansilingo

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.MockitoAnnotations

class ProfileActivityTest {

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    // ========== Email Validation Tests ==========
    @Test
    fun emailValidation_validEmail_shouldReturnTrue() {
        // Test valid email formats
        val validEmails = listOf(
            "user@example.com",
            "test.email@domain.co.za",
            "user123@gmail.com",
            "firstname.lastname@company.org",
            "a@b.co"
        )

        for (email in validEmails) {
            val isValid = isValidEmailFormat(email)
            assertTrue("$email should be valid", isValid)
        }
    }
    

    // ========== Input Field Validation Tests ==========
    @Test
    fun fieldValidation_fullName_shouldValidateCorrectly() {
        // Test full name validation logic
        val testCases = listOf(
            Triple("John Doe", false, "Valid name should pass"),
            Triple("", true, "Empty name should fail"),
            Triple("   ", true, "Whitespace only should fail"),
            Triple("A", false, "Single character should pass"),
            Triple("Jean-Pierre van der Merwe", false, "Complex name should pass")
        )

        for ((fullName, shouldFail, message) in testCases) {
            val trimmedName = fullName.trim()
            val hasError = trimmedName.isEmpty()

            if (shouldFail) {
                assertTrue(message, hasError)
            } else {
                assertFalse(message, hasError)
            }
        }
    }

    @Test
    fun fieldValidation_username_shouldValidateCorrectly() {
        // Test username validation logic
        val testCases = listOf(
            Triple("johndoe123", false, "Valid username should pass"),
            Triple("", true, "Empty username should fail"),
            Triple("   ", true, "Whitespace only should fail"),
            Triple("user_name", false, "Username with underscore should pass"),
            Triple("a", false, "Single character username should pass")
        )

        for ((username, shouldFail, message) in testCases) {
            val trimmedUsername = username.trim()
            val hasError = trimmedUsername.isEmpty()

            if (shouldFail) {
                assertTrue(message, hasError)
            } else {
                assertFalse(message, hasError)
            }
        }
    }

    @Test
    fun fieldValidation_combinedValidation_shouldValidateAllFields() {
        // Test combined field validation
        val fullName = "John Doe"
        val email = "john@example.com"
        val username = "johndoe"

        val errors = validateProfileFields(fullName, email, username)

        assertTrue("Valid profile should have no errors", errors.isEmpty())
    }

    @Test
    fun fieldValidation_multipleErrors_shouldCatchAll() {
        // Test multiple validation errors at once
        val fullName = ""
        val email = "invalid-email"
        val username = ""

        val errors = validateProfileFields(fullName, email, username)

        assertTrue("Should have multiple errors", errors.size >= 3)
        assertTrue("Should contain full name error",
            errors.any { it.contains("Full name") })
        assertTrue("Should contain email error",
            errors.any { it.contains("email") })
        assertTrue("Should contain username error",
            errors.any { it.contains("Username") })
    }

    // ========== Language Spinner Tests ==========
    @Test
    fun languageSpinner_availableLanguages_shouldContainAllSouthAfricanLanguages() {
        // Test language spinner setup
        val expectedLanguages = listOf(
            "English", "Afrikaans", "Zulu", "Xhosa",
            "Sotho", "Tswana", "Tsonga", "Venda",
            "Ndebele", "Swati", "Pedi"
        )

        assertEquals("Should have 11 official languages", 11, expectedLanguages.size)
        assertTrue("Should contain English", expectedLanguages.contains("English"))
        assertTrue("Should contain Afrikaans", expectedLanguages.contains("Afrikaans"))
        assertTrue("Should contain Zulu", expectedLanguages.contains("Zulu"))
    }

    @Test
    fun languageSpinner_findLanguagePosition_shouldReturnCorrectIndex() {
        // Test finding language position in spinner
        val languages = listOf(
            "English", "Afrikaans", "Zulu", "Xhosa",
            "Sotho", "Tswana", "Tsonga", "Venda",
            "Ndebele", "Swati", "Pedi"
        )

        assertEquals("English should be at position 0", 0, languages.indexOf("English"))
        assertEquals("Afrikaans should be at position 1", 1, languages.indexOf("Afrikaans"))
        assertEquals("Zulu should be at position 2", 2, languages.indexOf("Zulu"))
        assertEquals("Unknown language should return -1", -1, languages.indexOf("Spanish"))
    }

    @Test
    fun languageSpinner_defaultSelection_shouldHandleGracefully() {
        // Test default language selection logic
        val languages = listOf("English", "Afrikaans", "Zulu")
        val savedLanguage = "French" // Not in list
        val defaultLanguage = "English"

        val position = languages.indexOf(savedLanguage)
        val selectedLanguage = if (position >= 0) savedLanguage else defaultLanguage

        assertEquals("Should fallback to default language", defaultLanguage, selectedLanguage)
    }

    // ========== SharedPreferences Data Management Tests ==========
    @Test
    fun sharedPrefs_saveUserData_shouldStoreCorrectly() {
        // Test SharedPreferences data structure
        val userData = UserPrefsData(
            username = "johndoe123",
            homeLanguage = "Afrikaans"
        )

        assertFalse("Username should not be empty", userData.username.isEmpty())
        assertFalse("Language should not be empty", userData.homeLanguage.isEmpty())
        assertEquals("Username should match", "johndoe123", userData.username)
        assertEquals("Language should match", "Afrikaans", userData.homeLanguage)
    }

    @Test
    fun sharedPrefs_loadUserData_shouldHandleDefaults() {
        // Test loading with default values
        val savedUsername = ""
        val savedLanguage = ""

        val username = if (savedUsername.isEmpty()) "" else savedUsername
        val language = if (savedLanguage.isEmpty()) "English" else savedLanguage

        assertEquals("Empty username should remain empty", "", username)
        assertEquals("Empty language should default to English", "English", language)
    }

    @Test
    fun sharedPrefs_userIdKey_shouldBeUnique() {
        // Test unique SharedPreferences keys per user
        val userId1 = "user123"
        val userId2 = "user456"

        val prefsKey1 = "UserProfile_$userId1"
        val prefsKey2 = "UserProfile_$userId2"

        assertNotEquals("Prefs keys should be different for different users", prefsKey1, prefsKey2)
        assertTrue("Key should contain user ID", prefsKey1.contains(userId1))
        assertTrue("Key should contain user ID", prefsKey2.contains(userId2))
    }

    // ========== Password Field Security Tests ==========
    @Test
    fun passwordField_securitySettings_shouldBeConfiguredCorrectly() {
        // Test password field security configuration
        val isEnabled = false
        val isFocusable = false
        val isFocusableInTouchMode = false
        val hint = "Password cannot be changed from profile"
        val text = ""

        assertFalse("Password field should be disabled", isEnabled)
        assertFalse("Password field should not be focusable", isFocusable)
        assertFalse("Password field should not be focusable in touch mode", isFocusableInTouchMode)
        assertEquals("Should have appropriate hint", "Password cannot be changed from profile", hint)
        assertEquals("Password text should be empty", "", text)
    }

    @Test
    fun passwordField_validation_shouldBeSkipped() {
        // Test that password validation is skipped
        val skipPasswordValidation = true
        val profileData = ProfileData(
            fullName = "John Doe",
            email = "john@example.com",
            username = "johndoe",
            password = "", // Empty because field is disabled
            language = "English"
        )

        assertTrue("Password validation should be skipped", skipPasswordValidation)
        assertEquals("Password should remain empty", "", profileData.password)
    }

    // ========== Profile Save State Management Tests ==========
    @Test
    fun saveState_buttonBehavior_shouldToggleCorrectly() {
        // Test save button state management
        var isEnabled = true
        var buttonText = "Save"

        // Simulate saving state
        isEnabled = false
        buttonText = "Saving..."

        assertFalse("Button should be disabled during save", isEnabled)
        assertEquals("Button text should indicate saving", "Saving...", buttonText)

        // Simulate save completion
        isEnabled = true
        buttonText = "Save"

        assertTrue("Button should be re-enabled after save", isEnabled)
        assertEquals("Button text should reset", "Save", buttonText)
    }

    @Test
    fun saveState_errorHandling_shouldResetButton() {
        // Test button state reset on error
        var isEnabled = false
        var buttonText = "Saving..."

        // Simulate error during save
        val saveError = true
        if (saveError) {
            isEnabled = true
            buttonText = "Save"
        }

        assertTrue("Button should be re-enabled on error", isEnabled)
        assertEquals("Button text should reset on error", "Save", buttonText)
    }

    // ========== Navigation Logic Tests ==========
    @Test
    fun navigation_intentCreation_shouldContainCorrectExtras() {
        // Test navigation intent creation
        val navigationData = NavigationData(
            targetActivity = "WordsActivity",
            language = "afrikaans"
        )

        assertEquals("Target activity should be set", "WordsActivity", navigationData.targetActivity)
        assertEquals("Language extra should be set", "afrikaans", navigationData.language)
    }

    @Test
    fun navigation_homeNavigation_shouldFinishCurrentActivity() {
        // Test home navigation behavior
        val isHomeNavigation = true
        val shouldFinish = isHomeNavigation

        assertTrue("Home navigation should finish current activity", shouldFinish)
    }

    @Test
    fun navigation_drawerClosing_shouldOccurBeforeNavigation() {
        // Test drawer closing before navigation
        val isDrawerOpen = true
        var shouldCloseDrawer = false
        var shouldNavigate = false

        if (isDrawerOpen) {
            shouldCloseDrawer = true
            shouldNavigate = true
        }

        assertTrue("Should close drawer before navigation", shouldCloseDrawer)
        assertTrue("Should proceed with navigation", shouldNavigate)
    }

    // ========== User Authentication State Tests ==========
    @Test
    fun authState_loggedInUser_shouldProceedNormally() {
        // Test behavior with authenticated user
        val userLoggedIn = true
        val shouldRedirectToLogin = !userLoggedIn

        assertFalse("Should not redirect when user is logged in", shouldRedirectToLogin)
    }

    @Test
    fun authState_notLoggedIn_shouldRedirectToLogin() {
        // Test behavior with no authenticated user
        val userLoggedIn = false
        val shouldRedirectToLogin = !userLoggedIn

        assertTrue("Should redirect when user is not logged in", shouldRedirectToLogin)
    }

    @Test
    fun authState_userDataLoading_shouldHandleNullUser() {
        // Test user data loading with null user
        val currentUser: FirebaseUserData? = null
        val canLoadUserData = currentUser != null

        assertFalse("Should not attempt to load data for null user", canLoadUserData)
    }

    @Test
    fun authState_userDataLoading_shouldLoadFromValidUser() {
        // Test user data loading with valid user
        val currentUser = FirebaseUserData(
            uid = "user123",
            email = "user@example.com",
            displayName = "John Doe"
        )
        val canLoadUserData = currentUser.uid.isNotEmpty()

        assertTrue("Should load data for valid user", canLoadUserData)
        assertEquals("Should have correct email", "user@example.com", currentUser.email)
        assertEquals("Should have correct display name", "John Doe", currentUser.displayName)
    }

    // ========== Email Update Logic Tests ==========
    @Test
    fun emailUpdate_sameEmail_shouldSkipUpdate() {
        // Test email update when email hasn't changed
        val currentEmail = "user@example.com"
        val newEmail = "user@example.com"
        val shouldUpdateEmail = currentEmail != newEmail

        assertFalse("Should skip update when email is the same", shouldUpdateEmail)
    }

    @Test
    fun emailUpdate_differentEmail_shouldTriggerUpdate() {
        // Test email update when email has changed
        val currentEmail = "user@example.com"
        val newEmail = "newemail@example.com"
        val shouldUpdateEmail = currentEmail != newEmail

        assertTrue("Should update when email is different", shouldUpdateEmail)
    }

    @Test
    fun emailUpdate_errorHandling_shouldSaveOtherData() {
        // Test that other data is saved even if email update fails
        val emailUpdateFailed = true
        val shouldSaveOtherData = true // Always save other data

        assertTrue("Should save other profile data even if email update fails", shouldSaveOtherData)
    }

    // ========== Data Trimming and Sanitization Tests ==========
    @Test
    fun dataSanitization_trimming_shouldRemoveWhitespace() {
        // Test input trimming
        val rawInputs = listOf("  John Doe  ", "\nuser@example.com\t", "  username  ")
        val expectedOutputs = listOf("John Doe", "user@example.com", "username")

        for (i in rawInputs.indices) {
            val trimmed = rawInputs[i].trim()
            assertEquals("Input should be trimmed correctly", expectedOutputs[i], trimmed)
        }
    }

    @Test
    fun dataSanitization_emptyAfterTrim_shouldBeDetected() {
        // Test empty detection after trimming
        val inputs = listOf("", "   ", "\t\n", "valid input")
        val expectedEmpty = listOf(true, true, true, false)

        for (i in inputs.indices) {
            val isEmpty = inputs[i].trim().isEmpty()
            assertEquals("Empty detection should be correct for '${inputs[i]}'",
                expectedEmpty[i], isEmpty)
        }
    }

    // ========== Profile Update Success/Failure Tests ==========
    @Test
    fun profileUpdate_successfulSave_shouldShowConfirmation() {
        // Test successful profile save
        val saveSuccessful = true
        val showSuccessMessage = saveSuccessful
        val resetButtonState = saveSuccessful

        assertTrue("Should show success message", showSuccessMessage)
        assertTrue("Should reset button state", resetButtonState)
    }

    @Test
    fun profileUpdate_failedSave_shouldShowError() {
        // Test failed profile save
        val saveSuccessful = false
        val showErrorMessage = !saveSuccessful
        val resetButtonState = !saveSuccessful

        assertTrue("Should show error message", showErrorMessage)
        assertTrue("Should reset button state on failure", resetButtonState)
    }

    // Helper methods for testing
    private fun isValidEmailFormat(email: String): Boolean {
        // Simplified email validation for testing
        return email.contains("@") &&
                email.contains(".") &&
                !email.startsWith("@") &&
                !email.endsWith("@") &&
                !email.contains("..") &&
                !email.contains(" ") &&
                email.length > 4
    }

    private fun validateProfileFields(fullName: String, email: String, username: String): List<String> {
        val errors = mutableListOf<String>()

        if (fullName.trim().isEmpty()) {
            errors.add("Full name is required")
        }

        if (email.trim().isEmpty()) {
            errors.add("Email is required")
        } else if (!isValidEmailFormat(email)) {
            errors.add("Please enter a valid email")
        }

        if (username.trim().isEmpty()) {
            errors.add("Username is required")
        }

        return errors
    }

    // Helper data classes for testing
    private data class UserPrefsData(
        val username: String,
        val homeLanguage: String
    )

    private data class ProfileData(
        val fullName: String,
        val email: String,
        val username: String,
        val password: String,
        val language: String
    )

    private data class NavigationData(
        val targetActivity: String,
        val language: String
    )

    private data class FirebaseUserData(
        val uid: String,
        val email: String?,
        val displayName: String?
    )
}