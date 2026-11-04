package com.vital.ui

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
 * The in-progress Lift draft. Lift's nav entry (and therefore its ViewModel) is destroyed
 * when the user switches tabs, so the selected exercise and the two fields live here until
 * the app process dies. Logged sets are in Room.
 */
@Singleton
class LiftDraftStore @Inject constructor() {
    val selectedExerciseId = MutableStateFlow<String?>(null)
    val weightText = MutableStateFlow("")
    val repsText = MutableStateFlow("")
}

/**
 * Rest timer that keeps running while the Lift tab is off screen. A deadline is stored
 * instead of counting ticks, so a throttled or paused process still reports the right
 * number when it comes back. It resets when the app process is recreated.
 */
@Singleton
class RestTimerController @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private var deadlineMillis = 0L
    private var tickJob: Job? = null

    fun start(totalSeconds: Int) {
        tickJob?.cancel()
        if (totalSeconds <= 0) {
            _remainingSeconds.value = 0
            return
        }
        deadlineMillis = System.currentTimeMillis() + totalSeconds * 1_000L
        _remainingSeconds.value = totalSeconds
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
}
