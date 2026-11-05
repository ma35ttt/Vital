package com.vital.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.ai.AiProvider as AiSdkProvider
import com.vital.ai.ChatMessage
import com.vital.ai.CoachChatService
import com.vital.ai.CoachContext
import com.vital.ai.CoachReply
import com.vital.data.entity.AiProvider
import com.vital.data.entity.Settings
import com.vital.data.repository.MealLogRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.UserRepository
import com.vital.data.repository.WorkoutRepository
import com.vital.data.seed.LOCAL_USER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

/** One visible turn. [imageBase64] is kept only so a failed send can be retried. */
data class CoachMsg(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val image: ImageBitmap? = null,
    val imageBase64: String? = null,
    val chips: List<String> = emptyList()
)

data class CoachUiState(
    val provider: AiProvider = AiProvider.GEMINI,
    val hasKey: Boolean = false,
    val messages: List<CoachMsg> = emptyList(),
    val sending: Boolean = false,
    val error: String? = null
)

/**
 * Backs the coach screen. Owns the visible conversation and turns it plus live Room data into a
 * [CoachContext] for the provider. The API key lives in Settings; this only reads it.
 */
@HiltViewModel
class CoachViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val mealLogRepository: MealLogRepository,
    private val workoutRepository: WorkoutRepository,
    private val coachChatService: CoachChatService
) : ViewModel() {

    private val fallback = Settings.create(LOCAL_USER_ID)
    private val today: String = LocalDate.now().toString()

    private val settings = settingsRepository.getSettings(LOCAL_USER_ID)
        .map { it ?: fallback }

    private val _messages = MutableStateFlow<List<CoachMsg>>(emptyList())
    private val _sending = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    /** The payload of the most recent attempt, so RETRY can resend it verbatim. */
    private var lastAttempt: Triple<String, ImageBitmap?, String?>? = null

    val state: StateFlow<CoachUiState> =
        combine(settings, _messages, _sending, _error) { s, messages, sending, error ->
            CoachUiState(
                provider = s.aiProvider,
                hasKey = !s.aiApiKeyEncrypted.isNullOrEmpty(),
                messages = messages,
                sending = sending,
                error = error
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CoachUiState())

    fun setProvider(provider: AiProvider) = updateSettings { it.copy(aiProvider = provider) }

    /** Saves the provider and key together, the same write the coach's "Connect a model" makes. */
    fun connect(provider: AiProvider, key: String) = updateSettings {
        it.copy(aiProvider = provider, aiApiKeyEncrypted = key.trim().ifBlank { null })
    }

    fun send(text: String, image: ImageBitmap?, imageBase64: String?) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() && imageBase64 == null) return
        if (_sending.value) return

        lastAttempt = Triple(trimmed, image, imageBase64)
        _messages.value = _messages.value + CoachMsg(
            id = UUID.randomUUID().toString(),
            text = trimmed.ifEmpty { "(photo)" },
            isUser = true,
            image = image,
            imageBase64 = imageBase64
        )
        _error.value = null
        _sending.value = true

        viewModelScope.launch {
            val s = settingsRepository.getSettings(LOCAL_USER_ID).first() ?: fallback
            val key = s.aiApiKeyEncrypted?.takeIf { it.isNotBlank() }
            if (key == null) {
                _sending.value = false
                _error.value = "Add an API key to start chatting."
                return@launch
            }

            val context = buildContext(s)
            val history = _messages.value.dropLast(1).map {
                ChatMessage(if (it.isUser) "user" else "assistant", it.text)
            }

            val reply = coachChatService.respond(
                history = history,
                userText = trimmed.ifEmpty { "Estimate this meal and its macros." },
                imageBase64 = imageBase64,
                provider = s.aiProvider.toSdk(),
                apiKey = key,
                context = context
            )
            _sending.value = false
            when (reply) {
                is CoachReply.Ok -> _messages.value = _messages.value + CoachMsg(
                    id = UUID.randomUUID().toString(),
                    text = reply.text,
                    isUser = false,
                    chips = chipsFor(context)
                )
                is CoachReply.Error -> _error.value = reply.message
            }
        }
    }

    fun retry() {
        val (text, image, base64) = lastAttempt ?: return
        // Drop the failed user turn so the resend does not duplicate it.
        _messages.value = _messages.value.dropLastWhile { it.isUser }
        _error.value = null
        send(text, image, base64)
    }

    fun clearError() {
        _error.value = null
    }

    fun clear() {
        _messages.value = emptyList()
        _error.value = null
        lastAttempt = null
    }

    private fun updateSettings(transform: (Settings) -> Settings) {
        viewModelScope.launch {
            val current = settingsRepository.getSettings(LOCAL_USER_ID).first() ?: fallback
            settingsRepository.insertSettings(
                transform(current).copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    private suspend fun buildContext(s: Settings): CoachContext {
        val meals = mealLogRepository.getMealsForDate(LOCAL_USER_ID, today).first()
        val sessions = workoutRepository.getSessionsForDate(LOCAL_USER_ID, today).first()
        val user = userRepository.getUser(LOCAL_USER_ID).first()

        val eatenKcal = meals.sumOf { it.kcal }
        val protein = meals.sumOf { it.proteinG }
        val carbs = meals.sumOf { it.carbsG }
        val fat = meals.sumOf { it.fatG }

        val recentMeals = meals.takeIf { it.isNotEmpty() }?.joinToString(", ") {
            "${it.foodName} (${it.kcal.roundToInt()} kcal)"
        }
        val recentWorkout = sessions.takeIf { it.isNotEmpty() }?.joinToString(", ") { session ->
            val minutes = (((session.endedAt ?: session.updatedAt) - session.startedAt) / 60_000)
                .coerceAtLeast(0)
            "lift $minutes min"
        }

        return CoachContext(
            name = user?.name?.takeIf { it.isNotBlank() },
            goals = null,
            bodyWeightKg = s.bodyWeightKg,
            kcalTarget = s.kcalTarget,
            proteinTarget = s.proteinTarget,
            carbsTarget = s.carbsTarget,
            fatTarget = s.fatTarget,
            todayKcal = eatenKcal,
            todayProtein = protein,
            todayCarbs = carbs,
            todayFat = fat,
            remainingKcal = s.kcalTarget - eatenKcal,
            remainingProtein = s.proteinTarget - protein,
            remainingCarbs = s.carbsTarget - carbs,
            remainingFat = s.fatTarget - fat,
            recentWorkout = recentWorkout,
            recentMeals = recentMeals
        )
    }

    private fun chipsFor(context: CoachContext): List<String> = buildList {
        if (context.remainingKcal > 0) add("${context.remainingKcal.roundToInt()} KCAL LEFT")
        if (context.remainingProtein > 0) add("${context.remainingProtein.roundToInt()} G PROTEIN")
        if (isEmpty()) add("${context.kcalTarget} KCAL TARGET")
    }.take(2)
}

private fun AiProvider.toSdk(): AiSdkProvider = when (this) {
    AiProvider.GEMINI -> AiSdkProvider.GEMINI
    AiProvider.OPENROUTER -> AiSdkProvider.OPENROUTER
}
