package com.fake.mzansilingo

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class TestResult(
    val authUserId: String = "",
    val testType: String = "",
    val language: String = "",
    val scorePercentage: Double = 0.0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val testDuration: Long = 0,
    val timestamp: Long = 0L,
    val difficulty: String = "",
    val category: String = "",
    val testId: String = "",
    val userId: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L
) {
    // Computed properties for backwards compatibility and convenience
    val score: Double get() = scorePercentage
    val timeSpent: Long get() = testDuration
    val timestampAsDate: Date get() = Date(timestamp)
}

class ExportTestDataActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var testResults: List<TestResult> = listOf()
    private var filteredResults: List<TestResult> = listOf()
    private var fromDate: Date? = null
    private var toDate: Date? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // View references
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnBack: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var btnDateFrom: AppCompatButton
    private lateinit var btnDateTo: AppCompatButton
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerTestType: Spinner
    private lateinit var btnPreview: AppCompatButton
    private lateinit var btnExport: AppCompatButton
    private lateinit var btnShare: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotalTests: TextView
    private lateinit var tvAverageScore: TextView
    private lateinit var tvDateRange: TextView
    private lateinit var navHome: TextView
    private lateinit var navBack: ImageView
    private lateinit var navDictionary: ImageView
    private lateinit var navChat: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_test_data)

        db = FirebaseFirestore.getInstance()

        initViews()
        setupUI()
        setupSpinners()
        setupClickListeners()
        loadTestData()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnBack = findViewById(R.id.btn_back)
        btnMenu = findViewById(R.id.btn_menu)
        btnDateFrom = findViewById(R.id.btn_date_from)
        btnDateTo = findViewById(R.id.btn_date_to)
        spinnerLanguage = findViewById(R.id.spinner_language)
        spinnerTestType = findViewById(R.id.spinner_test_type)
        btnPreview = findViewById(R.id.btn_preview)
        btnExport = findViewById(R.id.btn_export)
        btnShare = findViewById(R.id.btn_share)
        progressBar = findViewById(R.id.progress_bar)
        tvTotalTests = findViewById(R.id.tv_total_tests)
        tvAverageScore = findViewById(R.id.tv_average_score)
        tvDateRange = findViewById(R.id.tv_date_range)
        navHome = findViewById(R.id.nav_home)
        navBack = findViewById(R.id.nav_back)
        navDictionary = findViewById(R.id.nav_dictionary)
        navChat = findViewById(R.id.nav_chat)
    }

    private fun setupUI() {
        setupNavigationDrawer()
    }

    private fun setupSpinners() {
        // Language spinner
        val languages = arrayOf("All Languages", "English", "Afrikaans", "Zulu", "Xhosa", "Sotho")
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = languageAdapter

        // Test type spinner - updated to match your actual data
        val testTypes = arrayOf("All Types", "WORD_TEST", "PHRASE_TEST", "Mixed", "Vocabulary", "Grammar")
        val testTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, testTypes)
        testTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTestType.adapter = testTypeAdapter

        // Add listeners to spinners for filtering
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterAndUpdateData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerTestType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterAndUpdateData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }

        btnDateFrom.setOnClickListener { showDatePicker(true) }
        btnDateTo.setOnClickListener { showDatePicker(false) }

        btnPreview.setOnClickListener { previewData() }
        btnExport.setOnClickListener { exportToPDF() }
        btnShare.setOnClickListener { shareResults() }

        // Navigation drawer clicks
        navHome.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            try {
                val intent = Intent(this, Class.forName("${packageName}.HomeActivity"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "Home activity not found", Toast.LENGTH_SHORT).show()
            }
        }

        navBack.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            finish()
        }
    }

    private fun showDatePicker(isFromDate: Boolean) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = calendar.time

                if (isFromDate) {
                    fromDate = selectedDate
                    btnDateFrom.text = dateFormat.format(selectedDate)
                } else {
                    toDate = selectedDate
                    btnDateTo.text = dateFormat.format(selectedDate)
                }

                filterAndUpdateData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun loadTestData() {
        progressBar.visibility = View.VISIBLE
        Log.d("ExportTestData", "Starting to load test data...")

        // Get current user's ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId == null) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        // Filter by current user's authUserId
        db.collection("test_results")
            .whereEqualTo("authUserId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("ExportTestData", "Firebase query successful. Document count: ${documents.size()}")
                val results = mutableListOf<TestResult>()

                for (document in documents) {
                    try {
                        Log.d("ExportTestData", "Processing document: ${document.id}")
                        val data = document.data
                        Log.d("ExportTestData", "Document data: $data")

                        // Manual parsing to handle your specific Firebase structure
                        val result = TestResult(
                            authUserId = data["authUserId"] as? String ?: "",
                            testType = data["testType"] as? String ?: "",
                            language = data["language"] as? String ?: "Unknown",
                            scorePercentage = when (val scoreValue = data["scorePercentage"]) {
                                is Number -> scoreValue.toDouble()
                                is String -> scoreValue.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            },
                            totalQuestions = when (val totalValue = data["totalQuestions"]) {
                                is Number -> totalValue.toInt()
                                is String -> totalValue.toIntOrNull() ?: 0
                                else -> 0
                            },
                            correctAnswers = when (val correctValue = data["correctAnswers"]) {
                                is Number -> correctValue.toInt()
                                is String -> correctValue.toIntOrNull() ?: 0
                                else -> 0
                            },
                            incorrectAnswers = when (val incorrectValue = data["incorrectAnswers"]) {
                                is Number -> incorrectValue.toInt()
                                is String -> incorrectValue.toIntOrNull() ?: 0
                                else -> 0
                            },
                            testDuration = when (val durationValue = data["testDuration"]) {
                                is Number -> durationValue.toLong()
                                is String -> durationValue.toLongOrNull() ?: 0L
                                else -> 0L
                            },
                            timestamp = when (val timestampValue = data["timestamp"]) {
                                is Number -> timestampValue.toLong()
                                is String -> timestampValue.toLongOrNull() ?: 0L
                                else -> 0L
                            },
                            difficulty = data["difficulty"] as? String ?: "Unknown",
                            category = data["category"] as? String ?: "",
                            testId = data["testId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            startTime = when (val startValue = data["startTime"]) {
                                is Number -> startValue.toLong()
                                else -> 0L
                            },
                            endTime = when (val endValue = data["endTime"]) {
                                is Number -> endValue.toLong()
                                else -> 0L
                            }
                        )

                        Log.d("ExportTestData", "Parsed result: $result")
                        results.add(result)
                    } catch (e: Exception) {
                        Log.e("ExportTestData", "Error parsing document ${document.id}: ${e.message}", e)
                        continue
                    }
                }

                testResults = results
                filteredResults = results
                Log.d("ExportTestData", "Final results count: ${results.size}")
                updateSummaryStats()
                progressBar.visibility = View.GONE

                if (results.isEmpty()) {
                    Toast.makeText(this, "No test results found for current user", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Loaded ${results.size} test results for current user", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ExportTestData", "Firebase query failed: ${exception.message}", exception)
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterAndUpdateData() {
        filteredResults = testResults.filter { result ->
            val matchesLanguage = spinnerLanguage.selectedItem.toString() == "All Languages" ||
                    result.language.equals(spinnerLanguage.selectedItem.toString(), ignoreCase = true) ||
                    (spinnerLanguage.selectedItem.toString() == "English" && result.language.isEmpty())

            val selectedTestType = spinnerTestType.selectedItem.toString()
            val matchesTestType = selectedTestType == "All Types" ||
                    result.testType.equals(selectedTestType, ignoreCase = true)

            val resultDate = Date(result.timestamp)
            val matchesDateRange = (fromDate == null || resultDate >= fromDate) &&
                    (toDate == null || resultDate <= toDate)

            matchesLanguage && matchesTestType && matchesDateRange
        }

        updateSummaryStats()
    }

    private fun updateSummaryStats() {
        tvTotalTests.text = filteredResults.size.toString()

        val averageScore = if (filteredResults.isNotEmpty()) {
            filteredResults.map { it.scorePercentage }.average()
        } else 0.0

        tvAverageScore.text = "${String.format("%.1f", averageScore)}%"

        val dateRangeText = when {
            fromDate != null && toDate != null -> "${dateFormat.format(fromDate!!)} - ${dateFormat.format(toDate!!)}"
            fromDate != null -> "From ${dateFormat.format(fromDate!!)}"
            toDate != null -> "Until ${dateFormat.format(toDate!!)}"
            else -> "All Time"
        }

        tvDateRange.text = dateRangeText
    }

    private fun previewData() {
        if (filteredResults.isEmpty()) {
            Toast.makeText(this, "No data to preview", Toast.LENGTH_SHORT).show()
            return
        }

        // Show preview in a dialog
        val previewText = generatePreviewText()
        showPreviewDialog(previewText)
    }

    private fun generatePreviewText(): String {
        val sb = StringBuilder()
        sb.append("TEST RESULTS EXPORT PREVIEW\n")
        sb.append("=".repeat(40)).append("\n\n")
        sb.append("Export Summary:\n")
        sb.append("Total Tests: ${filteredResults.size}\n")

        val avgScore = if (filteredResults.isNotEmpty()) {
            filteredResults.map { it.scorePercentage }.average()
        } else 0.0

        sb.append("Average Score: ${String.format("%.1f", avgScore)}%\n")
        sb.append("Date Range: ${tvDateRange.text}\n")
        sb.append("Export Format: PDF\n\n")

        sb.append("Sample Results (First 5):\n")
        sb.append("-".repeat(40)).append("\n")

        filteredResults.take(5).forEach { result ->
            sb.append("Date: ${timestampFormat.format(Date(result.timestamp))}\n")
            sb.append("Language: ${result.language}\n")
            sb.append("Test Type: ${result.testType}\n")
            sb.append("Score: ${result.scorePercentage}% (${result.correctAnswers}/${result.totalQuestions})\n")
            sb.append("Time: ${result.testDuration / 1000}s\n")
            sb.append("Category: ${result.category}\n")
            sb.append("\n")
        }

        return sb.toString()
    }

    private fun showPreviewDialog(previewText: String) {
        AlertDialog.Builder(this)
            .setTitle("Data Preview")
            .setMessage(previewText)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Export PDF") { dialog, _ ->
                dialog.dismiss()
                exportToPDF()
            }
            .show()
    }

    private fun exportToPDF() {
        if (filteredResults.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        try {
            val fileName = "test_results_${System.currentTimeMillis()}.pdf"
            val file = File(getExternalFilesDir(null), fileName)

            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(file))

            document.open()

            // Title
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor.BLUE)
            val title = Paragraph("TEST RESULTS EXPORT REPORT", titleFont)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)

            // Summary
            val summaryFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
            document.add(Paragraph("EXPORT SUMMARY", summaryFont))
            document.add(Paragraph("Total Tests: ${filteredResults.size}"))

            val avgScore = if (filteredResults.isNotEmpty()) {
                filteredResults.map { it.scorePercentage }.average()
            } else 0.0

            document.add(Paragraph("Average Score: ${String.format("%.1f", avgScore)}%"))
            document.add(Paragraph("Export Date: ${dateFormat.format(Date())}"))
            document.add(Paragraph("Date Range: ${tvDateRange.text}"))
            document.add(Paragraph(" "))

            // Results table
            val table = PdfPTable(7)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(15f, 15f, 15f, 10f, 15f, 15f, 15f))

            // Headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            table.addCell(Phrase("Date", headerFont))
            table.addCell(Phrase("Language", headerFont))
            table.addCell(Phrase("Test Type", headerFont))
            table.addCell(Phrase("Score", headerFont))
            table.addCell(Phrase("Questions", headerFont))
            table.addCell(Phrase("Time (s)", headerFont))
            table.addCell(Phrase("Category", headerFont))

            // Data rows
            val dataFont = Font(Font.FontFamily.HELVETICA, 9f)
            filteredResults.forEach { result ->
                table.addCell(Phrase(timestampFormat.format(Date(result.timestamp)), dataFont))
                table.addCell(Phrase(result.language, dataFont))
                table.addCell(Phrase(result.testType, dataFont))
                table.addCell(Phrase("${result.scorePercentage}%", dataFont))
                table.addCell(Phrase("${result.correctAnswers}/${result.totalQuestions}", dataFont))
                table.addCell(Phrase("${result.testDuration / 1000}", dataFont))
                table.addCell(Phrase(result.category, dataFont))
            }

            document.add(table)
            document.close()

            progressBar.visibility = View.GONE
            Toast.makeText(this, "PDF exported successfully: $fileName", Toast.LENGTH_LONG).show()

            // Open the PDF
            openFile(file)

        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Error exporting PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openFile(file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open PDF files", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareResults() {
        if (filteredResults.isEmpty()) {
            Toast.makeText(this, "No data to share", Toast.LENGTH_SHORT).show()
            return
        }

        val shareText = generateShareText()

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Test Results Export")
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(shareIntent, "Share Results"))
    }

    private fun generateShareText(): String {
        val sb = StringBuilder()
        sb.append("TEST RESULTS SUMMARY\n\n")
        sb.append("Export Date: ${dateFormat.format(Date())}\n")
        sb.append("Total Tests: ${filteredResults.size}\n")

        val avgScore = if (filteredResults.isNotEmpty()) {
            filteredResults.map { it.scorePercentage }.average()
        } else 0.0

        sb.append("Average Score: ${String.format("%.1f", avgScore)}%\n")
        sb.append("Date Range: ${tvDateRange.text}\n\n")

        val topResults = filteredResults.sortedByDescending { it.scorePercentage }.take(3)
        if (topResults.isNotEmpty()) {
            sb.append("TOP PERFORMANCES:\n")
            topResults.forEachIndexed { index, result ->
                sb.append("${index + 1}. ${result.scorePercentage}% - ${result.category} (${result.testType})\n")
            }
        }

        sb.append("\nGenerated by Mzansi Lingo App")

        return sb.toString()
    }

    private fun setupNavigationDrawer() {
        // Handle drawer navigation items
        navHome.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            try {
                val intent = Intent(this, Class.forName("${packageName}.HomeActivity"))
                startActivity(intent)
                finish()
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "Home activity not found", Toast.LENGTH_SHORT).show()
            }
        }

        navBack.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            finish()
        }

        navDictionary.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            // Start dictionary activity
            try {
                val intent = Intent(this, Class.forName("${packageName}.DictionaryActivity"))
                startActivity(intent)
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "Dictionary activity not found", Toast.LENGTH_SHORT).show()
            }
        }

        navChat.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            // Start AI chat activity
            try {
                val intent = Intent(this, Class.forName("${packageName}.AiChatActivity"))
                startActivity(intent)
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "Chat activity not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}