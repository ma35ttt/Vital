package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.OnboardingPreferences
import com.vital.data.entity.GoalType
import com.vital.data.entity.Settings
import com.vital.data.repository.SettingsRepository
import com.vital.data.seed.LOCAL_USER_ID
import com.vital.domain.bodyGoalPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Drives the first-run walk-through. It shows once per install: the initial value comes from
 * [OnboardingPreferences]. The walk-through now collects the body-goal inputs (height, age, sex,
 * cardio, current/goal weight), so the draft lives here and is written to Room when the user taps
 * Get started; Skip leaves the stored profile untouched.
 */
@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _showTutorial = MutableStateFlow(!onboardingPreferences.tutorialSeen())
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    private val fallback = Settings.create(LOCAL_USER_ID)
    private val _draft = MutableStateFlow(fallback)
    val draft: StateFlow<Settings> = _draft.asStateFlow()

    init {
        viewModelScope.launch {
            val current = settingsRepository.getSettings(LOCAL_USER_ID).first() ?: fallback
            // A fresh install has no goal yet. Start from a concrete loss target so the Goal and
            // Numbers pages show real figures instead of dashes; the user can change it freely.
            _draft.value = if (current.goalWeightKg == null) {
                current.copy(
                    goalType = GoalType.LOSE,
                    goalWeightKg = (current.bodyWeightKg - 5.0).coerceAtLeast(30.0)
                )
            } else {
                current
            }
        }
    }

    fun setDraft(settings: Settings) {
        _draft.value = settings
    }

    /** Skip: dismiss the walk-through without touching the stored profile. */
    fun skip() {
        onboardingPreferences.markTutorialSeen()
        _showTutorial.value = false
    }

    /** Get started: persist the profile and the derived target/macros, then dismiss. */
    fun finish() {
        viewModelScope.launch {
            val draft = _draft.value
            val plan = bodyGoalPlan(draft)
            settingsRepository.insertSettings(
                draft.copy(
                    kcalTarget = plan.targetKcal,
                    proteinTarget = plan.macros.proteinG.roundToInt(),
                    carbsTarget = plan.macros.carbsG.roundToInt(),
                    fatTarget = plan.macros.fatG.roundToInt(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            onboardingPreferences.markTutorialSeen()
            _showTutorial.value = false
        }
    }
}