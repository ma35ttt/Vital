package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.Exercise
import com.vital.data.entity.MuscleGroup
import com.vital.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Library filter chip; [ALL] keeps every muscle group. Labels match the browse screen tabs. */
enum class MuscleFilter(val label: String, val group: MuscleGroup?) {
    ALL("All", null),
    CHEST("Chest", MuscleGroup.CHEST),
    BACK("Back", MuscleGroup.BACK),
    SHOULDERS("Shoulders", MuscleGroup.SHOULDERS),
    ARMS("Arms", MuscleGroup.ARMS),
    LEGS("Legs", MuscleGroup.LEGS),
    CORE("Core", MuscleGroup.CORE),
    CARDIO("Cardio", MuscleGroup.CARDIO)
}

data class ExerciseLibraryState(
    val query: String = "",
    val filter: MuscleFilter = MuscleFilter.ALL,
    val visible: List<Exercise> = emptyList(),
    val totalCount: Int = 0,
    val compoundCount: Int = 0,
    val groupCounts: Map<MuscleGroup, Int> = emptyMap()
) {
    /** "923 movements · 517 compound" for ALL, "LEGS · 306 movements" once a group is picked. */
    val countLabel: String
        get() = if (filter.group == null) {
            "$totalCount movements · $compoundCount compound"
        } else {
            "${filter.group.name} · ${groupCounts[filter.group] ?: 0} movements"
        }
}

/**
 * The exercise library reads the whole catalog once and filters it in memory: search is a
 * name match and the tabs are a muscle-group match, so 923 rows never need a new query.
 */
@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
    private val draft: LiftDraftStore
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(MuscleFilter.ALL)

    private val _selected = MutableStateFlow<Exercise?>(null)
    val selected: StateFlow<Exercise?> = _selected.asStateFlow()

    val state: StateFlow<ExerciseLibraryState> =
        combine(
            exerciseRepository.getAllExercises(),
            query,
            filter
        ) { all, query, filter ->
            val byGroup =
                if (filter.group == null) all else all.filter { it.muscleGroup == filter.group }
            val trimmed = query.trim()
            val visible = if (trimmed.isEmpty()) byGroup
            else byGroup.filter { it.name.contains(trimmed, ignoreCase = true) }

            ExerciseLibraryState(
                query = query,
                filter = filter,
                visible = visible,
                totalCount = all.size,
                compoundCount = all.count { it.isCompound },
                groupCounts = all.groupingBy { it.muscleGroup }.eachCount()
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ExerciseLibraryState()
        )

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: MuscleFilter) {
        filter.value = value
    }

    fun open(exercise: Exercise) {
        _selected.value = exercise
    }

    fun dismiss() {
        _selected.value = null
    }

    /** Hands the exercise to Lift and closes the dialog; Lift prefills weight/reps from history. */
    fun useInLift(exercise: Exercise) {
        draft.selectedExerciseId.value = exercise.id
        draft.weightText.value = ""
        draft.repsText.value = ""
        _selected.value = null
    }
}
