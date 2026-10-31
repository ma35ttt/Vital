package com.vital.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoachChatService @Inject constructor(
    private val geminiClient: GeminiClient,
    private val openRouterClient: OpenRouterClient
) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    suspend fun sendMessage(
        userMessage: String,
        provider: AiProvider,
        apiKey: String,
        context: CoachContext
    ): String? {
        _isLoading.value = true
        val newMessages = _messages.value + ChatMessage("user", userMessage)
        _messages.value = newMessages

        return withContext(Dispatchers.IO) {
            val contextPrompt = buildContextPrompt(context)
            val fullMessages = listOf(ChatMessage("system", contextPrompt)) + newMessages

            val response = when (provider) {
                AiProvider.GEMINI -> geminiClient.chat(fullMessages, apiKey)
                AiProvider.OPENROUTER -> openRouterClient.chat(fullMessages, apiKey)
            }

            response?.let {
                _messages.value = newMessages + ChatMessage("assistant", it)
            }
            _isLoading.value = false
            response
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }

    private fun buildContextPrompt(context: CoachContext): String {
        return """You are a knowledgeable fitness and nutrition coach. Be concise (2-3 sentences max), encouraging, and actionable.

USER CONTEXT:
- Goals: ${context.goals}
- Body weight: ${context.bodyWeightKg} kg
- Daily targets: ${context.kcalTarget} kcal, ${context.proteinTarget}g protein, ${context.carbsTarget}g carbs, ${context.fatTarget}g fat
- Today so far: ${context.todayKcal} kcal, ${context.todayProtein}g P, ${context.todayCarbs}g C, ${context.todayFat}g F
- Remaining: ${context.remainingKcal} kcal, ${context.remainingProtein}g P, ${context.remainingCarbs}g C, ${context.remainingFat}g F
- Recent workout: ${context.recentWorkout ?: "none"}
- Recent meals: ${context.recentMeals ?: "none"}

Provide practical, specific advice. Reference their actual numbers when relevant."""
    }
}

data class CoachContext(
    val goals: String,
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