package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.Exercise
import com.vital.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LiveIslandState(
    val restRemaining: Int = 0,
    val restTotal: Int = 0,
    val exerciseName: String? = null
) {
    val isActive: Boolean get() = restRemaining > 0
    val progress: Float
        get() = if (restTotal <= 0) 0f else (restRemaining.toFloat() / restTotal).coerceIn(0f, 1f)
}

/**
 * Feeds the rest-timer surface in the app shell (the divider line above the bottom nav). It
 * reads the same singletons as Lift, so the line keeps advancing on every tab and survives
 * leaving the Lift screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LiveIslandViewModel @Inject constructor(
    private val restTimer: RestTimerController,
    private val draft: LiftDraftStore,
    exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val exerciseName: StateFlow<String?> =
        combine(exerciseRepository.getAllExercises(), draft.selectedExerciseId) { list, id ->
            nameOf(list, id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val state: StateFlow<LiveIslandState> =
        combine(restTimer.remainingSeconds, restTimer.totalSeconds, exerciseName) { left, total, name ->
            LiveIslandState(restRemaining = left, restTotal = total, exerciseName = name)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LiveIslandState()
        )

    /** Same fallback as Lift: the drafted exercise, else Bench Press, else the first row. */
    private fun nameOf(list: List<Exercise>, id: String?): String? =
        list.firstOrNull { it.id == id }?.name
            ?: list.firstOrNull { it.name == "Bench Press" }?.name
            ?: list.firstOrNull()?.name
}
