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
    val score: Double get() = scorePercentage
    val timeSpent: Long get() = testDuration
    val timestampAsDate: Date get() = Date(timestamp)
}

class ExportTestDataActivity : BaseActivity() {

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
    private lateinit var tvTitle: TextView
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
    private lateinit var navLanguage: TextView
    private lateinit var navWords: TextView
    private lateinit var navPhrases: TextView
    private lateinit var navSettings: TextView
    private lateinit var navProfile: TextView
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

    override fun onResume() {
        super.onResume()
        // Refresh UI when language changes
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = prefs.getString("home_language", "English") ?: "English"

        val currentLocale = resources.configuration.locales[0].language
        val expectedLocale = when (currentLanguage) {
            "English" -> "en"
            "isiZulu" -> "zu"
            else -> "en"
        }

        if (currentLocale != expectedLocale) {
            recreate()
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnBack = findViewById(R.id.btn_back)
        btnMenu = findViewById(R.id.btn_menu)
        tvTitle = findViewById(R.id.tv_title)
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
        navLanguage = findViewById(R.id.nav_language)
        navWords = findViewById(R.id.nav_words)
        navPhrases = findViewById(R.id.nav_phrases)
        navSettings = findViewById(R.id.nav_settings)
        navProfile = findViewById(R.id.nav_profile)
        navBack = findViewById(R.id.nav_back)
        navDictionary = findViewById(R.id.nav_dictionary)
        navChat = findViewById(R.id.nav_chat)
    }

    private fun setupUI() {
        // Use string resources for all text
        tvTitle.text = getString(R.string.export_data_title)
        btnDateFrom.text = getString(R.string.btn_date_from)
        btnDateTo.text = getString(R.string.btn_date_to)
        btnPreview.text = getString(R.string.btn_preview_data)
        btnExport.text = getString(R.string.btn_export_pdf)
        btnShare.text = getString(R.string.btn_share_results)

        // Set initial date range text
        tvDateRange.text = getString(R.string.date_range_all_time)

        setupNavigationDrawer()
    }

    private fun setupSpinners() {
        // Language spinner - Use string resources
        val languages = resources.getStringArray(R.array.export_languages)
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = languageAdapter

        // Test type spinner - Use string resources
        val testTypes = resources.getStringArray(R.array.export_test_types)
        val testTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, testTypes)
        testTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTestType.adapter = testTypeAdapter

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
            navigateToHome()
        }

        navLanguage.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToLanguageSelection()
        }

        navWords.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToWords()
        }

        navPhrases.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToPhrases()
        }

        navSettings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToSettings()
        }

        navProfile.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToProfile()
        }

        navBack.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            finish()
        }

        navChat.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToChat()
        }

        navDictionary.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navigateToDictionary()
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

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId == null) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, getString(R.string.error_user_not_authenticated), Toast.LENGTH_LONG).show()
            return
        }

        db.collection("test_results")
            .whereEqualTo("authUserId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("ExportTestData", "Firebase query successful. Document count: ${documents.size()}")
                val results = mutableListOf<TestResult>()

                for (document in documents) {
                    try {
                        val data = document.data
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

                        results.add(result)
                    } catch (e: Exception) {
                        Log.e("ExportTestData", "Error parsing document ${document.id}: ${e.message}", e)
                        continue
                    }
                }

                testResults = results
                filteredResults = results
                updateSummaryStats()
                progressBar.visibility = View.GONE

                if (results.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_test_results_found), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.loaded_test_results, results.size), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ExportTestData", "Firebase query failed: ${exception.message}", exception)
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_loading_data, exception.message), Toast.LENGTH_LONG).show()
            }
    }

    private fun filterAndUpdateData() {
        val allLanguagesText = resources.getStringArray(R.array.export_languages)[0]
        val allTypesText = resources.getStringArray(R.array.export_test_types)[0]

        filteredResults = testResults.filter { result ->
            val matchesLanguage = spinnerLanguage.selectedItem.toString() == allLanguagesText ||
                    result.language.equals(spinnerLanguage.selectedItem.toString(), ignoreCase = true) ||
                    (spinnerLanguage.selectedItem.toString() == "English" && result.language.isEmpty())

            val selectedTestType = spinnerTestType.selectedItem.toString()
            val matchesTestType = selectedTestType == allTypesText ||
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
            fromDate != null -> getString(R.string.date_range_from, dateFormat.format(fromDate!!))
            toDate != null -> getString(R.string.date_range_until, dateFormat.format(toDate!!))
            else -> getString(R.string.date_range_all_time)
        }

        tvDateRange.text = dateRangeText
    }

    private fun previewData() {
        if (filteredResults.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_data_to_preview), Toast.LENGTH_SHORT).show()
            return
        }

        val previewText = generatePreviewText()
        showPreviewDialog(previewText)
    }

    private fun generatePreviewText(): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.preview_title)).append("\n")
        sb.append("=".repeat(40)).append("\n\n")
        sb.append(getString(R.string.export_summary_label)).append("\n")
        sb.append(getString(R.string.total_tests_label, filteredResults.size)).append("\n")

        val avgScore = if (filteredResults.isNotEmpty()) {
            filteredResults.map { it.scorePercentage }.average()
        } else 0.0

        sb.append(getString(R.string.average_score_label, String.format("%.1f", avgScore))).append("\n")
        sb.append(getString(R.string.date_range_label, tvDateRange.text)).append("\n")
        sb.append(getString(R.string.export_format_label)).append("\n\n")

        sb.append(getString(R.string.sample_results_label)).append("\n")
        sb.append("-".repeat(40)).append("\n")

        filteredResults.take(5).forEach { result ->
            sb.append(getString(R.string.result_date_label, timestampFormat.format(Date(result.timestamp)))).append("\n")
            sb.append(getString(R.string.result_language_label, result.language)).append("\n")
            sb.append(getString(R.string.result_test_type_label, result.testType)).append("\n")
            sb.append(getString(R.string.result_score_label, result.scorePercentage, result.correctAnswers, result.totalQuestions)).append("\n")
            sb.append(getString(R.string.result_time_label, result.testDuration / 1000)).append("\n")
            sb.append(getString(R.string.result_category_label, result.category)).append("\n\n")
        }

        return sb.toString()
    }

    private fun showPreviewDialog(previewText: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.data_preview_title))
            .setMessage(previewText)
            .setPositiveButton(getString(R.string.btn_ok)) { dialog, _ -> dialog.dismiss() }
            .setNegativeButton(getString(R.string.btn_export_pdf_short)) { dialog, _ ->
                dialog.dismiss()
                exportToPDF()
            }
            .show()
    }

    private fun exportToPDF() {
        if (filteredResults.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_data_to_export), Toast.LENGTH_SHORT).show()
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
            val title = Paragraph(getString(R.string.pdf_title), titleFont)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)

            // Summary
            val summaryFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
            document.add(Paragraph(getString(R.string.export_summary_label), summaryFont))
            document.add(Paragraph(getString(R.string.total_tests_label, filteredResults.size)))

            val avgScore = if (filteredResults.isNotEmpty()) {
                filteredResults.map { it.scorePercentage }.average()
            } else 0.0

            document.add(Paragraph(getString(R.string.average_score_label, String.format("%.1f", avgScore))))
            document.add(Paragraph(getString(R.string.export_date_label, dateFormat.format(Date()))))
            document.add(Paragraph(getString(R.string.date_range_label, tvDateRange.text)))
            document.add(Paragraph(" "))

            // Results table
            val table = PdfPTable(7)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(15f, 15f, 15f, 10f, 15f, 15f, 15f))

            // Headers
            val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            table.addCell(Phrase(getString(R.string.table_header_date), headerFont))
            table.addCell(Phrase(getString(R.string.table_header_language), headerFont))
            table.addCell(Phrase(getString(R.string.table_header_test_type), headerFont))
            table.addCell(Phrase(getString(R.string.table_header_score), headerFont))
            table.addCell(Phrase(getString(R.string.table_header_questions), headerFont))
            table.addCell(Phrase(getString(R.string.table_header_time), headerFont))
            table.addCell(Phrase(getString(R.string.table_header_category), headerFont))

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
            Toast.makeText(this, getString(R.string.pdf_export_success, fileName), Toast.LENGTH_LONG).show()

            openFile(file)

        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, getString(R.string.error_exporting_pdf, e.message), Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, getString(R.string.error_no_pdf_app), Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareResults() {
        if (filteredResults.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_data_to_share), Toast.LENGTH_SHORT).show()
            return
        }

        val shareText = generateShareText()

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)))
    }

    private fun generateShareText(): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.share_summary_title)).append("\n\n")
        sb.append(getString(R.string.export_date_label, dateFormat.format(Date()))).append("\n")
        sb.append(getString(R.string.total_tests_label, filteredResults.size)).append("\n")

        val avgScore = if (filteredResults.isNotEmpty()) {
            filteredResults.map { it.scorePercentage }.average()
        } else 0.0

        sb.append(getString(R.string.average_score_label, String.format("%.1f", avgScore))).append("\n")
        sb.append(getString(R.string.date_range_label, tvDateRange.text)).append("\n\n")

        val topResults = filteredResults.sortedByDescending { it.scorePercentage }.take(3)
        if (topResults.isNotEmpty()) {
            sb.append(getString(R.string.top_performances_label)).append("\n")
            topResults.forEachIndexed { index, result ->
                sb.append("${index + 1}. ${result.scorePercentage}% - ${result.category} (${result.testType})\n")
            }
        }

        sb.append("\n").append(getString(R.string.generated_by_app))

        return sb.toString()
    }

    private fun setupNavigationDrawer() {
        // Navigation drawer text uses string resources
        navHome.text = getString(R.string.nav_home)
        navLanguage.text = getString(R.string.nav_language)
        navWords.text = getString(R.string.nav_words)
        navPhrases.text = getString(R.string.nav_phrases)
        navSettings.text = getString(R.string.nav_settings)
        navProfile.text = getString(R.string.nav_profile)
    }

    // Navigation methods
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLanguageSelection() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToWords() {
        val intent = Intent(this, WordsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToPhrases() {
        val intent = Intent(this, PhrasesActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDictionary() {
        val intent = Intent(this, OfflineActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToChat() {
        val intent = Intent(this, AiChatActivity::class.java)
        startActivity(intent)
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