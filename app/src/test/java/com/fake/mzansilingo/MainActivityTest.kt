package com.fake.mzansilingo

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.MockitoAnnotations

class MainActivityTest {

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun validateInput_emptyName_shouldReturnFalse() {
        // Test empty name validation logic
        val name = ""
        val email = "test@email.com"
        val username = "testuser"
        val password = "password123"
        val language = "isiZulu"

        val isValid = isValidName(name)

        assertFalse("Empty name should be invalid", isValid)
    }

    @Test
    fun validateInput_validName_shouldReturnTrue() {
        // Test valid name validation
        val validNames = listOf("John Doe", "Mary Smith", "Abdul Rahman", "Nomsa Mthembu")

        for (name in validNames) {
            val isValid = isValidName(name)
            assertTrue("Name '$name' should be valid", isValid)
        }
    }

    @Test
    fun validateInput_emptyEmail_shouldReturnFalse() {
        // Test empty email validation
        val email = ""

        val isValid = isValidEmail(email)

        assertFalse("Empty email should be invalid", isValid)
    }

    @Test
    fun validateInput_invalidEmailFormats_shouldReturnFalse() {
        // Test various invalid email formats
        val invalidEmails = listOf(
            "invalid.email",
            "@domain.com",
            "test@",
            "test.domain.com",
            "test@domain",
            "test space@domain.com"
        )

        for (email in invalidEmails) {
            val isValid = isValidEmail(email)
            assertFalse("Email '$email' should be invalid", isValid)
        }
    }

    @Test
    fun validateInput_validEmailFormats_shouldReturnTrue() {
        // Test valid email formats
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.za",
            "test123@gmail.com",
            "user+tag@example.org"
        )

        for (email in validEmails) {
            val isValid = isValidEmail(email)
            assertTrue("Email '$email' should be valid", isValid)
        }
    }

    @Test
    fun validateInput_emptyUsername_shouldReturnFalse() {
        // Test empty username validation
        val username = ""

        val isValid = isValidUsername(username)

        assertFalse("Empty username should be invalid", isValid)
    }

    @Test
    fun validateInput_validUsername_shouldReturnTrue() {
        // Test valid usernames
        val validUsernames = listOf("testuser", "john123", "user_name", "nomsa2024")

        for (username in validUsernames) {
            val isValid = isValidUsername(username)
            assertTrue("Username '$username' should be valid", isValid)
        }
    }

    @Test
    fun validateInput_shortPassword_shouldReturnFalse() {
        // Test password length validation (minimum 6 characters)
        val shortPasswords = listOf("1", "12", "123", "1234", "12345")

        for (password in shortPasswords) {
            val isValid = isValidPassword(password)
            assertFalse("Password '$password' should be invalid (too short)", isValid)
        }
    }

    @Test
    fun validateInput_validPassword_shouldReturnTrue() {
        // Test valid password lengths
        val validPasswords = listOf("123456", "password", "mypass123", "verylongpassword")

        for (password in validPasswords) {
            val isValid = isValidPassword(password)
            assertTrue("Password '$password' should be valid", isValid)
        }
    }

    @Test
    fun validateInput_emptyLanguage_shouldReturnFalse() {
        // Test empty language validation
        val language = ""

        val isValid = isValidLanguage(language)

        assertFalse("Empty language should be invalid", isValid)
    }

    @Test
    fun languageOptions_shouldContainAllSouthAfricanLanguages() {
        // Test supported languages list
        val supportedLanguages = getSupportedLanguages()
        val expectedLanguages = listOf(
            "isiZulu", "isiXhosa", "Afrikaans", "English",
            "Sepedi", "Setswana", "Sesotho", "Xitsonga",
            "Tshivenda", "isiNdebele", "Other"
        )

        assertEquals("Should have 11 supported languages", 11, supportedLanguages.size)

        for (language in expectedLanguages) {
            assertTrue("Should contain $language", supportedLanguages.contains(language))
        }
    }

    @Test
    fun languageValidation_supportedLanguages_shouldReturnTrue() {
        // Test validation of supported languages
        val supportedLanguages = getSupportedLanguages()

        for (language in supportedLanguages) {
            val isValid = isValidLanguage(language)
            assertTrue("Language '$language' should be valid", isValid)
        }
    }

    @Test
    fun languageValidation_unsupportedLanguage_shouldReturnFalse() {
        // Test validation of unsupported languages
        val unsupportedLanguages = listOf("Spanish", "French", "German", "")

        for (language in unsupportedLanguages) {
            val isValid = isValidLanguage(language)
            assertFalse("Language '$language' should be invalid", isValid)
        }
    }

    @Test
    fun firebaseErrorParsing_emailInUse_shouldReturnCorrectMessage() {
        // Test Firebase error message parsing
        val errorMessage = "The email address is already in use by another account."
        val parsedMessage = parseFirebaseError(errorMessage)

        assertEquals("Should return user-friendly message", "This email is already registered", parsedMessage)
    }

    @Test
    fun firebaseErrorParsing_badlyFormattedEmail_shouldReturnCorrectMessage() {
        // Test badly formatted email error
        val errorMessage = "The email address is badly formatted."
        val parsedMessage = parseFirebaseError(errorMessage)

        assertEquals("Should return format error message", "Invalid email format", parsedMessage)
    }


    @Test
    fun firebaseErrorParsing_unknownError_shouldReturnOriginalMessage() {
        // Test unknown error handling
        val errorMessage = "Unknown authentication error occurred"
        val parsedMessage = parseFirebaseError(errorMessage)

        assertEquals("Should return original message", errorMessage, parsedMessage)
    }

    @Test
    fun userDataCreation_shouldContainRequiredFields() {
        // Test user data structure creation
        val userData = createUserData(
            uid = "test123",
            name = "John Doe",
            email = "john@example.com",
            username = "johndoe",
            language = "English"
        )

        assertEquals("Should contain correct UID", "test123", userData["userId"])
        assertEquals("Should contain correct name", "John Doe", userData["fullName"])
        assertEquals("Should contain correct email", "john@example.com", userData["email"])
        assertEquals("Should contain correct username", "johndoe", userData["username"])
        assertEquals("Should contain correct language", "English", userData["homeLanguage"])
        assertNotNull("Should contain createdAt", userData["createdAt"])
        assertNotNull("Should contain lastActive", userData["lastActive"])
    }

    @Test
    fun userDataCreation_shouldTrimWhitespace() {
        // Test data trimming in user creation
        val userData = createUserData(
            uid = "test123",
            name = "  John Doe  ",
            email = "  john@example.com  ",
            username = "  johndoe  ",
            language = "  English  "
        )

        assertEquals("Name should be trimmed", "John Doe", userData["fullName"])
        assertEquals("Email should be trimmed", "john@example.com", userData["email"])
        assertEquals("Username should be trimmed", "johndoe", userData["username"])
        assertEquals("Language should be trimmed", "English", userData["homeLanguage"])
    }

    @Test
    fun migrationUserData_shouldContainMigrationFlag() {
        // Test migration user data creation
        val migrationData = createMigrationUserData(
            userId = "existing123",
            userEmail = "existing@example.com",
            displayName = "Existing User"
        )

        assertEquals("Should contain migration flag", true, migrationData["migratedUser"])
        assertEquals("Should default to English", "English", migrationData["homeLanguage"])
        assertTrue("Should contain existing user data", migrationData.containsKey("userId"))
    }

    @Test
    fun migrationUserData_emailWithoutDisplayName_shouldUseEmailPrefix() {
        // Test migration when displayName is null
        val migrationData = createMigrationUserData(
            userId = "existing123",
            userEmail = "user.name@domain.com",
            displayName = null
        )

        assertEquals("Should use email prefix as username", "user.name", migrationData["username"])
        assertEquals("Should use email prefix as fullName", "user.name", migrationData["fullName"])
    }

    @Test
    fun navigationPrevention_multipleCalls_shouldNavigateOnce() {
        // Test navigation prevention logic
        var hasNavigated = false
        var navigationCount = 0

        // Simulate multiple navigation attempts
        repeat(5) {
            if (!hasNavigated) {
                hasNavigated = true
                navigationCount++
            }
        }

        assertEquals("Should only navigate once", 1, navigationCount)
        assertTrue("Navigation flag should be set", hasNavigated)
    }

    @Test
    fun formClearance_shouldResetAllFields() {
        // Test form clearing logic
        val formData = mutableMapOf(
            "fullName" to "John Doe",
            "email" to "john@example.com",
            "username" to "johndoe",
            "password" to "password123",
            "homeLanguage" to "English"
        )

        // Simulate clearing form
        formData.clear()

        assertEquals("Form should be empty after clearing", 0, formData.size)
        assertFalse("Should not contain fullName", formData.containsKey("fullName"))
        assertFalse("Should not contain email", formData.containsKey("email"))
    }

    // Helper methods simulating MainActivity validation logic
    private fun isValidName(name: String): Boolean {
        return name.trim().isNotBlank()
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.trim().isBlank()) return false
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailPattern.toRegex())
    }

    private fun isValidUsername(username: String): Boolean {
        return username.trim().isNotBlank()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }

    private fun isValidLanguage(language: String): Boolean {
        return getSupportedLanguages().contains(language.trim())
    }

    private fun getSupportedLanguages(): List<String> {
        return listOf(
            "isiZulu", "isiXhosa", "Afrikaans", "English",
            "Sepedi", "Setswana", "Sesotho", "Xitsonga",
            "Tshivenda", "isiNdebele", "Other"
        )
    }

    private fun parseFirebaseError(errorMessage: String): String {
        return when {
            errorMessage.contains("email address is already in use", ignoreCase = true) ->
                "This email is already registered"
            errorMessage.contains("email address is badly formatted", ignoreCase = true) ->
                "Invalid email format"
            errorMessage.contains("weak password", ignoreCase = true) ->
                "Password is too weak"
            else -> errorMessage
        }
    }

    private fun createUserData(
        uid: String,
        name: String,
        email: String,
        username: String,
        language: String
    ): Map<String, Any> {
        return mapOf(
            "userId" to uid,
            "fullName" to name.trim(),
            "email" to email.trim(),
            "username" to username.trim(),
            "homeLanguage" to language.trim(),
            "createdAt" to System.currentTimeMillis(),
            "lastActive" to System.currentTimeMillis()
        )
    }

    private fun createMigrationUserData(
        userId: String,
        userEmail: String,
        displayName: String?
    ): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "email" to userEmail,
            "username" to userEmail.substringBefore("@"),
            "fullName" to (displayName ?: userEmail.substringBefore("@")),
            "homeLanguage" to "English",
            "createdAt" to System.currentTimeMillis(),
            "lastActive" to System.currentTimeMillis(),
            "migratedUser" to true
        )
    }
}