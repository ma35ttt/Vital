package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.ai.CardioScanService
import com.vital.data.entity.CardioSession
import com.vital.data.entity.CardioType
import com.vital.data.entity.Intensity
import com.vital.data.entity.KcalSource
import com.vital.data.entity.Settings
import com.vital.data.entity.StrokeType
import com.vital.data.repository.CardioRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.seed.LOCAL_USER_ID
import com.vital.domain.Acsm
import com.vital.domain.Met
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
 * the user switches tabs, so the selected type and the typed values live here until the
 * process dies. Values are keyed by [CardioField], so switching activity keeps the shared
 * ones (duration, distance) and simply hides the rest. Saved sessions are Room rows.
 */
@Singleton
class CardioDraftStore @Inject constructor() {
    val type = MutableStateFlow(CardioType.RUNNING)
    val values = MutableStateFlow<Map<CardioField, String>>(emptyMap())
}

/**
 * The treadmill-photo affordance. [Filled] carries how many inputs the read replaced so the
 * screen can ask the user to check them before logging.
 */
sealed interface CardioPhotoState {
    object Idle : CardioPhotoState
    object Reading : CardioPhotoState
    data class Filled(val fields: Int) : CardioPhotoState
    data class Error(val message: String) : CardioPhotoState
}

/**
 * Cardio reads today's sessions for the hero total and the three most recent sessions for
 * the RECENT list. Walking and running estimate with ACSM; every other activity uses a MET
 * estimate. The app has no calorie field on this tab.
 */
@HiltViewModel
class CardioViewModel @Inject constructor(
    private val cardioRepository: CardioRepository,
    private val settingsRepository: SettingsRepository,
    private val draft: CardioDraftStore,
    private val scanService: CardioScanService
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

    val todayCount: StateFlow<Int> =
        cardioRepository.getCardioForDate(LOCAL_USER_ID, date)
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val type: StateFlow<CardioType> = draft.type.asStateFlow()
    val values: StateFlow<Map<CardioField, String>> = draft.values.asStateFlow()

    private val _photo = MutableStateFlow<CardioPhotoState>(CardioPhotoState.Idle)
    val photo: StateFlow<CardioPhotoState> = _photo.asStateFlow()

    /** Current body weight, read once here so the note can preview the estimate. */
    private val weightKg: StateFlow<Double> =
        settingsRepository.getSettings(LOCAL_USER_ID)
            .map { it?.bodyWeightKg ?: fallback.bodyWeightKg }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), fallback.bodyWeightKg)

    /** Live calories for the current form, or null while the duration is missing/invalid. */
    val estimateKcal: StateFlow<Double?> =
        combine(draft.type, draft.values, weightKg) { type, values, weight ->
            parseCardioEntry(type, values)?.let { estimateSessionKcal(type, it, weight) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setType(value: CardioType) {
        draft.type.value = value
        _photo.value = CardioPhotoState.Idle
    }

    fun setValue(field: CardioField, text: String) {
        val filtered = when (field.input) {
            CardioInput.TEXT -> text.filter { it.isDigit() || it == ':' }.take(8)
            CardioInput.INTEGER -> text.filter { it.isDigit() }.take(4)
            CardioInput.DECIMAL -> filterDecimal(text)
            CardioInput.PICKER -> return
        }
        draft.values.value = draft.values.value + (field to filtered)
        _photo.value = CardioPhotoState.Idle
    }

    fun cycle(field: CardioField) {
        if (field.input != CardioInput.PICKER) return
        val next = cyclePicker(field, draft.values.value[field]) ?: return
        draft.values.value = draft.values.value + (field to next)
        _photo.value = CardioPhotoState.Idle
    }

    /** Dismiss the photo status line without touching the typed values. */
    fun dismissPhoto() {
        _photo.value = CardioPhotoState.Idle
    }

    /**
     * Reads a treadmill photo into the Run/Walk inputs. Ignored for the other activities,
     * which have no console layout this can fill. A missing key is a normal, explainable
     * state rather than a silent no-op.
     */
    fun scanPhoto(imageBase64: String) {
        if (!draft.type.value.usesAcsm) return
        if (_photo.value == CardioPhotoState.Reading) return
        _photo.value = CardioPhotoState.Reading

        viewModelScope.launch {
            val settings = settingsRepository.getSettings(LOCAL_USER_ID).first() ?: fallback
            val key = settings.aiApiKeyEncrypted?.takeIf { it.isNotBlank() }
            if (key == null) {
                _photo.value = CardioPhotoState.Error(
                    "Add an AI key in Settings to scan a treadmill photo."
                )
                return@launch
            }
            // The user may have switched activity while the request was in flight.
            if (!draft.type.value.usesAcsm) {
                _photo.value = CardioPhotoState.Idle
                return@launch
            }
            when (val result = scanService.scan(imageBase64, settings.aiProvider.toSdk(), key)) {
                is CardioScanService.Result.Success -> {
                    val filled = treadmillDraftValues(result.reading)
                    if (filled.isEmpty()) {
                        _photo.value = CardioPhotoState.Error(
                            "No session numbers were visible. Fit the whole display in frame."
                        )
                    } else {
                        draft.values.value = draft.values.value + filled
                        _photo.value = CardioPhotoState.Filled(filled.size)
                    }
                }
                is CardioScanService.Result.Error ->
                    _photo.value = CardioPhotoState.Error(result.message)
            }
        }
    }

    fun save() {
        val type = draft.type.value
        val entry = parseCardioEntry(type, draft.values.value) ?: return

        viewModelScope.launch {
            saveMutex.withLock {
                // Read the current weight here: the screen only collects the estimate flow.
                val weight = settingsRepository.getSettings(LOCAL_USER_ID)
                    .first()?.bodyWeightKg ?: fallback.bodyWeightKg
                val kcal = estimateSessionKcal(type, entry, weight)

                cardioRepository.insertCardioSession(
                    CardioSession.create(
                        userId = LOCAL_USER_ID,
                        date = date,
                        type = type,
                        durationSec = entry.durationSec,
                        kcal = kcal ?: 0.0,
                        distanceKm = entry.distanceKm,
                        speedKmh = entry.speedKmh,
                        inclinePct = entry.inclinePct,
                        level = entry.level,
                        cadenceSpm = entry.cadenceSpm,
                        floors = entry.floors,
                        laps = entry.laps,
                        stroke = entry.stroke,
                        intensity = entry.intensity,
                        kcalSource = when {
                            kcal == null -> KcalSource.READ
                            type.usesAcsm -> KcalSource.ACSM
                            else -> KcalSource.MET
                        }
                    )
                )
                draft.values.value = emptyMap()
                _photo.value = CardioPhotoState.Idle
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
 * A typed Cardio form parsed into the values a session stores. Null when the duration is
 * missing or invalid. Distance and speed are inferred from each other when only one is set;
 * rowing's /500 m pace is converted to a speed so it lands in the same column.
 */
internal data class CardioEntry(
    val durationSec: Int,
    val distanceKm: Double?,
    val speedKmh: Double?,
    val inclinePct: Double?,
    val level: Int?,
    val cadenceSpm: Double?,
    val floors: Int?,
    val laps: Int?,
    val stroke: StrokeType?,
    val intensity: Intensity?
)

internal fun parseCardioEntry(type: CardioType, values: Map<CardioField, String>): CardioEntry? {
    val durationSec = parseCardioDuration(values[CardioField.DURATION].orEmpty()) ?: return null

    val typedDistanceKm = values[CardioField.DISTANCE_KM]?.toDoubleOrNull()?.takeIf { it > 0.0 }
    val typedDistanceM = values[CardioField.DISTANCE_M]?.toDoubleOrNull()?.takeIf { it > 0.0 }
    val distanceKm = typedDistanceKm ?: typedDistanceM?.let { it / 1000.0 }

    val typedSpeed = values[CardioField.SPEED]?.toDoubleOrNull()?.takeIf { it > 0.0 }
    val paceSpeed = paceToSpeedKmh(values[CardioField.PACE])
    val speedKmh = typedSpeed ?: paceSpeed ?: distanceKm?.let { km ->
        if (durationSec > 0) km / (durationSec / 3_600.0) else null
    }
    // Mirror the existing screen: a treadmill-style speed fills in the distance.
    val distance = distanceKm ?: speedKmh?.let { it * durationSec / 3_600.0 }

    return CardioEntry(
        durationSec = durationSec,
        distanceKm = distance,
        speedKmh = speedKmh,
        inclinePct = values[CardioField.INCLINE]?.toDoubleOrNull()?.coerceAtLeast(0.0),
        level = values[CardioField.LEVEL]?.toIntOrNull(),
        cadenceSpm = values[CardioField.CADENCE]?.toDoubleOrNull(),
        floors = values[CardioField.FLOORS]?.toIntOrNull(),
        laps = values[CardioField.LAPS]?.toIntOrNull(),
        stroke = resolveStroke(type, values[CardioField.STROKE]),
        intensity = resolveIntensity(type, values[CardioField.INTENSITY])
    )
}

/** Walking/running go through ACSM; every other activity uses its MET value. */
internal fun estimateSessionKcal(
    type: CardioType,
    entry: CardioEntry,
    weightKg: Double
): Double? = if (type.usesAcsm) {
    estimateCardioKcal(entry.speedKmh, entry.inclinePct, weightKg, entry.durationSec)
} else {
    Met.kcal(type, weightKg, entry.durationSec, entry.intensity, entry.stroke)
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

/** Rowing's `2:30` per 500 m as a speed in km/h (1800 / seconds). */
internal fun paceToSpeedKmh(pace: String?): Double? {
    val seconds = pace?.let { parseCardioDuration(it) } ?: return null
    return if (seconds > 0) 1_800.0 / seconds else null
}

/** A running speed in km/h as the /500 m split, for the detail dialog. */
internal fun speedToPace(speedKmh: Double): Int? =
    if (speedKmh > 0.0) (1_800.0 / speedKmh).roundToInt() else null

internal fun estimateCardioKcal(
    speedKmh: Double?,
    inclinePercent: Double?,
    weightKg: Double,
    durationSeconds: Int
): Double? {
    val speed = speedKmh?.takeIf { it > 0.0 } ?: return null
    // The ACSM walking equation covers a walk; faster speeds fall through to the running
    // equation inside Acsm, so the estimate keeps working at jog/run pace.
    return Acsm.kcal(
        speedKmh = speed,
        inclinePercent = inclinePercent ?: 0.0,
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

/** Stored AI provider (data) to the client provider (ai). */
private fun com.vital.data.entity.AiProvider.toSdk(): com.vital.ai.AiProvider = when (this) {
    com.vital.data.entity.AiProvider.GEMINI -> com.vital.ai.AiProvider.GEMINI
    com.vital.data.entity.AiProvider.OPENROUTER -> com.vital.ai.AiProvider.OPENROUTER
}
