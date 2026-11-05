package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.Exercise
import com.vital.data.entity.Settings
import com.vital.data.entity.WorkoutSession
import com.vital.data.entity.WorkoutSet
import com.vital.data.repository.ExerciseRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.WorkoutRepository
import com.vital.data.seed.LOCAL_USER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Lift reads and writes the real WorkoutSession / WorkoutSet tables for today.
 *
 * The selected exercise, the weight/reps draft and the rest countdown live in singletons
 * because the Lift nav entry (and this ViewModel) is destroyed whenever the user changes
 * tabs. The logged sets themselves are Room rows, so they survive a restart.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LiftViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    private val draft: LiftDraftStore,
    private val restTimer: RestTimerController
) : ViewModel() {

    private val date: String = LocalDate.now().toString()
    private val fallback = Settings.create(LOCAL_USER_ID)
    private val logMutex = Mutex()

    /** The session created by the first LOG SET, before Room's flow has emitted it. */
    private var pendingSessionId: String? = null
    private val nextSetIndex = mutableMapOf<String, Int>()

    val settings: StateFlow<Settings> = settingsRepository.getSettings(LOCAL_USER_ID)
        .map { it ?: fallback }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), fallback)

    val exercises: StateFlow<List<Exercise>> = exerciseRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedExercise: StateFlow<Exercise?> =
        combine(exercises, draft.selectedExerciseId) { list, id ->
            list.firstOrNull { it.id == id }
                ?: list.firstOrNull { it.name == "Bench Press" }
                ?: list.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val todaySessions: StateFlow<List<WorkoutSession>> =
        workoutRepository.getSessionsForDate(LOCAL_USER_ID, date)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeSession: StateFlow<WorkoutSession?> = todaySessions
        .map { sessions -> sessions.firstOrNull { it.endedAt == null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessionSets: StateFlow<List<WorkoutSet>> = activeSession
        .flatMapLatest { session ->
            if (session == null) flowOf<List<WorkoutSet>>(emptyList())
            else workoutRepository.getSetsForSession(session.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exerciseSets: StateFlow<List<WorkoutSet>> =
        combine(sessionSets, selectedExercise) { sets, exercise ->
            if (exercise == null) emptyList() else sets.filter { it.exerciseId == exercise.id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lastSet: StateFlow<WorkoutSet?> = selectedExercise
        .flatMapLatest { exercise: Exercise? ->
            if (exercise == null) flowOf<WorkoutSet?>(null)
            else workoutRepository.getLatestSetForExercise(exercise.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessionVolume: StateFlow<Double> = sessionSets
        .map { sets -> sets.sumOf { it.weightKg * it.reps } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** Three presets around the stored rest time: previous minute, current, next minute. */
    val restPresets: StateFlow<List<Int>> = settings
        .map { current ->
            val base = current.restPresetSec.coerceIn(60, 3_600)
            listOf((base - 60).coerceAtLeast(30), base, base + 60)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf(120, 180, 240))

    val restRemaining: StateFlow<Int> = restTimer.remainingSeconds

    val weightText: StateFlow<String> = draft.weightText.asStateFlow()
    val repsText: StateFlow<String> = draft.repsText.asStateFlow()

    init {
        viewModelScope.launch {
            val list = exercises.first { it.isNotEmpty() }
            val storedId = draft.selectedExerciseId.value
                ?.takeIf { id -> list.any { it.id == id } }

            if (storedId == null) {
                val default = list.firstOrNull { it.name == "Bench Press" } ?: list.first()
                draft.selectedExerciseId.value = default.id
                if (draft.weightText.value.isBlank() && draft.repsText.value.isBlank()) {
                    prefillFromLastSet(default.id)
                }
            } else if (draft.weightText.value.isBlank() && draft.repsText.value.isBlank()) {
                prefillFromLastSet(storedId)
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        draft.selectedExerciseId.value = exercise.id
        draft.weightText.value = ""
        draft.repsText.value = ""
        viewModelScope.launch { prefillFromLastSet(exercise.id) }
    }

    fun setWeight(text: String) {
        draft.weightText.value = text
            .filterIndexed { index, char ->
                char.isDigit() || (char == '.' && text.take(index).none { it == '.' })
            }
            .take(6)
    }

    fun setReps(text: String) {
        draft.repsText.value = text.filter { it.isDigit() }.take(3)
    }

    fun setRestPreset(seconds: Int) {
        if (seconds <= 0) return
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(restPresetSec = seconds))
        }
    }

    /** Adds time to a running rest without logging another set. */
    fun extendRest(seconds: Int) = restTimer.extend(seconds)

    /** Ends the rest early. */
    fun skipRest() = restTimer.stop()

    fun logSet() {
        val exercise = selectedExercise.value ?: return
        val weight = weightText.value.toDoubleOrNull() ?: return
        val reps = repsText.value.toIntOrNull() ?: return
        if (weight <= 0.0 || reps <= 0) return

        val restSeconds = restPresets.value.getOrNull(1) ?: 180
        viewModelScope.launch {
            logMutex.withLock {
                val sessionId = activeSession.value?.id ?: pendingSessionId ?: run {
                    val created = WorkoutSession.create(LOCAL_USER_ID, date)
                    workoutRepository.insertSession(created)
                    pendingSessionId = created.id
                    created.id
                }

                val storedMax = exerciseSets.value
                    .filter { it.exerciseId == exercise.id }
                    .maxOfOrNull { it.setIndex } ?: 0
                val localNext = nextSetIndex[exercise.id] ?: 0
                val setIndex = maxOf(storedMax, localNext) + 1
                nextSetIndex[exercise.id] = setIndex

                workoutRepository.insertSet(
                    WorkoutSet.create(
                        sessionId = sessionId,
                        exerciseId = exercise.id,
                        setIndex = setIndex,
                        weightKg = weight,
                        reps = reps
                    )
                )
                restTimer.start(restSeconds)
            }
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.softDeleteSet(set.id, System.currentTimeMillis())
        }
    }

    private suspend fun prefillFromLastSet(exerciseId: String) {
        val last = workoutRepository.getLatestSetForExercise(exerciseId).first() ?: return
        draft.weightText.value = formatLiftWeight(last.weightKg)
        draft.repsText.value = last.reps.toString()
    }
}

internal fun formatLiftWeight(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
}

internal fun formatRestTime(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return String.format(Locale.US, "%d:%02d", safe / 60, safe % 60)
}

internal fun formatLiftVolume(volume: Double): String =
    String.format(Locale.US, "%,d", volume.roundToInt())
