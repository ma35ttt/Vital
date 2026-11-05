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
import java.time.Instant
import java.time.ZoneId
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
    val rpeText: StateFlow<String> = draft.rpeText.asStateFlow()

    /** Non-null while a workout started from "New workout" is being walked. */
    val planProgress: StateFlow<PlanProgress?> =
        combine(draft.plan, draft.planIndex) { plan, index ->
            if (plan.isEmpty()) null else PlanProgress(index + 1, plan.size)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Set by the More sheet; Lift opens the New workout sheet when it turns true. */
    val newWorkoutRequested: StateFlow<Boolean> = draft.newWorkoutRequested.asStateFlow()

    private val _newWorkout = MutableStateFlow<NewWorkoutPlan?>(null)

    /** The New workout sheet's contents: null while loading or closed. */
    val newWorkout: StateFlow<NewWorkoutPlan?> = _newWorkout.asStateFlow()

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
        draft.rpeText.value = ""
        // Picking an exercise by hand leaves the planned workout behind.
        draft.plan.value = emptyList()
        draft.planIndex.value = 0
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

    /** RPE is optional; allow one decimal so half-steps still work. */
    fun setRpe(text: String) {
        draft.rpeText.value = text
            .filterIndexed { index, char ->
                char.isDigit() || (char == '.' && text.take(index).none { it == '.' })
            }
            .take(4)
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

        val rpe = rpeText.value.toDoubleOrNull()?.takeIf { it in 1.0..10.0 }
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
                        reps = reps,
                        rpe = rpe
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
        draft.rpeText.value = last.rpe?.let { formatRpe(it) } ?: ""
    }

    // ---- New workout ------------------------------------------------------------------------

    /** Called by the More sheet to ask Lift to open its creator. */
    fun requestNewWorkout() {
        draft.newWorkoutRequested.value = true
    }

    fun consumeNewWorkoutRequest() {
        draft.newWorkoutRequested.value = false
    }

    /**
     * Builds the New workout plan from the most recent session that actually has sets,
     * skipping today's in-progress session. Each exercise keeps the intensity, reps and RPE
     * from its last set so the workout can be repeated in one tap.
     */
    fun loadNewWorkout() {
        _newWorkout.value = null
        viewModelScope.launch {
            val names = exerciseRepository.getAllExercises().first()
                .associate { it.id to it.name }
            // Skip today's in-progress session so "last workout" means the previous one.
            val activeId = workoutRepository.getActiveSession(LOCAL_USER_ID).first()?.id
                ?: pendingSessionId
            val sessions = workoutRepository.getAllSessions(LOCAL_USER_ID).first()

            var source: WorkoutSession? = null
            var sets: List<WorkoutSet> = emptyList()
            for (session in sessions) {
                if (session.id == activeId) continue
                val candidate = workoutRepository.getSetsForSession(session.id).first()
                if (candidate.isNotEmpty()) {
                    source = session
                    sets = candidate
                    break
                }
            }

            _newWorkout.value = NewWorkoutPlan(
                items = if (source == null) emptyList() else buildPlanItems(sets, names),
                ageLabel = source?.let { relativeAgeLabel(it.startedAt) }
            )
        }
    }

    fun dismissNewWorkout() {
        _newWorkout.value = null
    }

    fun updatePlanWeight(index: Int, text: String) =
        updatePlanItem(index, weightText = sanitizeDecimal(text))

    fun updatePlanReps(index: Int, text: String) =
        updatePlanItem(index, repsText = text.filter { it.isDigit() }.take(3))

    fun updatePlanRpe(index: Int, text: String) =
        updatePlanItem(index, rpeText = sanitizeDecimal(text))

    private fun updatePlanItem(
        index: Int,
        weightText: String? = null,
        repsText: String? = null,
        rpeText: String? = null
    ) {
        val plan = _newWorkout.value ?: return
        val item = plan.items.getOrNull(index) ?: return
        val updated = plan.items.toMutableList().also { list ->
            list[index] = item.copy(
                weightText = weightText ?: item.weightText,
                repsText = repsText ?: item.repsText,
                rpeText = rpeText ?: item.rpeText
            )
        }
        _newWorkout.value = plan.copy(items = updated)
    }

    /** Adds an exercise to the plan, prefilled from the last time it was logged. */
    fun addPlanExercise(exercise: Exercise) {
        viewModelScope.launch {
            val last = workoutRepository.getLatestSetForExercise(exercise.id).first()
            val item = WorkoutPlanItem(
                exerciseId = exercise.id,
                name = exercise.name,
                weightText = last?.let { formatLiftWeight(it.weightKg) } ?: "",
                repsText = last?.reps?.toString() ?: "",
                rpeText = last?.rpe?.let { formatRpe(it) } ?: "",
                setCount = 3
            )
            val plan = _newWorkout.value ?: NewWorkoutPlan(emptyList(), null)
            _newWorkout.value = plan.copy(items = plan.items + item)
        }
    }

    /** Prefills the draft from the plan. Nothing is written to Room until a set is logged. */
    fun startWorkout() {
        val items = _newWorkout.value?.items.orEmpty()
        if (items.isEmpty()) {
            _newWorkout.value = null
            return
        }
        draft.plan.value = items.map {
            LiftPlanEntry(it.exerciseId, it.weightText, it.repsText, it.rpeText)
        }
        draft.planIndex.value = 0
        applyPlanEntry(0)
        _newWorkout.value = null
    }

    /** Closes the creator and clears the draft so the workout starts fresh. */
    fun startEmpty() {
        draft.plan.value = emptyList()
        draft.planIndex.value = 0
        draft.weightText.value = ""
        draft.repsText.value = ""
        draft.rpeText.value = ""
        _newWorkout.value = null
    }

    /** Steps to the next planned exercise, prefilling its saved values. */
    fun nextExercise() {
        val plan = draft.plan.value
        if (plan.isEmpty()) return
        val next = draft.planIndex.value + 1
        if (next >= plan.size) {
            draft.plan.value = emptyList()
            draft.planIndex.value = 0
            return
        }
        draft.planIndex.value = next
        applyPlanEntry(next)
    }

    private fun applyPlanEntry(index: Int) {
        val entry = draft.plan.value.getOrNull(index) ?: return
        draft.selectedExerciseId.value = entry.exerciseId
        draft.weightText.value = entry.weightText
        draft.repsText.value = entry.repsText
        draft.rpeText.value = entry.rpeText
    }

    private fun buildPlanItems(
        sets: List<WorkoutSet>,
        names: Map<String, String>
    ): List<WorkoutPlanItem> = sets
        .groupBy { it.exerciseId }
        .map { (exerciseId, group) ->
            val ordered = group.sortedBy { it.setIndex }
            val last = ordered.last()
            WorkoutPlanItem(
                exerciseId = exerciseId,
                name = names[exerciseId] ?: "Exercise",
                weightText = formatLiftWeight(last.weightKg),
                repsText = last.reps.toString(),
                rpeText = last.rpe?.let { formatRpe(it) } ?: "",
                setCount = ordered.size
            )
        }

    private fun sanitizeDecimal(text: String): String = text
        .filterIndexed { index, char ->
            char.isDigit() || (char == '.' && text.take(index).none { it == '.' })
        }
        .take(6)
}

internal fun formatLiftWeight(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
}

/** RPE reads as a whole number when it is one (7), else one decimal (7.5). */
internal fun formatRpe(value: Double): String = formatLiftWeight(value)

/** "Today" / "Yesterday" / "N days ago", from a stored session timestamp. */
internal fun relativeAgeLabel(startedAt: Long): String {
    val started = Instant.ofEpochMilli(startedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val days = java.time.temporal.ChronoUnit.DAYS.between(started, LocalDate.now())
    return when {
        days <= 0L -> "Today"
        days == 1L -> "Yesterday"
        else -> "$days days ago"
    }
}

internal fun formatRestTime(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return String.format(Locale.US, "%d:%02d", safe / 60, safe % 60)
}

internal fun formatLiftVolume(volume: Double): String =
    String.format(Locale.US, "%,d", volume.roundToInt())
