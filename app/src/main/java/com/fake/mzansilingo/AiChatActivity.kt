package com.fake.mzansilingo

import android.os.Bundle
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.ConnectivityManager
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class AiChatActivity : BaseActivity() {  // Changed from AppCompatActivity to BaseActivity

    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var scrollView: ScrollView
    private lateinit var backButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        initializeViews()
        setupClickListeners()
        addWelcomeMessage()
        testApiConnection()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the activity if language changed
        val prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = prefs.getString("home_language", "English") ?: "English"

        // Check if the locale matches the saved language
        val currentLocale = resources.configuration.locales[0].language
        val expectedLocale = when (currentLanguage) {
            "English" -> "en"
            "isiZulu" -> "zu"
            else -> "en"
        }

        if (currentLocale != expectedLocale) {
            recreate() // Recreate activity to apply new language
        }
    }

    private fun initializeViews() {
        chatContainer = findViewById(R.id.chatContainer)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        scrollView = findViewById(R.id.scrollView)
        backButton = findViewById(R.id.btn_back)
    }

    private fun addWelcomeMessage() {
        val welcomeText = getString(R.string.ai_chat_welcome_message)
        addMessageToChat(welcomeText, isUser = false)
    }

    private fun testApiConnection() {
        lifecycleScope.launch {
            val result = ApiKeyManager.testConnection()
            withContext(Dispatchers.Main) {
                result.onSuccess { message ->
                    addMessageToChat("✅ $message", isUser = false)
                }.onFailure { error ->
                    addMessageToChat("${getString(R.string.api_connection_failed)} ${error.message}", isUser = false)
                }
                scrollToBottom()
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) sendUserMessage(message)
        }

        messageInput.addTextChangedListener {
            val hasText = it.toString().trim().isNotEmpty()
            sendButton.alpha = if (hasText) 1.0f else 0.5f
            sendButton.isEnabled = hasText
        }

        messageInput.setOnEditorActionListener { _, _, _ ->
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) { sendUserMessage(message); true } else false
        }

        sendButton.alpha = 0.5f
        sendButton.isEnabled = false
    }

    private fun sendUserMessage(message: String) {
        addMessageToChat(message, isUser = true)
        messageInput.text.clear()

        if (!isNetworkAvailable()) {
            addMessageToChat(getString(R.string.no_internet_connection), isUser = false)
            scrollToBottom()
            return
        }

        val loadingMessage = addMessageToChat(getString(R.string.ai_thinking), isUser = false)

        lifecycleScope.launch {
            try {
                val result = ApiKeyManager.sendChatMessage(message)

                withContext(Dispatchers.Main) {
                    chatContainer.removeView(loadingMessage)

                    result.onSuccess { response ->
                        addMessageToChat(response, isUser = false)
                    }.onFailure { error ->
                        addMessageToChat("${getString(R.string.error_prefix)} ${error.message}", isUser = false)
                    }
                    scrollToBottom()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    chatContainer.removeView(loadingMessage)
                    addMessageToChat("${getString(R.string.network_error)} ${e.message}", isUser = false)
                    scrollToBottom()
                }
            }
        }
    }

    private fun addMessageToChat(message: String, isUser: Boolean): CardView {
        val cardView = CardView(this).apply {
            radius = 16f
            cardElevation = 2f
            useCompatPadding = true
        }

        val textView = TextView(this).apply {
            text = if (isUser) message else "🤖 $message"
            textSize = 16f
            setPadding(20, 16, 20, 16)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(if (isUser) 64 else 0, 0, if (isUser) 0 else 64, 16)
            gravity = if (isUser) android.view.Gravity.END else android.view.Gravity.START
        }

        cardView.layoutParams = params
        cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isUser) android.R.color.holo_blue_light else R.color.mz_accent_green
            )
        )
        textView.setTextColor(
            ContextCompat.getColor(
                this,
                if (isUser) android.R.color.white else R.color.mz_navy_dark
            )
        )

        cardView.addView(textView)
        chatContainer.addView(cardView)
        return cardView
    }

    private fun scrollToBottom() {
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}