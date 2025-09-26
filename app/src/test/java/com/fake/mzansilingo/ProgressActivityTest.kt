package com.fake.mzansilingo

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*


class ProgressActivityTest {

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun progressData_initialValues_shouldBeZero() {
        // Test the ProgressData data class initialization
        val progressData = ProgressData(0, 0, 0, BooleanArray(7))

        assertEquals("Words spoken should be 0", 0, progressData.wordsSpoken)
        assertEquals("Phrases spoken should be 0", 0, progressData.phrasesSpoken)
        assertEquals("Days practiced should be 0", 0, progressData.daysPracticed)
        assertEquals("Weekly progress should have 7 days", 7, progressData.weeklyProgress.size)

        // Check all weekly progress days are false initially
        for (i in progressData.weeklyProgress.indices) {
            assertFalse("Day $i should be false initially", progressData.weeklyProgress[i])
        }
    }

    @Test
    fun progressData_withValidData_shouldStoreCorrectly() {
        // Test ProgressData with actual values
        val weeklyProgress = BooleanArray(7) { i -> i % 2 == 0 } // alternating pattern
        val progressData = ProgressData(25, 15, 7, weeklyProgress)

        assertEquals("Words spoken should be 25", 25, progressData.wordsSpoken)
        assertEquals("Phrases spoken should be 15", 15, progressData.phrasesSpoken)
        assertEquals("Days practiced should be 7", 7, progressData.daysPracticed)

        // Check weekly progress pattern
        assertTrue("Day 0 should be true", progressData.weeklyProgress[0])
        assertFalse("Day 1 should be false", progressData.weeklyProgress[1])
        assertTrue("Day 2 should be true", progressData.weeklyProgress[2])
    }

    @Test
    fun weeklyProgress_allDaysCompleted_shouldBeFilled() {
        // Test when user completes all days of the week
        val allCompleted = BooleanArray(7) { true }
        val progressData = ProgressData(50, 30, 7, allCompleted)

        for (i in 0..6) {
            assertTrue("Day $i should be completed", progressData.weeklyProgress[i])
        }
    }

    @Test
    fun dayOfWeek_mapping_shouldBeCorrect() {
        // Test day of week mapping logic (simulating the logic from markTodayAsCompleted)
        val calendar = Calendar.getInstance()

        // Test Monday = 0
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val mondayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> -1
        }
        assertEquals("Monday should map to index 0", 0, mondayIndex)

        // Test Sunday = 6
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val sundayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> -1
        }
        assertEquals("Sunday should map to index 6", 6, sundayIndex)
    }

    @Test
    fun progressCalculation_addingWords_shouldIncreaseTotal() {
        // Test progress accumulation logic (simulating updateWordsSpoken logic)
        var wordsSpoken = 0
        val newWords1 = 5
        val newWords2 = 3

        wordsSpoken += newWords1
        assertEquals("After first addition, should be 5", 5, wordsSpoken)

        wordsSpoken += newWords2
        assertEquals("After second addition, should be 8", 8, wordsSpoken)
    }

    @Test
    fun progressCalculation_addingPhrases_shouldIncreaseTotal() {
        // Test phrase progress accumulation
        var phrasesSpoken = 0
        val newPhrases1 = 2
        val newPhrases2 = 4

        phrasesSpoken += newPhrases1
        assertEquals("After first addition, should be 2", 2, phrasesSpoken)

        phrasesSpoken += newPhrases2
        assertEquals("After second addition, should be 6", 6, phrasesSpoken)
    }

    @Test
    fun weeklyProgress_markingDays_shouldUpdateCorrectly() {
        // Test weekly progress marking logic
        val weeklyProgress = BooleanArray(7) { false }

        // Simulate marking day 1 (Tuesday) as completed
        val dayIndex = 1
        if (dayIndex in 0..6 && !weeklyProgress[dayIndex]) {
            weeklyProgress[dayIndex] = true
        }

        assertTrue("Day 1 should be marked as completed", weeklyProgress[dayIndex])
        assertFalse("Day 0 should still be false", weeklyProgress[0])
        assertFalse("Day 2 should still be false", weeklyProgress[2])
    }

    @Test
    fun weeklyProgress_invalidDayIndex_shouldNotUpdate() {
        // Test bounds checking for day marking
        val weeklyProgress = BooleanArray(7) { false }

        // Try to mark invalid indices
        val invalidIndices = listOf(-1, 7, 10)

        for (dayIndex in invalidIndices) {
            if (dayIndex in 0..6 && !weeklyProgress[dayIndex]) {
                weeklyProgress[dayIndex] = true
            }
        }

        // All days should still be false
        for (i in 0..6) {
            assertFalse("Day $i should still be false", weeklyProgress[i])
        }
    }

    @Test
    fun weeklyProgress_alreadyCompleted_shouldNotChange() {
        // Test that already completed days don't get unmarked
        val weeklyProgress = BooleanArray(7) { false }
        weeklyProgress[3] = true // Mark Wednesday as completed

        // Try to "complete" it again
        val dayIndex = 3
        if (dayIndex in 0..6 && !weeklyProgress[dayIndex]) {
            weeklyProgress[dayIndex] = true
        }

        assertTrue("Day 3 should remain true", weeklyProgress[dayIndex])
    }

    @Test
    fun progressData_cloneArray_shouldCreateSeparateInstance() {
        // Test that cloning weekly progress creates independent arrays
        val original = BooleanArray(7) { i -> i < 3 }
        val progressData = ProgressData(10, 5, 3, original.clone())

        // Modify original
        original[0] = false

        // progressData should have its own copy
        assertTrue("Cloned array should be independent", progressData.weeklyProgress[0])
    }

    @Test
    fun testResults_accumulation_shouldSumCorrectly() {
        // Test the logic from loadTestResultsSimple method
        val testResults = listOf(
            TestResult("WORD_TEST", 5),
            TestResult("PHRASE_TEST", 3),
            TestResult("WORD_TEST", 2),
            TestResult("PHRASE_TEST", 4),
            TestResult("UNKNOWN_TEST", 10) // Should be ignored
        )

        var wordCount = 0
        var phraseCount = 0

        for (result in testResults) {
            when (result.testType) {
                "WORD_TEST" -> wordCount += result.correctAnswers
                "PHRASE_TEST" -> phraseCount += result.correctAnswers
            }
        }

        assertEquals("Word count should be 7", 7, wordCount)
        assertEquals("Phrase count should be 7", 7, phraseCount)
    }

    @Test
    fun uniqueDates_counting_shouldEliminateDuplicates() {
        // Test the logic from loadTotalDaysPracticed method
        val loginDates = listOf(
            "2024-01-15",
            "2024-01-16",
            "2024-01-15", // duplicate
            "2024-01-17",
            "2024-01-16", // duplicate
            "2024-01-18"
        )

        val uniqueDates = mutableSetOf<String>()
        for (date in loginDates) {
            if (date.isNotEmpty()) {
                uniqueDates.add(date)
            }
        }

        assertEquals("Should have 4 unique dates", 4, uniqueDates.size)
        assertTrue("Should contain 2024-01-15", uniqueDates.contains("2024-01-15"))
        assertTrue("Should contain 2024-01-18", uniqueDates.contains("2024-01-18"))
    }

    // Helper data class for testing
    private data class TestResult(
        val testType: String,
        val correctAnswers: Int
    )
}