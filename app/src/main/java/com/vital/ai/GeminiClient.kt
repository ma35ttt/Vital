package com.vital.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiClient(private val client: OkHttpClient) {

    private val gson = Gson()

    // Gemini 1.5 was retired; 3.x is the current generation. Try newest first so a key
    // that cannot see the newest model still gets an answer from an older one.
    private val models = listOf(
        "gemini-3.8-flash",
        "gemini-3.5-flash",
        "gemini-3-flash-preview",
        "gemini-3.1-flash-lite"
    )
    private fun generateUrl(model: String) =
        "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"

    data class FoodAnalysisResult(
        val foodName: String,
        val confidence: Double,
        val proteinPer100: Double,
        val carbsPer100: Double,
        val fatPer100: Double,
        val kcalPer100: Double,
        val suggestedGrams: Double,
        val variant: String? = null
    )

    data class CoachResponse(
        val message: String,
        val done: Boolean = true
    )

    suspend fun analyzeFood(imageBase64: String, context: String?, apiKey: String): FoodAnalysisResult? = withContext(Dispatchers.IO) {
        val prompt = buildFoodPrompt(context)
        val requestBody = buildRequest(prompt, imageBase64)
        val request = Request.Builder()
            .url(generateUrl(models.first()))
            .addHeader("x-goog-api-key", apiKey)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            parseFoodResponse(body)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun chat(
        messages: List<ChatMessage>,
        apiKey: String,
        imageBase64: String? = null
    ): AiChatResult = withContext(Dispatchers.IO) {
        val prompt = buildChatPrompt(messages)
        val requestBody = buildTextRequest(prompt, imageBase64)
        var lastError = "Request failed"

        for (model in models) {
            val request = Request.Builder()
                .url(generateUrl(model))
                .addHeader("x-goog-api-key", apiKey)
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val parsed = parseChatResponse(body)
                        if (parsed != null) return@withContext AiChatResult.Ok(parsed)
                        lastError = "Empty response from $model"
                    } else {
                        lastError = parseError(body) ?: "HTTP ${response.code} from $model"
                        // Retry a missing/retired model, a transient spike, or a per-model
                        // quota. A bad key (400) or blocked API (403) is the same everywhere.
                        val retryable = response.code == 404 ||
                            response.code == 429 ||
                            response.code == 503
                        if (!retryable) return@withContext AiChatResult.Failure(lastError)
                    }
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Network error"
            }
        }
        AiChatResult.Failure(lastError)
    }

    private fun parseError(body: String): String? = try {
        JsonParser.parseString(body).asJsonObject
            .getAsJsonObject("error")
            .get("message")?.asString
    } catch (e: Exception) {
        null
    }

    private fun buildFoodPrompt(context: String?): String {
        return """Analyze this food image and return ONLY a JSON object with:
{
  "foodName": "name of the food",
  "confidence": 0.0-1.0,
  "proteinPer100": grams,
  "carbsPer100": grams,
  "fatPer100": grams,
  "kcalPer100": calories,
  "suggestedGrams": typical serving size,
  "variant": "RAW|COOKED|NONE"
}
Context: ${context ?: "none"}
Use USDA/reference values. If unsure, set confidence < 0.7.
Return ONLY the JSON, no markdown, no extra text."""
    }

    private fun buildChatPrompt(messages: List<ChatMessage>): String {
        val systemPrompt = """You are a knowledgeable fitness and nutrition coach. Be concise, encouraging, and actionable.
User context: goals, recent logs, today's remaining macros.
Give practical advice for meals, workouts, recovery."""
        val conversation = messages.map { "${it.role}: ${it.content}" }.joinToString("\n")
        return "$systemPrompt\n\nConversation:\n$conversation\n\nCoach:"
    }

    private fun buildRequest(prompt: String, imageBase64: String): okhttp3.RequestBody {
        val parts = mutableListOf<JsonObject>()
        parts.add(JsonObject().apply { addProperty("text", prompt) })
        parts.add(JsonObject().apply {
            add("inline_data", JsonObject().apply {
                addProperty("mime_type", "image/jpeg")
                addProperty("data", imageBase64)
            })
        })
        val contents = JsonObject().apply {
            add("parts", gson.toJsonTree(parts))
        }
        val json = JsonObject().apply {
            add("contents", gson.toJsonTree(listOf(contents)))
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", 0.3)
                addProperty("maxOutputTokens", 500)
                addProperty("responseMimeType", "application/json")
            })
        }
        return gson.toJson(json).toRequestBody("application/json".toMediaType())
    }

    private fun buildTextRequest(prompt: String, imageBase64: String?): okhttp3.RequestBody {
        val parts = mutableListOf<JsonObject>()
        parts.add(JsonObject().apply { addProperty("text", prompt) })
        // A photo attached in the coach travels on the same turn as the text prompt.
        if (!imageBase64.isNullOrBlank()) {
            parts.add(JsonObject().apply {
                add("inline_data", JsonObject().apply {
                    addProperty("mime_type", "image/jpeg")
                    addProperty("data", imageBase64)
                })
            })
        }
        val contents = JsonObject().apply {
            add("parts", gson.toJsonTree(parts))
        }
        val json = JsonObject().apply {
            add("contents", gson.toJsonTree(listOf(contents)))
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", 0.7)
                addProperty("maxOutputTokens", 1000)
            })
        }
        return gson.toJson(json).toRequestBody("application/json".toMediaType())
    }

    private fun parseFoodResponse(body: String): FoodAnalysisResult? = try {
        val json = JsonParser.parseString(body).asJsonObject
        val candidates = json.getAsJsonArray("candidates")
        if (candidates.isEmpty()) {
            null
        } else {
            val content = candidates[0].asJsonObject.getAsJsonObject("content")
            val parts = content.getAsJsonArray("parts")
            val text = parts[0].asJsonObject.get("text").asString
            gson.fromJson(text, FoodAnalysisResult::class.java)
        }
    } catch (e: Exception) {
        null
    }

    private fun parseChatResponse(body: String): String? = try {
        val json = JsonParser.parseString(body).asJsonObject
        val candidates = json.getAsJsonArray("candidates")
        if (candidates == null || candidates.isEmpty()) {
            null
        } else {
            // Gemini 3.x can include reasoning parts; only join the visible answer text.
            val parts = candidates[0].asJsonObject
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
            parts.mapNotNull { part ->
                val obj = part.asJsonObject
                if (obj.get("thought")?.asBoolean == true) null else obj.get("text")?.asString
            }.joinToString("").ifBlank { null }
        }
    } catch (e: Exception) {
        null
    }
}

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)