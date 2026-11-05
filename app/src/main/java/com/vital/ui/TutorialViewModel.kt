package com.vital.ui

import androidx.lifecycle.ViewModel
import com.vital.data.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Drives the first-run walk-through. It shows once per install: the initial value comes from
 * [OnboardingPreferences], and finishing or skipping writes the flag and hides it.
 */
@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _showTutorial = MutableStateFlow(!onboardingPreferences.tutorialSeen())
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    fun finish() {
        onboardingPreferences.markTutorialSeen()
        _showTutorial.value = false
    }
}
