package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import com.vital.ui.theme.VitalColors
import com.vital.ui.components.Period

class MainViewModel @Inject constructor() : ViewModel() {

    // Today screen data
    val remainingKcal = MutableStateFlow(0f)
    val proteinCurrent = MutableStateFlow(0f)
    val carbsCurrent = MutableStateFlow(0f)
    val fatCurrent = MutableStateFlow(0f)
    val proteinTarget = MutableStateFlow(180)
    val carbsTarget = MutableStateFlow(250)
    val fatTarget = MutableStateFlow(85)
    /** Progress screen only; Weeks now reads Room through WeeksViewModel. */
    val weeklyWeightData = MutableStateFlow(emptyList<Float>())
    val weeklyKcalData = MutableStateFlow(emptyList<Float>())
    val weeklyLabels = MutableStateFlow(emptyList<String>())
    val workoutsToday = MutableStateFlow(emptyList<WorkoutCapsule>())
    val cardioToday = MutableStateFlow(emptyList<WorkoutCapsule>())

    // Summary dial data
    val kcalTarget = MutableStateFlow(2500)
    val trueBlack = MutableStateFlow(true)
    val aiProvider = MutableStateFlow("gemini")
    val bodyWeightKg = MutableStateFlow(80f)

    // Progress screen data
    val currentWeight = MutableStateFlow(80f)
    val weightChange = MutableStateFlow(0f)
    val bodyFatPct = MutableStateFlow(15f)
    val progressPhotos = MutableStateFlow<List<com.vital.ui.screens.ProgressPhotoItem>>(emptyList())
    val measurements = MutableStateFlow<List<com.vital.ui.screens.MeasurementItem>>(emptyList())

    // Meals screen data
    val consumedKcal = MutableStateFlow(0f)
    val mealsToday = MutableStateFlow<List<MealCapsule>>(emptyList())

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
            weeklyWeightData.value = listOf(
                0.50f, 0.48f, 0.49f, 0.47f, 0.46f, 0.45f,
                0.44f, 0.43f, 0.42f, 0.41f, 0.40f, 0.39f
            )

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