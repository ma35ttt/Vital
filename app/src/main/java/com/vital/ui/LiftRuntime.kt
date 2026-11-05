package com.vital.ui

import com.vital.service.RestTimerRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One exercise in a planned workout, with the intensity, reps and RPE carried over from
 * the user's last time doing it. Held in the draft until each entry is applied.
 */
data class LiftPlanEntry(
    val exerciseId: String,
    val weightText: String,
    val repsText: String,
    val rpeText: String
)

/** Position within the planned workout, for the "Next exercise" control. */
data class PlanProgress(val position: Int, val total: Int)

/** One row of the New workout sheet: an exercise plus its prefilled values. */
data class WorkoutPlanItem(
    val exerciseId: String,
    val name: String,
    val weightText: String,
    val repsText: String,
    val rpeText: String,
    val setCount: Int
)

/** The New workout sheet's contents: the prefilled exercises and how old the source is. */
data class NewWorkoutPlan(
    val items: List<WorkoutPlanItem>,
    val ageLabel: String?
)

/**
 * The in-progress Lift draft. Lift's nav entry (and therefore its ViewModel) is destroyed
 * when the user switches tabs, so the selected exercise and the fields live here until
 * the app process dies. Logged sets are in Room.
 */
@Singleton
class LiftDraftStore @Inject constructor() {
    val selectedExerciseId = MutableStateFlow<String?>(null)
    val weightText = MutableStateFlow("")
    val repsText = MutableStateFlow("")
    val rpeText = MutableStateFlow("")

    /** A workout started from "New workout"; Lift walks it with the Next exercise control. */
    val plan = MutableStateFlow<List<LiftPlanEntry>>(emptyList())
    val planIndex = MutableStateFlow(0)

    /** Set from the More sheet so Lift can open its creator on arrival. */
    val newWorkoutRequested = MutableStateFlow(false)
}

/**
 * Rest timer that keeps running while the Lift tab is off screen. A deadline is stored
 * instead of counting ticks, so a throttled or paused process still reports the right
 * number when it comes back. It resets when the app process is recreated.
 */
@Singleton
class RestTimerController @Inject constructor(
    private val relay: RestTimerRelay
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    /** The running rest's current length; grows with [extend] so the ring stays proportional. */
    private val _totalSeconds = MutableStateFlow(0)
    val totalSeconds: StateFlow<Int> = _totalSeconds.asStateFlow()

    private var deadlineMillis = 0L
    private var tickJob: Job? = null

    fun start(totalSeconds: Int) {
        tickJob?.cancel()
        if (totalSeconds <= 0) {
            _remainingSeconds.value = 0
            _totalSeconds.value = 0
            relay.cancel()
            return
        }
        _totalSeconds.value = totalSeconds
        deadlineMillis = System.currentTimeMillis() + totalSeconds * 1_000L
        _remainingSeconds.value = totalSeconds
        relay.start(totalSeconds)
        tickJob = scope.launch {
            while (isActive) {
                val left = ((deadlineMillis - System.currentTimeMillis() + 999L) / 1_000L)
                    .toInt()
                    .coerceAtLeast(0)
                _remainingSeconds.value = left
                if (left <= 0) break
                delay(250L)
            }
        }
    }

    /** Adds time to a running rest; a finished timer stays finished. */
    fun extend(seconds: Int) {
        if (seconds <= 0 || _remainingSeconds.value <= 0) return
        deadlineMillis += seconds * 1_000L
        _totalSeconds.value += seconds
        _remainingSeconds.value =
            ((deadlineMillis - System.currentTimeMillis() + 999L) / 1_000L).toInt()
                .coerceAtLeast(0)
        relay.start(_remainingSeconds.value, _totalSeconds.value)
    }

    /** Skips the rest early. */
    fun stop() {
        tickJob?.cancel()
        tickJob = null
        deadlineMillis = 0L
        _remainingSeconds.value = 0
        _totalSeconds.value = 0
        relay.cancel()
    }
}
