package com.fake.mzansilingo

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.*

class ExportTestDataActivityTest {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testResult_initialValues_shouldBeCorrect() {
        // Test the TestResult data class initialization
        val testResult = TestResult()

        assertEquals("Auth user ID should be empty", "", testResult.authUserId)
        assertEquals("Test type should be empty", "", testResult.testType)
        assertEquals("Language should be empty", "", testResult.language)
        assertEquals("Score percentage should be 0.0", 0.0, testResult.scorePercentage, 0.001)
        assertEquals("Total questions should be 0", 0, testResult.totalQuestions)
        assertEquals("Correct answers should be 0", 0, testResult.correctAnswers)
        assertEquals("Incorrect answers should be 0", 0, testResult.incorrectAnswers)
        assertEquals("Test duration should be 0", 0L, testResult.testDuration)
        assertEquals("Timestamp should be 0", 0L, testResult.timestamp)
        assertEquals("Difficulty should be empty", "", testResult.difficulty)
        assertEquals("Category should be empty", "", testResult.category)
        assertEquals("Test ID should be empty", "", testResult.testId)
        assertEquals("User ID should be empty", "", testResult.userId)
        assertEquals("Start time should be 0", 0L, testResult.startTime)
        assertEquals("End time should be 0", 0L, testResult.endTime)
    }

    @Test
    fun testResult_withValidData_shouldStoreCorrectly() {
        // Test TestResult with actual values
        val testResult = TestResult(
            authUserId = "user123",
            testType = "WORD_TEST",
            language = "English",
            scorePercentage = 85.5,
            totalQuestions = 20,
            correctAnswers = 17,
            incorrectAnswers = 3,
            testDuration = 120000L,
            timestamp = System.currentTimeMillis(),
            difficulty = "Medium",
            category = "Vocabulary",
            testId = "test456",
            userId = "user123",
            startTime = System.currentTimeMillis() - 120000L,
            endTime = System.currentTimeMillis()
        )

        assertEquals("Auth user ID should match", "user123", testResult.authUserId)
        assertEquals("Test type should match", "WORD_TEST", testResult.testType)
        assertEquals("Language should match", "English", testResult.language)
        assertEquals("Score percentage should match", 85.5, testResult.scorePercentage, 0.001)
        assertEquals("Total questions should match", 20, testResult.totalQuestions)
        assertEquals("Correct answers should match", 17, testResult.correctAnswers)
        assertEquals("Incorrect answers should match", 3, testResult.incorrectAnswers)
        assertEquals("Test duration should match", 120000L, testResult.testDuration)
        assertEquals("Difficulty should match", "Medium", testResult.difficulty)
        assertEquals("Category should match", "Vocabulary", testResult.category)
    }

    @Test
    fun testResult_computedProperties_shouldCalculateCorrectly() {
        // Test computed properties for backwards compatibility
        val testResult = TestResult(
            scorePercentage = 75.0,
            testDuration = 90000L,
            timestamp = 1640995200000L // January 1, 2022 00:00:00 UTC
        )

        assertEquals("Score should equal scorePercentage", 75.0, testResult.score, 0.001)
        assertEquals("Time spent should equal testDuration", 90000L, testResult.timeSpent)
        assertEquals("Timestamp as date should convert correctly", Date(1640995200000L), testResult.timestampAsDate)
    }

    @Test
    fun filtering_byLanguage_shouldFilterCorrectly() {
        // Test language filtering logic
        val testResults = listOf(
            TestResult(language = "English", testType = "WORD_TEST", scorePercentage = 80.0),
            TestResult(language = "Zulu", testType = "PHRASE_TEST", scorePercentage = 70.0),
            TestResult(language = "English", testType = "WORD_TEST", scorePercentage = 90.0),
            TestResult(language = "Afrikaans", testType = "PHRASE_TEST", scorePercentage = 85.0),
            TestResult(language = "", testType = "WORD_TEST", scorePercentage = 75.0) // Empty language
        )

        // Test filtering by English (including empty language)
        val englishResults = testResults.filter { result ->
            result.language.equals("English", ignoreCase = true) || result.language.isEmpty()
        }
        assertEquals("Should find 3 English results (including empty)", 3, englishResults.size)

        // Test filtering by Zulu
        val zuluResults = testResults.filter { result ->
            result.language.equals("Zulu", ignoreCase = true)
        }
        assertEquals("Should find 1 Zulu result", 1, zuluResults.size)

        // Test "All Languages" filter
        val allResults = testResults.filter { true } // All languages selected
        assertEquals("Should find all 5 results", 5, allResults.size)
    }

    @Test
    fun filtering_byTestType_shouldFilterCorrectly() {
        // Test test type filtering logic
        val testResults = listOf(
            TestResult(testType = "WORD_TEST", language = "English", scorePercentage = 80.0),
            TestResult(testType = "PHRASE_TEST", language = "Zulu", scorePercentage = 70.0),
            TestResult(testType = "WORD_TEST", language = "English", scorePercentage = 90.0),
            TestResult(testType = "Mixed", language = "Afrikaans", scorePercentage = 85.0),
            TestResult(testType = "Vocabulary", language = "English", scorePercentage = 75.0)
        )

        // Test filtering by WORD_TEST
        val wordResults = testResults.filter { result ->
            result.testType.equals("WORD_TEST", ignoreCase = true)
        }
        assertEquals("Should find 2 WORD_TEST results", 2, wordResults.size)

        // Test filtering by PHRASE_TEST
        val phraseResults = testResults.filter { result ->
            result.testType.equals("PHRASE_TEST", ignoreCase = true)
        }
        assertEquals("Should find 1 PHRASE_TEST result", 1, phraseResults.size)

        // Test "All Types" filter
        val allResults = testResults.filter { true } // All types selected
        assertEquals("Should find all 5 results", 5, allResults.size)
    }

    @Test
    fun filtering_byDateRange_shouldFilterCorrectly() {
        // Test date range filtering logic
        val calendar = Calendar.getInstance()

        // Create dates
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, -5)
        val fiveDaysAgo = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, -5)
        val tenDaysAgo = calendar.time

        val testResults = listOf(
            TestResult(timestamp = today.time, testType = "WORD_TEST"),
            TestResult(timestamp = fiveDaysAgo.time, testType = "PHRASE_TEST"),
            TestResult(timestamp = tenDaysAgo.time, testType = "WORD_TEST")
        )

        // Test filtering from 7 days ago to today
        calendar.time = today
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        val fromDate = calendar.time

        val recentResults = testResults.filter { result ->
            val resultDate = Date(result.timestamp)
            resultDate >= fromDate
        }
        assertEquals("Should find 2 results from last 7 days", 2, recentResults.size)

        // Test filtering up to 3 days ago
        calendar.time = today
        calendar.add(Calendar.DAY_OF_MONTH, -3)
        val toDate = calendar.time

        val olderResults = testResults.filter { result ->
            val resultDate = Date(result.timestamp)
            resultDate <= toDate
        }
        assertEquals("Should find 2 results up to 3 days ago", 2, olderResults.size)
    }

    @Test
    fun summaryStats_averageScore_shouldCalculateCorrectly() {
        // Test average score calculation
        val testResults = listOf(
            TestResult(scorePercentage = 80.0),
            TestResult(scorePercentage = 70.0),
            TestResult(scorePercentage = 90.0),
            TestResult(scorePercentage = 85.0)
        )

        val averageScore = if (testResults.isNotEmpty()) {
            testResults.map { it.scorePercentage }.average()
        } else 0.0

        assertEquals("Average score should be 81.25", 81.25, averageScore, 0.001)

        // Test with empty list
        val emptyResults = listOf<TestResult>()
        val emptyAverage = if (emptyResults.isNotEmpty()) {
            emptyResults.map { it.scorePercentage }.average()
        } else 0.0

        assertEquals("Empty list average should be 0.0", 0.0, emptyAverage, 0.001)
    }

    @Test
    fun summaryStats_totalCount_shouldCountCorrectly() {
        // Test total count logic
        val testResults = listOf(
            TestResult(testType = "WORD_TEST"),
            TestResult(testType = "PHRASE_TEST"),
            TestResult(testType = "WORD_TEST"),
            TestResult(testType = "Mixed")
        )

        val totalTests = testResults.size
        assertEquals("Total tests should be 4", 4, totalTests)

        // Test with empty list
        val emptyResults = listOf<TestResult>()
        val emptyTotal = emptyResults.size
        assertEquals("Empty list total should be 0", 0, emptyTotal)
    }


    @Test
    fun dateFormatting_shouldFormatCorrectly() {
        // Test date formatting logic
        val testTimestamp = 1640995200000L // January 1, 2022 00:00:00 UTC
        val testDate = Date(testTimestamp)

        // Test date format (dd/MM/yyyy)
        val formattedDate = dateFormat.format(testDate)
        assertTrue("Date should be formatted as dd/MM/yyyy", formattedDate.matches("\\d{2}/\\d{2}/\\d{4}".toRegex()))

        // Test timestamp format (dd/MM/yyyy HH:mm)
        val formattedTimestamp = timestampFormat.format(testDate)
        assertTrue("Timestamp should be formatted as dd/MM/yyyy HH:mm",
            formattedTimestamp.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}".toRegex()))
    }

    @Test
    fun combinedFiltering_multipleConditions_shouldFilterCorrectly() {
        // Test combined filtering with multiple conditions
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val tenDaysAgo = calendar.time

        val testResults = listOf(
            TestResult(
                language = "English",
                testType = "WORD_TEST",
                timestamp = today.time - 86400000L, // 1 day ago
                scorePercentage = 85.0
            ),
            TestResult(
                language = "Zulu",
                testType = "WORD_TEST",
                timestamp = today.time - 86400000L, // 1 day ago
                scorePercentage = 75.0
            ),
            TestResult(
                language = "English",
                testType = "PHRASE_TEST",
                timestamp = today.time - 86400000L, // 1 day ago
                scorePercentage = 90.0
            ),
            TestResult(
                language = "English",
                testType = "WORD_TEST",
                timestamp = tenDaysAgo.time, // 10 days ago
                scorePercentage = 80.0
            )
        )

        // Filter for English WORD_TEST results from last 5 days
        calendar.time = today
        calendar.add(Calendar.DAY_OF_MONTH, -5)
        val fromDate = calendar.time

        val filteredResults = testResults.filter { result ->
            val matchesLanguage = result.language.equals("English", ignoreCase = true)
            val matchesTestType = result.testType.equals("WORD_TEST", ignoreCase = true)
            val resultDate = Date(result.timestamp)
            val matchesDateRange = resultDate >= fromDate

            matchesLanguage && matchesTestType && matchesDateRange
        }

        assertEquals("Should find 1 result matching all conditions", 1, filteredResults.size)
        assertEquals("Result should be English WORD_TEST", "WORD_TEST", filteredResults[0].testType)
        assertEquals("Result should be English language", "English", filteredResults[0].language)
    }

    @Test
    fun timeCalculation_testDuration_shouldCalculateCorrectly() {
        // Test time duration calculations
        val testResult = TestResult(
            testDuration = 125000L, // 125 seconds in milliseconds
            startTime = 1640995200000L,
            endTime = 1640995325000L
        )

        val durationInSeconds = testResult.testDuration / 1000
        assertEquals("Duration should be 125 seconds", 125L, durationInSeconds)

        val calculatedDuration = testResult.endTime - testResult.startTime
        assertEquals("Calculated duration should match testDuration", testResult.testDuration, calculatedDuration)

        // Test duration formatting for display
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60
        val formattedTime = "${minutes}m ${seconds}s"

        assertEquals("Should format as 2m 5s", "2m 5s", formattedTime)
    }
}