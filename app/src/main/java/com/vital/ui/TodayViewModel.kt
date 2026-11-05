package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.CardioSession
import com.vital.data.entity.CardioType
import com.vital.data.entity.Exercise
import com.vital.data.entity.MealLogEntry
import com.vital.data.entity.Settings
import com.vital.data.entity.WorkoutSession
import com.vital.data.entity.WorkoutSet
import com.vital.data.repository.CardioRepository
import com.vital.data.repository.ExerciseRepository
import com.vital.data.repository.MealLogRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.WorkoutRepository
import com.vital.data.seed.LOCAL_USER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

data class TodayLift(
    val id: String,
    val title: String,
    val durationMin: Int
)

data class TodayCardio(
    val id: String,
    val type: CardioType,
    val durationSec: Int,
    val distanceKm: Double?
) {
    val durationMin: Int get() = durationSec / 60
}

data class WeekKcalPoint(
    val date: LocalDate,
    val kcal: Double
)

data class TodayState(
    val date: LocalDate,
    val eatenKcal: Double,
    val targetKcal: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val proteinTarget: Int,
    val carbsTarget: Int,
    val fatTarget: Int,
    val lifts: List<TodayLift>,
    val cardios: List<TodayCardio>,
    val week: List<WeekKcalPoint>
) {
    val leftKcal: Double get() = (targetKcal - eatenKcal).coerceAtLeast(0.0)
    val eatenPct: Int
        get() = if (targetKcal <= 0) 0 else (eatenKcal / targetKcal * 100).roundToInt()
    val weekTotal: Double get() = week.sumOf { it.kcal }
    val workoutCount: Int get() = lifts.size + cardios.size

    companion object {
        fun empty(settings: Settings = Settings.create(LOCAL_USER_ID)): TodayState =
            TodayState(
                date = LocalDate.now(),
                eatenKcal = 0.0,
                targetKcal = settings.kcalTarget,
                protein = 0.0,
                carbs = 0.0,
                fat = 0.0,
                proteinTarget = settings.proteinTarget,
                carbsTarget = settings.carbsTarget,
                fatTarget = settings.fatTarget,
                lifts = emptyList(),
                cardios = emptyList(),
                week = emptyList()
            )
    }
}

private data class TodayRaw(
    val meals: List<MealLogEntry>,
    val sessions: List<WorkoutSession>,
    val sets: List<WorkoutSet>,
    val cardio: List<CardioSession>,
    val weekMeals: List<MealLogEntry>
)

/** Today is the dashboard: real Room totals for the day plus the trailing week. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val workoutRepository: WorkoutRepository,
    private val cardioRepository: CardioRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val date: LocalDate = LocalDate.now()
    private val today: String = date.toString()
    private val weekStart: String = date.minusDays(6).toString()
    private val fallback = Settings.create(LOCAL_USER_ID)

    private val meals: Flow<List<MealLogEntry>> =
        mealLogRepository.getMealsForDate(LOCAL_USER_ID, today)

    private val weekMeals: Flow<List<MealLogEntry>> =
        mealLogRepository.getMealsForDateRange(LOCAL_USER_ID, weekStart, today)

    private val sessions: Flow<List<WorkoutSession>> =
        workoutRepository.getSessionsForDate(LOCAL_USER_ID, today)

    private val sets: Flow<List<WorkoutSet>> = sessions.flatMapLatest { list ->
        if (list.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(list.map { workoutRepository.getSetsForSession(it.id) }) { perSession ->
                perSession.flatMap { it.toList() }
            }
        }
    }

    private val cardio: Flow<List<CardioSession>> =
        cardioRepository.getCardioForDate(LOCAL_USER_ID, today)

    private val settings: Flow<Settings> =
        settingsRepository.getSettings(LOCAL_USER_ID).map { it ?: fallback }

    private val exercises: Flow<List<Exercise>> = exerciseRepository.getAllExercises()

    val state: StateFlow<TodayState> =
        combine(meals, sessions, sets, cardio, weekMeals) { m, s, st, c, w ->
            TodayRaw(m, s, st, c, w)
        }
            .combine(settings) { raw, current -> raw to current }
            .combine(exercises) { pair, allExercises ->
                build(pair.first, pair.second, allExercises)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                TodayState.empty(fallback)
            )

    private fun build(
        raw: TodayRaw,
        settings: Settings,
        exercises: List<Exercise>
    ): TodayState {
        val names = exercises.associate { it.id to it.name }
        val setsBySession = raw.sets.groupBy { it.sessionId }

        val lifts = raw.sessions.map { session ->
            val sessionSets = setsBySession[session.id].orEmpty()
            val lastSetAt = sessionSets.maxOfOrNull { it.updatedAt }
            val end = (session.endedAt ?: lastSetAt ?: session.startedAt)
                .coerceAtLeast(session.startedAt)
            val primaryId = sessionSets
                .groupBy { it.exerciseId }
                .maxByOrNull { it.value.size }
                ?.key
            TodayLift(
                id = session.id,
                title = names[primaryId]
                    ?.lowercase()
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Lift session",
                durationMin = ((end - session.startedAt) / 60_000.0)
                    .roundToInt()
                    .coerceAtLeast(if (sessionSets.isEmpty()) 0 else 1)
            )
        }.sortedBy { it.durationMin }

        val cardios = raw.cardio.map { session ->
            TodayCardio(
                id = session.id,
                type = session.type,
                durationSec = session.durationSec,
                distanceKm = session.distanceKm
            )
        }

        val byDay = raw.weekMeals.groupBy { it.date }
        val week = (0..6).map { offset ->
            val day = date.minusDays(6L - offset)
            WeekKcalPoint(
                date = day,
                kcal = byDay[day.toString()].orEmpty().sumOf { it.kcal }
            )
        }

        return TodayState(
            date = date,
            eatenKcal = raw.meals.sumOf { it.kcal },
            targetKcal = settings.kcalTarget,
            protein = raw.meals.sumOf { it.proteinG },
            carbs = raw.meals.sumOf { it.carbsG },
            fat = raw.meals.sumOf { it.fatG },
            proteinTarget = settings.proteinTarget,
            carbsTarget = settings.carbsTarget,
            fatTarget = settings.fatTarget,
            lifts = lifts,
            cardios = cardios,
            week = week
        )
    }
}
