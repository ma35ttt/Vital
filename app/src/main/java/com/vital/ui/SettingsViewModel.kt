package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.AiProvider
import com.vital.data.entity.Settings
import com.vital.data.entity.User
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.UserRepository
import com.vital.data.seed.LOCAL_USER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reads and writes the single local Settings and User rows through Room.
 *
 * There is no auth gate until M6, so every read is against [LOCAL_USER_ID], the row
 * SeedLoader writes on first launch. Writes upsert, so a missing row is created
 * rather than silently dropped by an UPDATE that matches nothing.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val fallback = Settings.create(LOCAL_USER_ID)

    val settings: StateFlow<Settings> = settingsRepository.getSettings(LOCAL_USER_ID)
        .map { it ?: fallback }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), fallback)

    val user: StateFlow<User?> = userRepository.getUser(LOCAL_USER_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setTrueBlack(enabled: Boolean) = updateSettings { it.copy(trueBlack = enabled) }

    fun setAiProvider(provider: AiProvider) = updateSettings { it.copy(aiProvider = provider) }

    fun setKcalTarget(kcal: Int) = updateSettings { it.copy(kcalTarget = kcal) }

    fun setMacroTargets(proteinG: Int, carbsG: Int, fatG: Int) = updateSettings {
        it.copy(proteinTarget = proteinG, carbsTarget = carbsG, fatTarget = fatG)
    }

    fun setBodyWeightKg(kg: Double) = updateSettings { it.copy(bodyWeightKg = kg) }

    fun setRestPresetSec(seconds: Int) = updateSettings { it.copy(restPresetSec = seconds) }

    fun setNotificationsEnabled(enabled: Boolean) =
        updateSettings { it.copy(notificationsEnabled = enabled) }

    fun setMealRemindersEnabled(enabled: Boolean) =
        updateSettings { it.copy(mealRemindersEnabled = enabled) }

    fun setWorkoutRemindersEnabled(enabled: Boolean) =
        updateSettings { it.copy(workoutRemindersEnabled = enabled) }

    fun setWaterRemindersEnabled(enabled: Boolean) =
        updateSettings { it.copy(waterRemindersEnabled = enabled) }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            val existing = userRepository.getUserSync(LOCAL_USER_ID)
            val updated = (existing ?: User(id = LOCAL_USER_ID, name = name, email = ""))
                .copy(name = name, updatedAt = System.currentTimeMillis())
            userRepository.insertUser(updated)
        }
    }

    private fun updateSettings(transform: (Settings) -> Settings) {
        viewModelScope.launch {
            val current = settingsRepository.getSettings(LOCAL_USER_ID).first() ?: fallback
            settingsRepository.insertSettings(
                transform(current).copy(updatedAt = System.currentTimeMillis())
            )
        }
    }
}
