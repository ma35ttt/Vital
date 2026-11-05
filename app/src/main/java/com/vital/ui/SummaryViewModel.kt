package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.CardioSession
import com.vital.data.entity.CardioType
import com.vital.data.entity.Exercise
import com.vital.data.entity.MealLogEntry
import com.vital.data.entity.MealSlot
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

data class SummaryMeal(
    val slot: MealSlot,
    val at: Long,
    val kcal: Double
)

data class SummaryLift(
    val id: String,
    val title: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMin: Int,
    val volumeKg: Double,
    val setCount: Int
)

data class SummaryCardio(
    val id: String,
    val type: CardioType,
    val startedAt: Long,
    val durationSec: Int,
    val distanceKm: Double?,
    val kcal: Double
) {
    val durationMin: Int get() = durationSec / 60
}

data class DaySummary(
    val date: LocalDate,
    val eatenKcal: Double,
    val targetKcal: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val proteinTarget: Int,
    val carbsTarget: Int,
    val fatTarget: Int,
    val meals: List<SummaryMeal>,
    val lifts: List<SummaryLift>,
    val cardios: List<SummaryCardio>,
    val bodyWeightKg: Double
) {
    val leftKcal: Double get() = targetKcal - eatenKcal
    val mealCount: Int get() = meals.size
    val liftMinutes: Int get() = lifts.sumOf { it.durationMin }
    val cardioMinutes: Int get() = cardios.sumOf { it.durationMin }
    val sessionCount: Int get() = lifts.size + cardios.size
    val hasAnything: Boolean get() = meals.isNotEmpty() || lifts.isNotEmpty() || cardios.isNotEmpty()

    companion object {
        fun empty(settings: Settings = Settings.create(LOCAL_USER_ID)): DaySummary = DaySummary(
            date = LocalDate.now(),
            eatenKcal = 0.0,
            targetKcal = settings.kcalTarget,
            protein = 0.0,
            carbs = 0.0,
            fat = 0.0,
            proteinTarget = settings.proteinTarget,
            carbsTarget = settings.carbsTarget,
            fatTarget = settings.fatTarget,
            meals = emptyList(),
            lifts = emptyList(),
            cardios = emptyList(),
            bodyWeightKg = settings.bodyWeightKg
        )
    }
}

private data class RawDay(
    val meals: List<MealLogEntry>,
    val sessions: List<WorkoutSession>,
    val sets: List<WorkoutSet>,
    val cardio: List<CardioSession>
)

/**
 * Summary aggregates today's real Room rows. It is deliberately separate from
 * MainViewModel, which still feeds the sample data on the old screens.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val workoutRepository: WorkoutRepository,
    private val cardioRepository: CardioRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val date: String = LocalDate.now().toString()
    private val fallback = Settings.create(LOCAL_USER_ID)

    private val meals: Flow<List<MealLogEntry>> =
        mealLogRepository.getMealsForDate(LOCAL_USER_ID, date)

    private val sessions: Flow<List<WorkoutSession>> =
        workoutRepository.getSessionsForDate(LOCAL_USER_ID, date)

    /** One Flow per session, combined so a logged set updates the day's volume and arcs. */
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
        cardioRepository.getCardioForDate(LOCAL_USER_ID, date)

    private val settings: Flow<Settings> =
        settingsRepository.getSettings(LOCAL_USER_ID).map { it ?: fallback }

    private val exercises: Flow<List<Exercise>> = exerciseRepository.getAllExercises()

    val summary: StateFlow<DaySummary> =
        combine(meals, sessions, sets, cardio) { m, s, st, c -> RawDay(m, s, st, c) }
            .combine(settings) { raw, current -> raw to current }
            .combine(exercises) { pair, allExercises ->
                buildDay(pair.first, pair.second, allExercises)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                DaySummary.empty(fallback)
            )

    private fun buildDay(
        raw: RawDay,
        settings: Settings,
        exercises: List<Exercise>
    ): DaySummary {
        val names = exercises.associate { it.id to it.name }

        // One dot per meal slot: the first food logged for that meal sets its place on the ring.
        val mealRows = raw.meals
            .groupBy { it.mealSlot }
            .map { (slot, entries) ->
                SummaryMeal(
                    slot = slot,
                    at = entries.minOf { it.updatedAt },
                    kcal = entries.sumOf { it.kcal }
                )
            }
            .sortedBy { it.at }

        val setsBySession = raw.sets.groupBy { it.sessionId }
        val lifts = raw.sessions.map { session ->
            val sessionSets = setsBySession[session.id].orEmpty()
            val lastSetAt = sessionSets.maxOfOrNull { it.updatedAt }
            val end = session.endedAt ?: lastSetAt ?: session.startedAt
            val safeEnd = end.coerceAtLeast(session.startedAt)
            val primaryId = sessionSets
                .groupBy { it.exerciseId }
                .maxByOrNull { it.value.size }
                ?.key
            SummaryLift(
                id = session.id,
                title = names[primaryId]
                    ?.lowercase()
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Lift session",
                startedAt = session.startedAt,
                endedAt = safeEnd,
                durationMin = ((safeEnd - session.startedAt) / 60_000.0)
                    .roundToInt()
                    .coerceAtLeast(if (sessionSets.isEmpty()) 0 else 1),
                volumeKg = sessionSets.sumOf { it.weightKg * it.reps },
                setCount = sessionSets.size
            )
        }.sortedBy { it.startedAt }

        val cardios = raw.cardio.map { session ->
            SummaryCardio(
                id = session.id,
                type = session.type,
                startedAt = session.updatedAt - session.durationSec * 1_000L,
                durationSec = session.durationSec,
                distanceKm = session.distanceKm,
                kcal = session.kcal
            )
        }.sortedBy { it.startedAt }

        return DaySummary(
            date = LocalDate.parse(date),
            eatenKcal = raw.meals.sumOf { it.kcal },
            targetKcal = settings.kcalTarget,
            protein = raw.meals.sumOf { it.proteinG },
            carbs = raw.meals.sumOf { it.carbsG },
            fat = raw.meals.sumOf { it.fatG },
            proteinTarget = settings.proteinTarget,
            carbsTarget = settings.carbsTarget,
            fatTarget = settings.fatTarget,
            meals = mealRows,
            lifts = lifts,
            cardios = cardios,
            bodyWeightKg = settings.bodyWeightKg
        )
    }
}
