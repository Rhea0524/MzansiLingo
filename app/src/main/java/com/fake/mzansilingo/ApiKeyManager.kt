import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log
import java.util.concurrent.TimeUnit
import com.google.gson.Gson
import com.google.gson.GsonBuilder


data class ChatRequest(
    val message: String,
    val userId: String = "user_${System.currentTimeMillis()}",
    val language: String = "afrikaans"
)

data class ChatResponse(
    val response: String,
    val messageId: Long,
    val userId: String,
    val timestamp: String,
    val success: Boolean = true // Add success flag
)

data class TranslationResponse(
    val originalText: String,
    val translatedText: String,
    val fromLanguage: String,
    val toLanguage: String,
    val success: Boolean
)

data class HealthResponse(
    val status: String,
    val timestamp: String,
    val languages: List<String>
)

// Add error response data class
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String
)

// API interface
interface MzansiLingoApiService {
    @GET("api/health")
    suspend fun getHealthStatus(): Response<HealthResponse>

    @POST("api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

    @GET("api/translate")
    suspend fun translateText(
        @Query("text") text: String,
        @Query("to") toLanguage: String,
        @Query("from") fromLanguage: String? = null
    ): Response<TranslationResponse>
}

class ApiKeyManager {
    companion object {
        private const val MZANSI_API_URL = "https://mzansilingo-production.up.railway.app/"
        private const val TAG = "MzansiLingoAPI"

        @Volatile
        private var apiService: MzansiLingoApiService? = null
        private val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        fun getCohereKey(): String = ""

        fun getApiService(): MzansiLingoApiService {
            return apiService ?: synchronized(this) {
                apiService ?: createApiService().also { apiService = it }
            }
        }

        private fun createApiService(): MzansiLingoApiService {
            val logging = HttpLoggingInterceptor { message ->
                Log.d(TAG, "HTTP: $message")
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(MZANSI_API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(MzansiLingoApiService::class.java)
        }

        // Enhanced chat message method with better debugging
        suspend fun sendChatMessage(message: String): Result<String> {
            return try {
                Log.d(TAG, "=== SENDING CHAT MESSAGE ===")
                Log.d(TAG, "Input message: '$message'")

                val apiService = getApiService()
                val request = ChatRequest(
                    message = message.trim(),
                    language = "afrikaans" // Make sure we specify Afrikaans
                )

                Log.d(TAG, "Request payload: ${gson.toJson(request)}")

                val response = apiService.sendMessage(request)

                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response headers: ${response.headers()}")

                if (response.isSuccessful) {
                    response.body()?.let { chatResponse ->
                        Log.d(TAG, "Success response body: ${gson.toJson(chatResponse)}")
                        Log.d(TAG, "Bot response: '${chatResponse.response}'")

                        // Check if response is meaningful or just generic
                        if (chatResponse.response.contains("Probeer vra") ||
                            chatResponse.response.contains("Afrikaanse vertalings")) {
                            Log.w(TAG, "⚠️ Detected generic/repetitive response!")
                        }

                        Result.success(chatResponse.response)
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error - Code: ${response.code()}")
                    Log.e(TAG, "API Error - Message: ${response.message()}")
                    Log.e(TAG, "API Error - Body: $errorBody")

                    // Try to parse error response
                    val errorMsg = try {
                        errorBody?.let {
                            val errorResponse = gson.fromJson(it, ErrorResponse::class.java)
                            "API Error: ${errorResponse.message}"
                        } ?: "API Error: ${response.code()} ${response.message()}"
                    } catch (e: Exception) {
                        "API Error: ${response.code()} ${response.message()}"
                    }

                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network/Parsing error: ${e.message}", e)
                Result.failure(e)
            }
        }


        suspend fun translateText(text: String, targetLanguage: String): Result<String> {
            return try {
                Log.d(TAG, "=== TRANSLATING TEXT ===")
                Log.d(TAG, "Text: '$text' -> $targetLanguage")

                val apiService = getApiService()
                val response = apiService.translateText(text.trim(), targetLanguage)

                Log.d(TAG, "Translation response code: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { translationResponse ->
                        Log.d(TAG, "Translation result: ${gson.toJson(translationResponse)}")
                        Result.success(translationResponse.translatedText)
                    } ?: Result.failure(Exception("Empty translation response"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Translation Error: ${response.code()} - $errorBody")
                    Result.failure(Exception("Translation Error: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Translation error", e)
                Result.failure(e)
            }
        }


        suspend fun testConnection(): Result<String> {
            return try {
                Log.d(TAG, "=== TESTING API CONNECTION ===")
                val apiService = getApiService()
                val response = apiService.getHealthStatus()

                Log.d(TAG, "Health check response code: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { health ->
                        Log.d(TAG, "Health response: ${gson.toJson(health)}")
                        Result.success("API is working! Status: ${health.status}, Languages: ${health.languages}")
                    } ?: Result.failure(Exception("Empty health response"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Health check failed: ${response.code()} - $errorBody")
                    Result.failure(Exception("Health check failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Health check error", e)
                Result.failure(e)
            }
        }

        // Method to test with different question types
        suspend fun debugQuestionTypes(): Result<String> {
            val testQuestions = listOf(
                "How do I say hello in Afrikaans?",
                "What are colors in Afrikaans?",
                "Tell me numbers in Afrikaans",
                "How do I greet someone in Afrikaans?"
            )

            var debugResults = "=== DEBUG QUESTION TESTS ===\n"

            for ((index, question) in testQuestions.withIndex()) {
                Log.d(TAG, "Testing question ${index + 1}: $question")
                val result = sendChatMessage(question)
                result.onSuccess { response ->
                    debugResults += "Q${index + 1}: $question\nA${index + 1}: $response\n\n"
                }.onFailure { error ->
                    debugResults += "Q${index + 1}: $question\nERROR: ${error.message}\n\n"
                }
            }

            Log.d(TAG, debugResults)
            return Result.success(debugResults)
        }
    }
}