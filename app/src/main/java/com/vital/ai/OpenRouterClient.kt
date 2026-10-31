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

class OpenRouterClient(private val client: OkHttpClient) {

    private val gson = Gson()
    private val baseUrl = "https://openrouter.ai/api/v1/chat/completions"

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

    suspend fun analyzeFood(imageBase64: String, context: String?, apiKey: String): FoodAnalysisResult? = withContext(Dispatchers.IO) {
        val prompt = buildFoodPrompt(context)
        val requestBody = buildRequest(prompt, imageBase64)
        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://vital.app")
            .addHeader("X-Title", "Vital")
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

    suspend fun chat(messages: List<ChatMessage>, apiKey: String, model: String = "meta-llama/llama-3.1-8b-instruct:free"): String? = withContext(Dispatchers.IO) {
        val requestBody = buildChatRequest(messages, model)
        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://vital.app")
            .addHeader("X-Title", "Vital")
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

    private fun buildRequest(prompt: String, imageBase64: String): okhttp3.RequestBody {
        val content = mutableListOf<JsonObject>()
        content.add(JsonObject().apply { addProperty("type", "text"); addProperty("text", prompt) })
        content.add(JsonObject().apply {
            addProperty("type", "image_url")
            add("image_url", JsonObject().apply {
                addProperty("url", "data:image/jpeg;base64,$imageBase64")
            })
        })
        val messages = listOf(JsonObject().apply {
            addProperty("role", "user")
            add("content", gson.toJsonTree(content))
        })
        val json = JsonObject().apply {
            addProperty("model", "google/gemini-2.0-flash-exp:free")
            add("messages", gson.toJsonTree(messages))
            addProperty("temperature", 0.3)
            addProperty("max_tokens", 500)
            add("response_format", JsonObject().apply { addProperty("type", "json_object") })
        }
        return gson.toJson(json).toRequestBody("application/json".toMediaType())
    }

    private fun buildChatRequest(messages: List<ChatMessage>, model: String): okhttp3.RequestBody {
        val systemPrompt = """You are a knowledgeable fitness and nutrition coach. Be concise, encouraging, and actionable.
Give practical advice for meals, workouts, recovery."""
        val chatMessages = mutableListOf<JsonObject>()
        chatMessages.add(JsonObject().apply {
            addProperty("role", "system")
            addProperty("content", systemPrompt)
        })
        messages.forEach { msg ->
            chatMessages.add(JsonObject().apply {
                addProperty("role", msg.role)
                addProperty("content", msg.content)
            })
        }
        val json = JsonObject().apply {
            addProperty("model", model)
            add("messages", gson.toJsonTree(chatMessages))
            addProperty("temperature", 0.7)
            addProperty("max_tokens", 1000)
            addProperty("stream", false)
        }
        return gson.toJson(json).toRequestBody("application/json".toMediaType())
    }

    private fun parseFoodResponse(body: String): FoodAnalysisResult? = try {
        val json = JsonParser.parseString(body).asJsonObject
        val choices = json.getAsJsonArray("choices")
        if (choices.isEmpty()) {
            null
        } else {
            val message = choices[0].asJsonObject.getAsJsonObject("message")
            val content = message.get("content").asString
            gson.fromJson(content, FoodAnalysisResult::class.java)
        }
    } catch (e: Exception) {
        null
    }

    private fun parseChatResponse(body: String): String? = try {
        val json = JsonParser.parseString(body).asJsonObject
        val choices = json.getAsJsonArray("choices")
        if (choices.isEmpty()) {
            null
        } else {
            val message = choices[0].asJsonObject.getAsJsonObject("message")
            message.get("content").asString
        }
    } catch (e: Exception) {
        null
    }
}