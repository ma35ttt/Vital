package com.vital.ai

import com.vital.data.entity.Food
import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodAnalysisService @Inject constructor(
    private val geminiClient: GeminiClient,
    private val openRouterClient: OpenRouterClient
) {

    sealed interface Result {
        data class Success(val food: Food) : Result
        data class Error(val message: String) : Result
        object LowConfidence : Result
    }

    suspend fun analyze(
        imageBase64: String,
        context: String?,
        provider: AiProvider,
        apiKey: String
    ): Result = withContext(Dispatchers.IO) {
        val result: GeminiClient.FoodAnalysisResult? = when (provider) {
            AiProvider.GEMINI -> geminiClient.analyzeFood(imageBase64, context, apiKey)
            AiProvider.OPENROUTER -> openRouterClient.analyzeFood(imageBase64, context, apiKey)?.let { r ->
                GeminiClient.FoodAnalysisResult(
                    foodName = r.foodName,
                    confidence = r.confidence,
                    proteinPer100 = r.proteinPer100,
                    carbsPer100 = r.carbsPer100,
                    fatPer100 = r.fatPer100,
                    kcalPer100 = r.kcalPer100,
                    suggestedGrams = r.suggestedGrams,
                    variant = r.variant
                )
            }
        }
        result?.let { aiResult ->
            if (aiResult.confidence < 0.7) {
                Result.LowConfidence
            } else {
                val variant = when (aiResult.variant?.uppercase()) {
                    "RAW" -> FoodVariant.RAW
                    "COOKED" -> FoodVariant.COOKED
                    else -> FoodVariant.NONE
                }
                val food = Food.create(
                    name = aiResult.foodName,
                    variant = variant,
                    proteinPer100 = aiResult.proteinPer100,
                    carbsPer100 = aiResult.carbsPer100,
                    fatPer100 = aiResult.fatPer100,
                    source = FoodSource.AI
                ).copy(
                    kcalPer100 = aiResult.kcalPer100
                )
                Result.Success(food)
            }
        } ?: Result.Error("AI analysis failed")
    }
}