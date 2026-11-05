package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.BodyMetrics
import com.vital.data.entity.CardioSession
import com.vital.data.entity.Exercise
import com.vital.data.entity.MealLogEntry
import com.vital.data.entity.Settings
import com.vital.data.entity.WorkoutSession
import com.vital.data.entity.WorkoutSet
import com.vital.data.repository.BodyMetricsRepository
import com.vital.data.repository.CardioRepository
import com.vital.data.repository.ExerciseRepository
import com.vital.data.repository.MealLogRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.WorkoutRepository
import com.vital.data.seed.LOCAL_USER_ID
import com.vital.domain.DayRecord
import com.vital.domain.OneRepMax
import com.vital.domain.WeeksSnapshot
import com.vital.domain.weeksSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/** The Weeks tab's three windows. L is the hero on first open. */
enum class WeeksPeriod(val label: String, val days: Int, val bars: Int) {
    L("12W", 84, 12),
    M("4W", 28, 4),
    H("7D", 7, 7)
}

data class WeeksState(
    val period: WeeksPeriod,
    val snapshot: WeeksSnapshot,
    val bodyWeightKg: Double?,
    val weightSeries: List<Float>,
    val volumeKg: Double,
    val volumeSeries: List<Float>,
    val benchPrKg: Double?,
    val prSeries: List<Float>,
    val hasData: Boolean
) {
    val volumeTonnes: Double get() = volumeKg / 1_000.0
    val adherencePct: Int get() = snapshot.adherencePct
    val trackedDays: Int get() = snapshot.trackedDays

    companion object {
        fun empty(period: WeeksPeriod, settings: Settings): WeeksState = WeeksState(
            period = period,
            snapshot = WeeksSnapshot(period.days, 0, 0, 0, 0, emptyList()),
            bodyWeightKg = settings.bodyWeightKg,
            weightSeries = emptyList(),
            volumeKg = 0.0,
            volumeSeries = emptyList(),
            benchPrKg = null,
            prSeries = emptyList(),
            hasData = false
        )
    }
}

private data class WeeksRaw(
    val meals: List<MealLogEntry>,
    val sessions: List<WorkoutSession>,
    val sets: List<WorkoutSet>,
    val cardio: List<CardioSession>,
    val settings: Settings,
    val metrics: List<BodyMetrics>,
    val exercises: List<Exercise>
)

/**
 * Weeks aggregates the trailing 84 days of real logs. The window is loaded once and the
 * period tabs only re-slice it, so switching 12W / 4W / 7D never touches the database again.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeeksViewModel @Inject constructor(
    mealLogRepository: MealLogRepository,
    workoutRepository: WorkoutRepository,
    cardioRepository: CardioRepository,
    bodyMetricsRepository: BodyMetricsRepository,
    exerciseRepository: ExerciseRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val windowStart: LocalDate = today.minusDays((WeeksPeriod.L.days - 1).toLong())
    private val fallback = Settings.create(LOCAL_USER_ID)

    private val _period = MutableStateFlow(WeeksPeriod.L)
    val period: StateFlow<WeeksPeriod> = _period.asStateFlow()

    fun setPeriod(period: WeeksPeriod) {
        _period.value = period
    }

    private val meals: Flow<List<MealLogEntry>> = mealLogRepository.getMealsForDateRange(
        LOCAL_USER_ID,
        windowStart.toString(),
        today.toString()
    )

    private val sessions: Flow<List<WorkoutSession>> = workoutRepository.getSessionsForRange(
        LOCAL_USER_ID,
        windowStart.toString(),
        today.toString()
    )

    private val sets: Flow<List<WorkoutSet>> = sessions.flatMapLatest { list ->
        if (list.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(list.map { workoutRepository.getSetsForSession(it.id) }) { perSession ->
                perSession.flatMap { it.toList() }
            }
        }
    }

    private val cardio: Flow<List<CardioSession>> = cardioRepository.getCardioForRange(
        LOCAL_USER_ID,
        windowStart.toString(),
        today.toString()
    )

    private val settings: Flow<Settings> =
        settingsRepository.getSettings(LOCAL_USER_ID).map { it ?: fallback }

    private val metrics: Flow<List<BodyMetrics>> =
        bodyMetricsRepository.getBodyMetricsForRange(
            LOCAL_USER_ID,
            windowStart.toString(),
            today.toString()
        )

    private val exercises: Flow<List<Exercise>> = exerciseRepository.getAllExercises()

    private val raw: Flow<WeeksRaw> =
        combine(meals, sessions, sets, cardio) { m, s, st, c -> WeeksRaw(m, s, st, c, fallback, emptyList(), emptyList()) }
            .combine(settings) { r, set -> r.copy(settings = set) }
            .combine(metrics) { r, met -> r.copy(metrics = met) }
            .combine(exercises) { r, ex -> r.copy(exercises = ex) }

    val state: StateFlow<WeeksState> =
        combine(raw, _period) { r, p -> build(r, p) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                WeeksState.empty(WeeksPeriod.L, fallback)
            )

    private fun build(raw: WeeksRaw, period: WeeksPeriod): WeeksState {
        val start = today.minusDays((period.days - 1).toLong())
        val windowDays = (0 until period.days).map { start.plusDays(it.toLong()) }

        val mealsByDate = raw.meals.groupBy { it.date }
        val liftedDates = raw.sessions.map { it.date }.toSet()
        val cardioDates = raw.cardio.map { it.date }.toSet()

        val records = windowDays.map { date ->
            val dayMeals = mealsByDate[date.toString()].orEmpty()
            DayRecord(
                date = date,
                kcal = dayMeals.sumOf { it.kcal },
                protein = dayMeals.sumOf { it.proteinG },
                carbs = dayMeals.sumOf { it.carbsG },
                fat = dayMeals.sumOf { it.fatG },
                lifted = date.toString() in liftedDates,
                cardio = date.toString() in cardioDates
            )
        }

        val snapshot = weeksSnapshot(
            records = records,
            today = today,
            periodDays = period.days,
            barCount = period.bars,
            proteinTarget = raw.settings.proteinTarget,
            carbsTarget = raw.settings.carbsTarget,
            fatTarget = raw.settings.fatTarget
        )

        val sortedMetrics = raw.metrics.sortedBy { it.date }
        val bodyWeight = sortedMetrics.lastOrNull()?.weightKg ?: raw.settings.bodyWeightKg
        val weightSeries = sortedMetrics
            .takeLast(8)
            .map { it.weightKg.toFloat() }

        val setsByDate = setsBySessionDate(raw.sets, raw.sessions)
        val windowSets = raw.sets.filter { set ->
            val date = setsByDate[set.sessionId] ?: return@filter false
            !date.isBefore(start) && !date.isAfter(today)
        }

        // Six trailing weeks for the sparkline, oldest first.
        val weekStarts = (5 downTo 0).map { weeksAgo ->
            today.minusDays((weeksAgo * 7L) + (today.dayOfWeek.value - 1).toLong())
        }
        fun weekIndexOf(date: LocalDate): Int =
            weekStarts.indexOfLast { !date.isBefore(it) }.coerceIn(0, weekStarts.size - 1)

        val volumeAll = weekStarts.mapIndexed { index, weekStart ->
            val weekEnd = if (index == weekStarts.lastIndex) today else weekStarts[index + 1].minusDays(1)
            raw.sets.filter { set ->
                val date = setsByDate[set.sessionId] ?: return@filter false
                !date.isBefore(weekStart) && !date.isAfter(weekEnd)
            }.sumOf { it.weightKg * it.reps }
        }
        val volumeSeries = volumeAll.map { it.toFloat() }
        val volume30d = raw.sets.filter { set ->
            val date = setsByDate[set.sessionId] ?: return@filter false
            !date.isBefore(today.minusDays(29)) && !date.isAfter(today)
        }.sumOf { it.weightKg * it.reps }

        val benchId = raw.exercises.firstOrNull { it.name.equals("Bench Press", ignoreCase = true) }?.id
        val benchSets = if (benchId == null) emptyList() else windowSets.filter { it.exerciseId == benchId }
        val benchPr = benchSets.maxOfOrNull { OneRepMax.epley(it.weightKg, it.reps) }
        val prSeries = weekStarts.mapIndexed { index, weekStart ->
            val weekEnd = if (index == weekStarts.lastIndex) today else weekStarts[index + 1].minusDays(1)
            benchSets.filter { set ->
                val date = setsByDate[set.sessionId] ?: return@filter false
                !date.isBefore(weekStart) && !date.isAfter(weekEnd)
            }.maxOfOrNull { OneRepMax.epley(it.weightKg, it.reps) }?.toFloat() ?: 0f
        }

        return WeeksState(
            period = period,
            snapshot = snapshot,
            bodyWeightKg = bodyWeight,
            weightSeries = weightSeries,
            volumeKg = volume30d,
            volumeSeries = volumeSeries,
            benchPrKg = benchPr,
            prSeries = prSeries,
            hasData = raw.meals.isNotEmpty() || raw.sessions.isNotEmpty() || raw.cardio.isNotEmpty()
        )
    }

    private fun setsBySessionDate(
        sets: List<WorkoutSet>,
        sessions: List<WorkoutSession>
    ): Map<String, LocalDate> {
        val dates = sessions.associate { it.id to LocalDate.parse(it.date) }
        return sets.mapNotNull { set -> dates[set.sessionId]?.let { set.sessionId to it } }.toMap()
    }
}
