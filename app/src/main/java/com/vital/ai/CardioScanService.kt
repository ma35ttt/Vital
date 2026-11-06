package com.vital.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads a treadmill/console photo into the four running and walking fields. Only those two
 * activities have a console layout the app can fill, so callers gate on the activity type.
 */
@Singleton
class CardioScanService @Inject constructor(
    private val geminiClient: GeminiClient,
    private val openRouterClient: OpenRouterClient
) {

    sealed interface Result {
        data class Success(val reading: TreadmillReading) : Result
        data class Error(val message: String) : Result
    }

    suspend fun scan(
        imageBase64: String,
        provider: AiProvider,
        apiKey: String
    ): Result = withContext(Dispatchers.IO) {
        val raw = when (provider) {
            AiProvider.GEMINI -> geminiClient.analyzeTreadmill(imageBase64, apiKey)
            AiProvider.OPENROUTER -> openRouterClient.analyzeTreadmill(imageBase64, apiKey)
        } ?: return@withContext Result.Error("Couldn't read that photo. Try again.")

        val reading = raw.sanitized()
        val values = listOfNotNull(
            reading.durationSec,
            reading.distanceKm,
            reading.speedKmh,
            reading.inclinePct
        )
        when {
            values.isEmpty() ->
                Result.Error("No session numbers were visible. Fit the whole display in frame.")
            reading.confidence < 0.4 ->
                Result.Error("The display wasn't clear enough. Move closer and retry.")
            else -> Result.Success(reading)
        }
    }
}