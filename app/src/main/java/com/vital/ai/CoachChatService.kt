package com.vital.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Outcome of one coach turn. The UI owns the visible message list, so nothing is kept here. */
sealed interface CoachReply {
    data class Ok(val text: String) : CoachReply
    data class Error(val message: String) : CoachReply
}

@Singleton
class CoachChatService @Inject constructor(
    private val geminiClient: GeminiClient,
    private val openRouterClient: OpenRouterClient
) {

    /**
     * Sends the whole conversation plus the new [userText] (and an optional photo) and returns
     * the coach's reply. [history] is every prior turn, oldest first, without the new message.
     */
    suspend fun respond(
        history: List<ChatMessage>,
        userText: String,
        imageBase64: String?,
        provider: AiProvider,
        apiKey: String,
        context: CoachContext
    ): CoachReply = withContext(Dispatchers.IO) {
        val full = buildList {
            add(ChatMessage("system", buildContextPrompt(context)))
            addAll(history)
            add(ChatMessage("user", userText))
        }

        val result = when (provider) {
            AiProvider.GEMINI -> geminiClient.chat(full, apiKey, imageBase64)
            AiProvider.OPENROUTER -> openRouterClient.chat(full, apiKey, imageBase64 = imageBase64)
        }

        when (result) {
            is AiChatResult.Ok -> CoachReply.Ok(result.text.trim())
            is AiChatResult.Failure ->
                CoachReply.Error("${provider.displayName}: ${result.message}")
        }
    }

    private fun buildContextPrompt(context: CoachContext): String {
        return """You are Vital's fitness and nutrition coach. Be concise (2-3 sentences max), encouraging, and actionable.
When the user sends a photo of food, estimate the meal and its macros from the image.

USER CONTEXT:
- Name: ${context.name ?: "not set"}
- Goals: ${context.goals ?: "not set"}
- Body weight: ${context.bodyWeightKg} kg
- Daily targets: ${context.kcalTarget} kcal, ${context.proteinTarget}g protein, ${context.carbsTarget}g carbs, ${context.fatTarget}g fat
- Today so far: ${context.todayKcal.round1()} kcal, ${context.todayProtein.round1()}g P, ${context.todayCarbs.round1()}g C, ${context.todayFat.round1()}g F
- Remaining: ${context.remainingKcal.round1()} kcal, ${context.remainingProtein.round1()}g P, ${context.remainingCarbs.round1()}g C, ${context.remainingFat.round1()}g F
- Today's workouts: ${context.recentWorkout ?: "none"}
- Today's meals: ${context.recentMeals ?: "none"}

Reference the user's actual numbers when relevant. Never invent data you were not given."""
    }
}

private fun Double.round1(): String {
    val rounded = kotlin.math.round(this * 10) / 10
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

val AiProvider.displayName: String
    get() = when (this) {
        AiProvider.GEMINI -> "Gemini"
        AiProvider.OPENROUTER -> "OpenRouter"
    }

data class CoachContext(
    val name: String? = null,
    val goals: String? = null,
    val bodyWeightKg: Double,
    val kcalTarget: Int,
    val proteinTarget: Int,
    val carbsTarget: Int,
    val fatTarget: Int,
    val todayKcal: Double,
    val todayProtein: Double,
    val todayCarbs: Double,
    val todayFat: Double,
    val remainingKcal: Double,
    val remainingProtein: Double,
    val remainingCarbs: Double,
    val remainingFat: Double,
    val recentWorkout: String?,
    val recentMeals: String?
)
