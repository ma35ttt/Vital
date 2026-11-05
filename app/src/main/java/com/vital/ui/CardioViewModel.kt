package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.CardioSession
import com.vital.data.entity.CardioType
import com.vital.data.entity.KcalSource
import com.vital.data.entity.Settings
import com.vital.data.repository.CardioRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.seed.LOCAL_USER_ID
import com.vital.domain.Acsm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Cardio's in-progress form. The nav entry (and therefore the ViewModel) is destroyed when
 * the user switches tabs, so the four fields and the selected type live here until the
 * process dies. Saved sessions are Room rows.
 */
@Singleton
class CardioDraftStore @Inject constructor() {
    val type = MutableStateFlow(CardioType.RUNNING)
    val durationText = MutableStateFlow("")
    val distanceText = MutableStateFlow("")
    val speedText = MutableStateFlow("")
    val inclineText = MutableStateFlow("")
}

/**
 * Cardio reads today's sessions for the hero total and the three most recent sessions for
 * the RECENT list. Calories are always an ACSM walking estimate from the form; the app has
 * no calorie field on this tab.
 */
@HiltViewModel
class CardioViewModel @Inject constructor(
    private val cardioRepository: CardioRepository,
    private val settingsRepository: SettingsRepository,
    private val draft: CardioDraftStore
) : ViewModel() {

    private val date: String = LocalDate.now().toString()
    private val fallback = Settings.create(LOCAL_USER_ID)
    private val saveMutex = Mutex()

    val todayKcal: StateFlow<Double> =
        cardioRepository.getCardioForDate(LOCAL_USER_ID, date)
            .map { sessions -> sessions.sumOf { it.kcal } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val recentSessions: StateFlow<List<CardioSession>> =
        cardioRepository.getAllCardioSessions(LOCAL_USER_ID)
            .map { sessions -> sessions.take(3) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val type: StateFlow<CardioType> = draft.type.asStateFlow()
    val durationText: StateFlow<String> = draft.durationText.asStateFlow()
    val distanceText: StateFlow<String> = draft.distanceText.asStateFlow()
    val speedText: StateFlow<String> = draft.speedText.asStateFlow()
    val inclineText: StateFlow<String> = draft.inclineText.asStateFlow()

    fun setType(value: CardioType) {
        draft.type.value = value
    }

    fun setDuration(text: String) {
        draft.durationText.value = text
            .filter { it.isDigit() || it == ':' }
            .take(8)
    }

    fun setDistance(text: String) {
        draft.distanceText.value = filterDecimal(text)
    }

    fun setSpeed(text: String) {
        draft.speedText.value = filterDecimal(text)
    }

    fun setIncline(text: String) {
        draft.inclineText.value = filterDecimal(text)
    }

    fun save() {
        val durationSec = parseCardioDuration(durationText.value) ?: return
        val typedDistance = distanceText.value.toDoubleOrNull()?.takeIf { it > 0.0 }
        val typedSpeed = speedText.value.toDoubleOrNull()?.takeIf { it > 0.0 }
        val speed = typedSpeed ?: typedDistance?.let { km ->
            if (durationSec > 0) km / (durationSec / 3_600.0) else null
        }
        // The mock shows the distance for a treadmill entry, so fill it in from speed x time.
        val distance = typedDistance ?: typedSpeed?.let { kmh -> kmh * durationSec / 3_600.0 }
        val incline = inclineText.value.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

        viewModelScope.launch {
            saveMutex.withLock {
                // Read the current weight here: nothing on this screen collects the settings
                // StateFlow, so a WhileSubscribed flow would stay at its fallback value.
                val weightKg = settingsRepository.getSettings(LOCAL_USER_ID)
                    .first()?.bodyWeightKg ?: fallback.bodyWeightKg
                val kcal = estimateCardioKcal(speed, incline, weightKg, durationSec)

                cardioRepository.insertCardioSession(
                    CardioSession.create(
                        userId = LOCAL_USER_ID,
                        date = date,
                        type = type.value,
                        durationSec = durationSec,
                        kcal = kcal ?: 0.0,
                        distanceKm = distance,
                        speedKmh = speed,
                        inclinePct = incline,
                        kcalSource = if (kcal != null) KcalSource.ACSM else KcalSource.READ
                    )
                )
                draft.durationText.value = ""
                draft.distanceText.value = ""
                draft.speedText.value = ""
                draft.inclineText.value = ""
            }
        }
    }

    fun delete(session: CardioSession) {
        viewModelScope.launch {
            cardioRepository.softDeleteCardioSession(session.id, System.currentTimeMillis())
        }
    }

    private fun filterDecimal(text: String): String {
        val filtered = text.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && text.take(index).none { it == '.' })
        }.take(6)
        return filtered
    }
}

/**
 * `32:10` is mm:ss, `1:02:10` is h:mm:ss and a bare `45` is 45 minutes.
 * Returns null when any component is out of range or the value is zero.
 */
internal fun parseCardioDuration(text: String): Int? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    val parts = trimmed.split(':')
    if (parts.size > 3) return null
    if (parts.any { it.isEmpty() || it.any { char -> !char.isDigit() } }) return null
    val numbers = parts.map { it.toIntOrNull() ?: return null }
    val seconds = when (numbers.size) {
        1 -> numbers[0] * 60
        2 -> numbers[0] * 60 + numbers[1]
        3 -> numbers[0] * 3_600 + numbers[1] * 60 + numbers[2]
        else -> return null
    }
    if (numbers.size >= 2 && numbers.last() >= 60) return null
    if (numbers.size == 3 && numbers[1] >= 60) return null
    return seconds.takeIf { it > 0 }
}

internal fun estimateCardioKcal(
    speedKmh: Double?,
    inclinePercent: Double,
    weightKg: Double,
    durationSeconds: Int
): Double? {
    val speed = speedKmh?.takeIf { it > 0.0 } ?: return null
    // The ACSM walking equation is only defined up to 6 km/h. Above that we still show an
    // estimate, clamped at the limit, and the footer switches to OUT OF RANGE.
    return Acsm.kcal(
        speedKmh = speed.coerceAtMost(Acsm.MAX_SPEED_KMH),
        inclinePercent = inclinePercent,
        weightKg = weightKg,
        durationSeconds = durationSeconds
    )
}

internal fun formatCardioSpeed(value: Double): String =
    String.format(Locale.US, "%.1f", value)

internal fun formatCardioKcal(value: Double): String =
    String.format(Locale.US, "%,d", value.roundToInt())

/** `yyyy-MM-dd` to the mock's `dd.MM`; unknown dates pass through unchanged. */
internal fun formatCardioDate(date: String): String = runCatching {
    LocalDate.parse(date).let { "%02d.%02d".format(Locale.US, it.dayOfMonth, it.monthValue) }
}.getOrDefault(date)
