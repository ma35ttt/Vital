package com.vital.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.SummaryDial
import com.vital.ui.components.Tag
import com.vital.ui.components.Sparkline
import com.vital.ui.WorkoutCapsule
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

    val remainingKcal by viewModel.remainingKcal.collectAsStateWithLifecycle(0f)
    val proteinCurrent by viewModel.proteinCurrent.collectAsStateWithLifecycle(0f)
    val carbsCurrent by viewModel.carbsCurrent.collectAsStateWithLifecycle(0f)
    val fatCurrent by viewModel.fatCurrent.collectAsStateWithLifecycle(0f)
    val proteinTarget by viewModel.proteinTarget.collectAsStateWithLifecycle(180)
    val carbsTarget by viewModel.carbsTarget.collectAsStateWithLifecycle(250)
    val fatTarget by viewModel.fatTarget.collectAsStateWithLifecycle(85)
    val weeklyKcalData by viewModel.weeklyKcalData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels.collectAsStateWithLifecycle(emptyList())
    val workoutsToday by viewModel.workoutsToday.collectAsStateWithLifecycle(emptyList())
    val cardioToday by viewModel.cardioToday.collectAsStateWithLifecycle(emptyList())

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VitalColors.Background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tag(text = dayTag, backgroundColor = VitalColors.AccentTDY)
                Text(
                    text = "TODAY",
                    color = VitalColors.PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Tag(text = dateTag, backgroundColor = VitalColors.AccentTDY)
            }

            // Hero Card on Accent Fill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(340.dp),
                color = VitalColors.AccentTDY,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: label and "Now"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Tag(
                            text = "KCAL REMAINING",
                            backgroundColor = Color.Black.copy(alpha = 0.3f),
                            textColor = Color.White
                        )
                        Text(
                            text = "NOW",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Big Kcal Display
                    BigKcalDisplay(
                        remaining = remainingKcal,
                        accentColor = Color.White
                    )

                    // Macro Row
                    MacroRow(
                        protein = proteinCurrent,
                        carbs = carbsCurrent,
                        fat = fatCurrent,
                        proteinTarget = proteinTarget,
                        carbsTarget = carbsTarget,
                        fatTarget = fatTarget
                    )

                    // Hero Line Graph (inset grey panel)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 8.dp),
                        color = VitalColors.InsetGreyPanel,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        HeroLineGraph(
                            modifier = Modifier.fillMaxSize(),
                            dataPoints = weeklyKcalData,
                            labels = weeklyLabels,
                            accentColor = Color.White,
                            showTooltip = true,
                            tooltipIndex = weeklyKcalData.lastIndex,
                            tooltipValue = weeklyKcalData.lastOrNull() ?: 0f
                        )
                    }
                }
            }

            // WORKOUTS Section
            SectionHeader(
                title = "WORKOUTS",
                accentColor = VitalColors.AccentTDY,
                showMoreAction = onShowSummary
            )

            // Workout capsules
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                items(workoutsToday) { workout ->
                    capsuleRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(workout.icon, "", tint = workout.accentColor) },
                        label = workout.name,
                        value = workout.value,
                        sparklineData = workout.sparklineData,
                        accentColor = workout.accentColor
                    )
                }

                // Cardio capsules
                items(cardioToday) { cardio ->
                    capsuleRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(cardio.icon, "", tint = cardio.accentColor) },
                        label = cardio.name,
                        value = cardio.value,
                        sparklineData = cardio.sparklineData,
                        accentColor = cardio.accentColor
                    )
                }

                // Empty state
                if (workoutsToday.isEmpty() && cardioToday.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO WORKOUTS LOGGED TODAY",
                                color = VitalColors.MutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preview data for testing
object TodayScreenPreviewData {
    val sampleWorkouts = listOf(
        WorkoutCapsule("Strength", "45 min", Icons.Default.LocalFireDepartment, VitalColors.AccentLFT, listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f)),
        WorkoutCapsule("Upper Body", "38 min", Icons.Default.DirectionsRun, VitalColors.AccentLFT, listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.65f))
    )
    val sampleCardio = listOf(
        WorkoutCapsule("Running", "5.2 km", Icons.Default.DirectionsBike, VitalColors.AccentCRD, listOf(0.2f, 0.4f, 0.35f, 0.5f, 0.45f))
    )
    val weeklyKcal = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val weeklyLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
}