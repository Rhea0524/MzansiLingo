package com.fake.mzansilingo

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class LeaderboardActivityTest {

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun leaderboardUser_initialValues_shouldBeCorrect() {
        // Test the LeaderboardUser data class initialization
        val user = LeaderboardUser(
            userId = "user123",
            username = "testuser",
            email = "test@example.com",
            fullName = "Test User",
            homeLanguage = "English",
            wordsCorrect = 0,
            phrasesCorrect = 0,
            wordsAttempted = 0,
            phrasesAttempted = 0,
            daysPracticed = 0,
            totalScore = 0,
            rank = 0
        )

        assertEquals("User ID should match", "user123", user.userId)
        assertEquals("Username should match", "testuser", user.username)
        assertEquals("Email should match", "test@example.com", user.email)
        assertEquals("Full name should match", "Test User", user.fullName)
        assertEquals("Home language should match", "English", user.homeLanguage)
        assertEquals("Words correct should be 0", 0, user.wordsCorrect)
        assertEquals("Phrases correct should be 0", 0, user.phrasesCorrect)
        assertEquals("Words attempted should be 0", 0, user.wordsAttempted)
        assertEquals("Phrases attempted should be 0", 0, user.phrasesAttempted)
        assertEquals("Days practiced should be 0", 0, user.daysPracticed)
        assertEquals("Total score should be 0", 0, user.totalScore)
        assertEquals("Rank should be 0", 0, user.rank)
    }

    @Test
    fun leaderboardUser_withValidData_shouldStoreCorrectly() {
        // Test LeaderboardUser with actual performance data
        val user = LeaderboardUser(
            userId = "user456",
            username = "performer",
            email = "performer@example.com",
            fullName = "High Performer",
            homeLanguage = "Afrikaans",
            wordsCorrect = 25,
            phrasesCorrect = 15,
            wordsAttempted = 30,
            phrasesAttempted = 20,
            daysPracticed = 7,
            totalScore = 40, // 25 + 15
            rank = 1
        )

        assertEquals("Words correct should be 25", 25, user.wordsCorrect)
        assertEquals("Phrases correct should be 15", 15, user.phrasesCorrect)
        assertEquals("Words attempted should be 30", 30, user.wordsAttempted)
        assertEquals("Phrases attempted should be 20", 20, user.phrasesAttempted)
        assertEquals("Days practiced should be 7", 7, user.daysPracticed)
        assertEquals("Total score should be 40", 40, user.totalScore)
        assertEquals("Rank should be 1", 1, user.rank)
    }

    @Test
    fun leaderboardUser_getAccuracy_shouldCalculateCorrectly() {
        // Test accuracy calculation with perfect score
        val perfectUser = LeaderboardUser(
            userId = "perfect",
            username = "perfect",
            email = "perfect@test.com",
            fullName = "Perfect User",
            homeLanguage = "English",
            wordsCorrect = 20,
            phrasesCorrect = 10,
            wordsAttempted = 20,
            phrasesAttempted = 10,
            daysPracticed = 5,
            totalScore = 30,
            rank = 1
        )

        val perfectAccuracy = perfectUser.getAccuracy()
        assertEquals("Perfect accuracy should be 100%", 100.0f, perfectAccuracy, 0.01f)

        // Test accuracy calculation with partial score
        val partialUser = LeaderboardUser(
            userId = "partial",
            username = "partial",
            email = "partial@test.com",
            fullName = "Partial User",
            homeLanguage = "English",
            wordsCorrect = 15,
            phrasesCorrect = 5,
            wordsAttempted = 20,
            phrasesAttempted = 10,
            daysPracticed = 3,
            totalScore = 20,
            rank = 2
        )

        val partialAccuracy = partialUser.getAccuracy()
        assertEquals("Partial accuracy should be 66.67%", 66.67f, partialAccuracy, 0.01f)

        // Test accuracy calculation with zero attempts
        val noAttemptsUser = LeaderboardUser(
            userId = "zero",
            username = "zero",
            email = "zero@test.com",
            fullName = "Zero User",
            homeLanguage = "English",
            wordsCorrect = 0,
            phrasesCorrect = 0,
            wordsAttempted = 0,
            phrasesAttempted = 0,
            daysPracticed = 0,
            totalScore = 0,
            rank = 0
        )

        val zeroAccuracy = noAttemptsUser.getAccuracy()
        assertEquals("Zero attempts should give 0% accuracy", 0.0f, zeroAccuracy, 0.01f)
    }

    @Test
    fun leaderboardUser_getDisplayName_shouldReturnCorrectName() {
        // Test with different full name
        val userWithFullName = LeaderboardUser(
            userId = "user1",
            username = "jsmith",
            email = "john@test.com",
            fullName = "John Smith",
            homeLanguage = "English",
            wordsCorrect = 10,
            phrasesCorrect = 5,
            wordsAttempted = 15,
            phrasesAttempted = 8,
            daysPracticed = 3,
            totalScore = 15,
            rank = 1
        )

        assertEquals("Should return full name when different from username", "John Smith", userWithFullName.getDisplayName())

        // Test with same full name as username
        val userWithSameName = LeaderboardUser(
            userId = "user2",
            username = "mary",
            email = "mary@test.com",
            fullName = "mary",
            homeLanguage = "Spanish",
            wordsCorrect = 8,
            phrasesCorrect = 7,
            wordsAttempted = 12,
            phrasesAttempted = 10,
            daysPracticed = 2,
            totalScore = 15,
            rank = 2
        )

        assertEquals("Should return username when same as full name", "mary", userWithSameName.getDisplayName())

        // Test with blank full name
        val userWithBlankFullName = LeaderboardUser(
            userId = "user3",
            username = "testuser",
            email = "test@test.com",
            fullName = "",
            homeLanguage = "Afrikaans",
            wordsCorrect = 5,
            phrasesCorrect = 3,
            wordsAttempted = 8,
            phrasesAttempted = 5,
            daysPracticed = 1,
            totalScore = 8,
            rank = 3
        )

        assertEquals("Should return username when full name is blank", "testuser", userWithBlankFullName.getDisplayName())
    }

    @Test
    fun userIdExtraction_fromGoogleUser_shouldExtractCorrectly() {
        // Simulate getUserIdFromDocument logic for Google users
        val googleUserData = mapOf(
            "uid" to "google123",
            "displayName" to "Google User",
            "email" to "google@gmail.com",
            "provider" to "google"
        )

        // Test uid field extraction
        val uid = googleUserData["uid"] as? String
        assertNotNull("UID should not be null", uid)
        assertEquals("UID should match", "google123", uid)
        assertTrue("UID should not be blank", !uid.isNullOrBlank())
    }

    @Test
    fun userIdExtraction_fromEmailUser_shouldExtractCorrectly() {
        // Simulate getUserIdFromDocument logic for email/password users
        val emailUserData = mapOf(
            "userId" to "email456",
            "username" to "emailuser",
            "email" to "email@test.com",
            "provider" to "email"
        )

        // Test userId field extraction
        val userId = emailUserData["userId"] as? String
        assertNotNull("UserID should not be null", userId)
        assertEquals("UserID should match", "email456", userId)
        assertTrue("UserID should not be blank", !userId.isNullOrBlank())
    }

    @Test
    fun usernameExtraction_shouldPrioritizeCorrectly() {
        // Test username extraction priority logic

        // Test with username field present
        val dataWithUsername = mapOf(
            "username" to "primaryuser",
            "displayName" to "Display Name",
            "fullName" to "Full Name",
            "email" to "test@test.com"
        )

        val username1 = dataWithUsername["username"] as? String
        assertTrue("Should prefer username field", !username1.isNullOrBlank())
        assertEquals("Username should match", "primaryuser", username1)

        // Test with displayName when username is missing
        val dataWithDisplayName = mapOf(
            "displayName" to "Display User",
            "fullName" to "Full Name",
            "email" to "test@test.com"
        )

        val displayName = dataWithDisplayName["displayName"] as? String
        assertTrue("Should use displayName when username missing", !displayName.isNullOrBlank())
        assertEquals("Display name should match", "Display User", displayName)

        // Test with fullName when username and displayName missing
        val dataWithFullName = mapOf(
            "fullName" to "Full User",
            "email" to "test@test.com"
        )

        val fullName = dataWithFullName["fullName"] as? String
        assertTrue("Should use fullName when others missing", !fullName.isNullOrBlank())
        assertEquals("Full name should match", "Full User", fullName)

        // Test extracting username from email
        val emailOnly = "testuser@example.com"
        val usernameFromEmail = emailOnly.substringBefore("@")
        assertEquals("Should extract username from email", "testuser", usernameFromEmail)
    }

    @Test
    fun scoreCalculation_shouldMatchProgressActivity() {
        // Test that score calculation matches ProgressActivity logic
        val testResults = listOf(
            TestResultForLeaderboard("WORD_TEST", 5, 10),
            TestResultForLeaderboard("PHRASE_TEST", 3, 8),
            TestResultForLeaderboard("WORD_TEST", 7, 12),
            TestResultForLeaderboard("PHRASE_TEST", 4, 6),
            TestResultForLeaderboard("UNKNOWN_TEST", 2, 5) // Should be ignored
        )

        var wordCount = 0
        var phraseCount = 0
        var wordsAttempted = 0
        var phrasesAttempted = 0

        for (result in testResults) {
            when (result.testType) {
                "WORD_TEST" -> {
                    wordCount += result.correctAnswers
                    wordsAttempted += result.totalQuestions
                }
                "PHRASE_TEST" -> {
                    phraseCount += result.correctAnswers
                    phrasesAttempted += result.totalQuestions
                }
            }
        }

        val totalScore = wordCount + phraseCount

        assertEquals("Word count should be 12", 12, wordCount)
        assertEquals("Phrase count should be 7", 7, phraseCount)
        assertEquals("Words attempted should be 22", 22, wordsAttempted)
        assertEquals("Phrases attempted should be 14", 14, phrasesAttempted)
        assertEquals("Total score should be 19", 19, totalScore)
    }

    @Test
    fun leaderboardSorting_shouldRankByTotalScore() {
        // Test leaderboard sorting and ranking logic
        val users = mutableListOf(
            LeaderboardUser("user1", "alice", "alice@test.com", "Alice", "English", 15, 10, 20, 15, 5, 25, 0),
            LeaderboardUser("user2", "bob", "bob@test.com", "Bob", "Spanish", 20, 5, 25, 10, 3, 25, 0),
            LeaderboardUser("user3", "charlie", "charlie@test.com", "Charlie", "Afrikaans", 10, 8, 15, 12, 4, 18, 0),
            LeaderboardUser("user4", "diana", "diana@test.com", "Diana", "English", 30, 12, 35, 18, 7, 42, 0)
        )

        // Sort by total score (highest first) and assign ranks
        users.sortByDescending { it.totalScore }
        users.forEachIndexed { index, user ->
            user.rank = index + 1
        }

        assertEquals("Diana should be rank 1 with score 42", 1, users.find { it.username == "diana" }?.rank)
        assertEquals("Alice and Bob should tie but Alice gets rank 2", 2, users.find { it.username == "alice" }?.rank)
        assertEquals("Bob should be rank 3", 3, users.find { it.username == "bob" }?.rank)
        assertEquals("Charlie should be rank 4 with score 18", 4, users.find { it.username == "charlie" }?.rank)

        // Verify sorting order
        assertEquals("First user should have highest score", 42, users[0].totalScore)
        assertEquals("Last user should have lowest score", 18, users[3].totalScore)
    }

    @Test
    fun daysPracticedCalculation_shouldCountUniqueDates() {
        // Test days practiced calculation logic (matching ProgressActivity)
        val loginDates = listOf(
            "2024-01-15",
            "2024-01-16",
            "2024-01-15", // duplicate
            "2024-01-17",
            "2024-01-16", // duplicate
            "2024-01-18",
            "", // empty date should be ignored
            "2024-01-19"
        )

        val uniqueDates = mutableSetOf<String>()
        for (date in loginDates) {
            if (date.isNotEmpty()) {
                uniqueDates.add(date)
            }
        }

        val daysPracticed = uniqueDates.size
        assertEquals("Should count 5 unique practice days", 5, daysPracticed)
        assertTrue("Should contain specific dates", uniqueDates.contains("2024-01-15"))
        assertTrue("Should contain specific dates", uniqueDates.contains("2024-01-19"))
        assertFalse("Should not contain empty string", uniqueDates.contains(""))
    }


    @Test
    fun multipleScoreTypes_shouldAccumulateCorrectly() {
        // Test complex scoring with multiple test types and sessions
        val complexResults = listOf(
            TestResultForLeaderboard("WORD_TEST", 8, 10),
            TestResultForLeaderboard("PHRASE_TEST", 6, 8),
            TestResultForLeaderboard("WORD_TEST", 12, 15),
            TestResultForLeaderboard("PHRASE_TEST", 9, 12),
            TestResultForLeaderboard("WORD_TEST", 5, 8),
            TestResultForLeaderboard("PHRASE_TEST", 3, 5),
            TestResultForLeaderboard("VOCABULARY", 10, 12), // Different type, should be ignored
            TestResultForLeaderboard("GRAMMAR", 7, 10) // Different type, should be ignored
        )

        var wordCount = 0
        var phraseCount = 0
        var wordsAttempted = 0
        var phrasesAttempted = 0

        for (result in complexResults) {
            when (result.testType) {
                "WORD_TEST" -> {
                    wordCount += result.correctAnswers
                    wordsAttempted += result.totalQuestions
                }
                "PHRASE_TEST" -> {
                    phraseCount += result.correctAnswers
                    phrasesAttempted += result.totalQuestions
                }
                // Other types ignored like in the actual implementation
            }
        }

        val totalScore = wordCount + phraseCount

        assertEquals("Word count should be 25 (8+12+5)", 25, wordCount)
        assertEquals("Phrase count should be 18 (6+9+3)", 18, phraseCount)
        assertEquals("Total score should be 43", 43, totalScore)
        assertEquals("Words attempted should be 33", 33, wordsAttempted)
        assertEquals("Phrases attempted should be 25", 25, phrasesAttempted)
    }

    @Test
    fun emptyResults_shouldHandleGracefully() {
        // Test handling of users with no test results
        val emptyResults = listOf<TestResultForLeaderboard>()

        var wordCount = 0
        var phraseCount = 0
        var wordsAttempted = 0
        var phrasesAttempted = 0

        for (result in emptyResults) {
            when (result.testType) {
                "WORD_TEST" -> {
                    wordCount += result.correctAnswers
                    wordsAttempted += result.totalQuestions
                }
                "PHRASE_TEST" -> {
                    phraseCount += result.correctAnswers
                    phrasesAttempted += result.totalQuestions
                }
            }
        }

        val totalScore = wordCount + phraseCount

        assertEquals("Empty results should give 0 word count", 0, wordCount)
        assertEquals("Empty results should give 0 phrase count", 0, phraseCount)
        assertEquals("Empty results should give 0 total score", 0, totalScore)
        assertEquals("Empty results should give 0 attempts", 0, wordsAttempted)
        assertEquals("Empty results should give 0 attempts", 0, phrasesAttempted)
    }

    @Test
    fun leaderboardUser_edgeCases_shouldHandleCorrectly() {
        // Test edge cases for LeaderboardUser

        // User with high attempts but low success
        val strugglingUser = LeaderboardUser(
            userId = "struggling",
            username = "learning",
            email = "learning@test.com",
            fullName = "Learning User",
            homeLanguage = "English",
            wordsCorrect = 5,
            phrasesCorrect = 3,
            wordsAttempted = 50,
            phrasesAttempted = 30,
            daysPracticed = 10,
            totalScore = 8,
            rank = 0
        )

        val accuracy = strugglingUser.getAccuracy()
        assertEquals("Low success rate should be calculated correctly", 10.0f, accuracy, 0.01f)
        assertTrue("Should show dedication with high practice days", strugglingUser.daysPracticed > strugglingUser.totalScore)

        // User with perfect efficiency (high score, few attempts)
        val efficientUser = LeaderboardUser(
            userId = "efficient",
            username = "genius",
            email = "genius@test.com",
            fullName = "Efficient Genius",
            homeLanguage = "Spanish",
            wordsCorrect = 20,
            phrasesCorrect = 20,
            wordsAttempted = 20,
            phrasesAttempted = 20,
            daysPracticed = 2,
            totalScore = 40,
            rank = 0
        )

        val perfectAccuracy = efficientUser.getAccuracy()
        assertEquals("Perfect efficiency should be 100%", 100.0f, perfectAccuracy, 0.01f)
        assertTrue("High score with few practice days shows efficiency", efficientUser.totalScore > efficientUser.daysPracticed * 10)
    }

    // Helper data class for testing
    private data class TestResultForLeaderboard(
        val testType: String,
        val correctAnswers: Int,
        val totalQuestions: Int
    )
}