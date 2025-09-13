package com.fake.mzansilingo

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.ConnectivityManager
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class AiChatActivity : AppCompatActivity() {

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

    private fun initializeViews() {
        chatContainer = findViewById(R.id.chatContainer)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        scrollView = findViewById(R.id.scrollView)
        backButton = findViewById(R.id.btn_back)
    }

    private fun addWelcomeMessage() {
        val welcomeText = """
            🦏 Hello! I am your Mzansi Lingo Afrikaans Assistant!

            🗣️ You can ask me in English:
            • "How do I say hello in Afrikaans?"
            • "What is thank you in Afrikaans?"
            • "How do I say numbers in Afrikaans?"
            
            💡 All my answers will be in Afrikaans.
        """.trimIndent()

        addMessageToChat(welcomeText, isUser = false)
    }

    private fun testApiConnection() {
        lifecycleScope.launch {
            val result = ApiKeyManager.testConnection()
            withContext(Dispatchers.Main) {
                result.onSuccess { message ->
                    addMessageToChat("✅ $message", isUser = false)
                }.onFailure { error ->
                    addMessageToChat("⚠️ API connection failed: ${error.message}", isUser = false)
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
            addMessageToChat("⚠️ No internet connection available.", isUser = false)
            scrollToBottom()
            return
        }

        val loadingMessage = addMessageToChat("🦏 Thinking...", isUser = false)

        lifecycleScope.launch {
            try {
                val result = ApiKeyManager.sendChatMessage(message)

                withContext(Dispatchers.Main) {
                    chatContainer.removeView(loadingMessage)

                    result.onSuccess { response ->
                        addMessageToChat(response, isUser = false)
                    }.onFailure { error ->
                        addMessageToChat("⚠️ Error: ${error.message}", isUser = false)
                    }
                    scrollToBottom()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    chatContainer.removeView(loadingMessage)
                    addMessageToChat("⚠️ Network error: ${e.message}", isUser = false)
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