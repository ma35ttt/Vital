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
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

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
            .url("$baseUrl?key=$apiKey")
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

    suspend fun chat(messages: List<ChatMessage>, apiKey: String): String? = withContext(Dispatchers.IO) {
        val prompt = buildChatPrompt(messages)
        val requestBody = buildTextRequest(prompt)
        val request = Request.Builder()
            .url("$baseUrl?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            parseChatResponse(body)
        } catch (e: Exception) {
            null
        }
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

    private fun buildTextRequest(prompt: String): okhttp3.RequestBody {
        val contents = JsonObject().apply {
            add("parts", gson.toJsonTree(listOf(JsonObject().apply { addProperty("text", prompt) })))
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
        if (candidates.isEmpty()) {
            null
        } else {
            val content = candidates[0].asJsonObject.getAsJsonObject("content")
            val parts = content.getAsJsonArray("parts")
            parts[0].asJsonObject.get("text").asString
        }
    } catch (e: Exception) {
        null
    }
}

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)