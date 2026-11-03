package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import com.vital.ui.theme.VitalColors
import com.vital.ui.screens.WeeksPeriod
import com.vital.ui.components.Period

class MainViewModel @Inject constructor() : ViewModel() {

    // Weeks screen data
    val weeksPeriod = MutableStateFlow(WeeksPeriod.L)
    val weeklyMacroData = MutableStateFlow(emptyList<Float>())
    val weeklyVolumeData = MutableStateFlow(emptyList<Float>())
    val weeklyWeightData = MutableStateFlow(emptyList<Float>())
    val weeklyPrData = MutableStateFlow(emptyList<Float>())
    val weeklyLabels12 = MutableStateFlow(emptyList<String>())

    // Today screen data
    val remainingKcal = MutableStateFlow(0f)
    val proteinCurrent = MutableStateFlow(0f)
    val carbsCurrent = MutableStateFlow(0f)
    val fatCurrent = MutableStateFlow(0f)
    val proteinTarget = MutableStateFlow(180)
    val carbsTarget = MutableStateFlow(250)
    val fatTarget = MutableStateFlow(85)
    val weeklyKcalData = MutableStateFlow(emptyList<Float>())
    val weeklyLabels = MutableStateFlow(emptyList<String>())
    val workoutsToday = MutableStateFlow(emptyList<WorkoutCapsule>())
    val cardioToday = MutableStateFlow(emptyList<WorkoutCapsule>())

    // Summary dial data
    val kcalTarget = MutableStateFlow(2500)
    val trueBlack = MutableStateFlow(true)
    val aiProvider = MutableStateFlow("gemini")
    val bodyWeightKg = MutableStateFlow(80f)
    val restPresetSec = MutableStateFlow(180)

    // Progress screen data
    val currentWeight = MutableStateFlow(80f)
    val weightChange = MutableStateFlow(0f)
    val bodyFatPct = MutableStateFlow(15f)
    val progressPhotos = MutableStateFlow<List<com.vital.ui.screens.ProgressPhotoItem>>(emptyList())
    val measurements = MutableStateFlow<List<com.vital.ui.screens.MeasurementItem>>(emptyList())

    // Meals screen data
    val consumedKcal = MutableStateFlow(0f)
    val mealsToday = MutableStateFlow<List<MealCapsule>>(emptyList())

    // Lift screen data
    val volumeToday = MutableStateFlow(0f)
    val restTimerActive = MutableStateFlow(false)
    val restTimerRemaining = MutableStateFlow(0)

    // Cardio screen data
    val cardioKcalToday = MutableStateFlow(0f)
    val weeklyCardioData = MutableStateFlow(emptyList<Float>())

    // Coach chat data
    val coachMessages = MutableStateFlow<List<CoachMessage>>(emptyList())
    val coachLoading = MutableStateFlow(false)
    val proteinPercent = MutableStateFlow(0.3f)
    val carbsPercent = MutableStateFlow(0.4f)
    val fatPercent = MutableStateFlow(0.3f)
    val summaryPeriod = MutableStateFlow(Period.M)

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        viewModelScope.launch {
            // Sample data for Weeks screen
            weeklyLabels12.value = com.vital.ui.screens.WeeksScreenPreviewData.weeklyLabels12
            weeklyMacroData.value = com.vital.ui.screens.WeeksScreenPreviewData.weeklyMacro
            weeklyVolumeData.value = com.vital.ui.screens.WeeksScreenPreviewData.weeklyVolume
            weeklyWeightData.value = com.vital.ui.screens.WeeksScreenPreviewData.weeklyWeight
            weeklyPrData.value = com.vital.ui.screens.WeeksScreenPreviewData.weeklyPrs

            // Sample data for Today screen
            remainingKcal.value = 850f
            proteinCurrent.value = 120f
            carbsCurrent.value = 180f
            fatCurrent.value = 55f
            proteinTarget.value = 180
            carbsTarget.value = 250
            fatTarget.value = 85
            weeklyKcalData.value = com.vital.ui.screens.TodayScreenPreviewData.weeklyKcal
            weeklyLabels.value = com.vital.ui.screens.TodayScreenPreviewData.weeklyLabels
            workoutsToday.value = com.vital.ui.screens.TodayScreenPreviewData.sampleWorkouts
            cardioToday.value = com.vital.ui.screens.TodayScreenPreviewData.sampleCardio

            // Sample data for Meals screen
            mealsToday.value = listOf(
                MealCapsule("Breakfast", "520 kcal", Icons.Default.Favorite, VitalColors.orange, listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f), "BREAKFAST"),
                MealCapsule("Lunch", "890 kcal", Icons.Default.Favorite, VitalColors.orange, listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.65f), "LUNCH"),
                MealCapsule("Dinner", "720 kcal", Icons.Default.Favorite, VitalColors.orange, listOf(0.3f, 0.4f, 0.5f, 0.45f, 0.5f), "DINNER"),
                MealCapsule("Snacks", "180 kcal", Icons.Default.Favorite, VitalColors.orange, listOf(0.1f, 0.2f, 0.15f, 0.25f, 0.2f), "SNACK")
            )

            // Sample data for Progress screen
            progressPhotos.value = com.vital.ui.screens.ProgressScreenPreviewData.samplePhotos
            measurements.value = com.vital.ui.screens.ProgressScreenPreviewData.sampleMeasurements
        }
    }

    fun setWeeksPeriod(period: WeeksPeriod) {
        weeksPeriod.value = period
    }
}

data class WorkoutCapsule(
    val name: String,
    val value: String,
    val icon: ImageVector,
    val accentColor: androidx.compose.ui.graphics.Color,
    val sparklineData: List<Float>
)

data class MealCapsule(
    val name: String,
    val value: String,
    val icon: ImageVector,
    val accentColor: androidx.compose.ui.graphics.Color,
    val sparklineData: List<Float>,
    val slot: String = ""
)

data class CoachMessage(
    val text: String,
    val isUser: Boolean
)