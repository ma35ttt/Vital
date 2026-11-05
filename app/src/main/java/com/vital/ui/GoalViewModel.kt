package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.Settings
import com.vital.data.repository.SettingsRepository
import com.vital.data.seed.LOCAL_USER_ID
import com.vital.domain.bodyGoalPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Reads and writes the body-goal settings. Saving doesn't just store the profile: it also
 * applies the resulting target and macro split to [Settings], so the rest of the app (Today,
 * Meals, the rings) follows the goal the user just set.
 */
@HiltViewModel
class GoalViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val fallback = Settings.create(LOCAL_USER_ID)

    val settings: StateFlow<Settings> = settingsRepository.getSettings(LOCAL_USER_ID)
        .map { it ?: fallback }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), fallback)

    /**
     * Writes only the goal fields onto the current row, then recomputes and stores the daily
     * kcal and macro targets. Reading first keeps unrelated settings (theme, reminders) safe.
     */
    fun saveGoal(draft: Settings) {
        viewModelScope.launch {
            val current = settingsRepository.getSettings(LOCAL_USER_ID).first() ?: fallback
            val saved = current.copy(
                bodyWeightKg = draft.bodyWeightKg,
                heightCm = draft.heightCm,
                age = draft.age,
                sex = draft.sex,
                activityLevel = draft.activityLevel,
                goalType = draft.goalType,
                goalWeightKg = draft.goalWeightKg,
                goalRateKgPerWeek = draft.goalRateKgPerWeek,
                updatedAt = System.currentTimeMillis()
            )
            val plan = bodyGoalPlan(saved)
            settingsRepository.insertSettings(
                saved.copy(
                    kcalTarget = plan.targetKcal,
                    proteinTarget = plan.macros.proteinG.roundToInt(),
                    carbsTarget = plan.macros.carbsG.roundToInt(),
                    fatTarget = plan.macros.fatG.roundToInt()
                )
            )
        }
    }
}