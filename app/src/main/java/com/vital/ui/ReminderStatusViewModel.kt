package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.repository.MealLogRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.WorkoutRepository
import com.vital.data.seed.LOCAL_USER_ID
import com.vital.domain.Reminders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Drives the quiet dot on the More tab: it is lit while a reminder the user enabled has come
 * round and the thing it points at is still unlogged. The rules live in [Reminders] so they
 * are unit-tested; this class only feeds them today's logs and a once-a-minute clock.
 */
@HiltViewModel
class ReminderStatusViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    mealLogRepository: MealLogRepository,
    workoutRepository: WorkoutRepository
) : ViewModel() {

    private val today: String = LocalDate.now().toString()

    /** Re-emits every minute so the dot can appear as a reminder time passes. */
    private val clock: Flow<LocalTime> = flow {
        while (true) {
            emit(LocalTime.now())
            delay(60_000)
        }
    }

    val moreDotVisible: StateFlow<Boolean> = combine(
        settingsRepository.getSettings(LOCAL_USER_ID),
        mealLogRepository.getMealsForDate(LOCAL_USER_ID, today),
        workoutRepository.getSessionsForDate(LOCAL_USER_ID, today),
        clock
    ) { settings, meals, sessions, now ->
        if (settings == null || !settings.notificationsEnabled) return@combine false

        val mealsDue = Reminders.mealsDue(
            enabled = settings.mealRemindersEnabled,
            times = Reminders.times(settings.mealReminderTimesJson),
            hasMealsToday = meals.isNotEmpty(),
            now = now
        )
        val workoutDue = Reminders.workoutDue(
            enabled = settings.workoutRemindersEnabled,
            days = Reminders.days(settings.workoutReminderDaysJson),
            time = Reminders.time(settings.workoutReminderTime),
            dayOfWeek = Reminders.isoDay(LocalDate.now().dayOfWeek),
            hasWorkoutToday = sessions.isNotEmpty(),
            now = now
        )
        mealsDue || workoutDue
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
