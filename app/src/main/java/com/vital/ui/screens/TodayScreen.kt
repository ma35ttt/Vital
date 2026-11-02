package com.vital.ui.screens

import com.vital.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
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
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.res.fontResource

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
    val dayTag = today.dayOfWeek.name.take(3).uppercase()
    val dateTag = today.month.name.take(3).uppercase() + ".${today.dayOfMonth}"
    val dateLabel = today.format(DateTimeFormatter.ofPattern("MMM d"))

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

    DesignUnitsProvider {
        // JetBrains Mono from res/font
        val jetbrainsMono = FontFamily(
            Font(R.font.jetbrains_mono_400, FontWeight.Normal),
            Font(R.font.jetbrains_mono_700, FontWeight.Bold)
        )
        Surface(
            modifier = modifier.fillMaxSize(),
            color = VitalColors.black
        ) {
            // Scrollable Column: header → 13 du spacer → hero card → workouts header → capsule rows → 84 du bottom padding
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 84f.duToDp())
            ) {
                // Top Bar — 13.1 du from top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.8f.duToDp())
                        .padding(top = 13.1f.duToDp()),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Tag(text = dayTag, backgroundColor = VitalColors.orange)
                    Text(
                        text = "TODAY",
                        color = VitalColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = jetbrainsMono,
                        letterSpacing = 1.5.sp
                    )
                    Tag(text = dateTag, backgroundColor = VitalColors.orange)
                }

                // 13 du spacer between header and hero
                Spacer(modifier = Modifier.padding(top = 13f.duToDp()))

                // Hero Card — fixed 365.4 × 326.6 du, 13.8 du horizontal inset
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.8f.duToDp())
                        .height(326.6f.duToDp()),
                    color = VitalColors.orange,
                    shape = RoundedCornerShape(11f.duToDp())
                ) {
                    // Hero card content as Box with absolute positioning from card top-left (0,0)
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Label row: cap height 18.5, top 19.1, left 14.3, NOW right edge at 354.6
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 19.1f.duToDp())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.3f.duToDp(), end = 10.8f.duToDp()), // 365.4 - 354.6 = 10.8
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "KCAL REMAINING",
                                    color = VitalColors.black,
                                    fontSize = 18.5f.duToSp(),
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = jetbrainsMono,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "NOW",
                                    color = Color(0xFFD33100),
                                    fontSize = 18.5f.duToSp(),
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = jetbrainsMono
                                )
                            }
                        }

                        // Big number: left-aligned at x 12, digit height 62.7, baseline ~114
                        // KCAL as small ink superscript to right of digits
                        BigKcalDisplay(
                            remaining = remainingKcal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12f.duToDp())
                                .padding(top = (114f - 62.7f).duToDp()) // baseline 114 - digit height 62.7 = top 51.3
                        )

                        // Macro row: 3 columns starting at x 15.5, pitch ~126
                        // Labels cap height 10 at top 141.5
                        // Values 17.3 tall at top 160.6
                        // Units 60% size on same baseline
                        MacroRow(
                            protein = proteinCurrent,
                            carbs = carbsCurrent,
                            fat = fatCurrent,
                            proteinTarget = proteinTarget,
                            carbsTarget = carbsTarget,
                            fatTarget = fatTarget,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 15.5f.duToDp())
                                .padding(top = 141.5f.duToDp())
                        )

                        // Grey Panel — top 199.4 du from card top, height 121.2 du, 6 du side inset (spec)
                        Surface(
                            modifier = Modifier
                                .width(353.4f.duToDp()) // 365.4 - 2*6
                                .height(121.2f.duToDp())
                                .offset(x = 6f.duToDp(), y = 199.4f.duToDp()),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(6.duToDp())
                        ) {
                            HeroLineGraph(
                                modifier = Modifier.fillMaxSize(),
                                dataPoints = weeklyKcalData,
                                labels = listOf("6m", "7m", "8m", "9m", "10m", "11m", "12m", "13m"),
                                accentColor = VitalColors.black,
                                showTooltip = weeklyKcalData.isNotEmpty(),
                                tooltipIndex = if (weeklyKcalData.isNotEmpty()) weeklyKcalData.lastIndex else -1,
                                tooltipValue = weeklyKcalData.lastOrNull() ?: 0f,
                                tooltipLabel = "BMP"
                            )
                        }
                    }
                }

                // WORKOUTS Section
                SectionHeader(
                    title = "WORKOUTS",
                    accentColor = VitalColors.orange,
                    showMoreAction = onShowSummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp)
                )

                // Workout capsules in a Column for proper flow
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    workoutsToday.forEach { workout ->
                        capsuleRow(
                            modifier = Modifier.fillMaxWidth(),
                            icon = { Icon(workout.icon, "", tint = workout.accentColor) },
                            label = workout.name,
                            value = workout.value,
                            sparklineData = workout.sparklineData,
                            accentColor = workout.accentColor,
                            dateLabel = dateLabel
                        )
                    }
                    cardioToday.forEach { cardio ->
                        capsuleRow(
                            modifier = Modifier.fillMaxWidth(),
                            icon = { Icon(cardio.icon, "", tint = cardio.accentColor) },
                            label = cardio.name,
                            value = cardio.value,
                            sparklineData = cardio.sparklineData,
                            accentColor = cardio.accentColor,
                            dateLabel = dateLabel
                        )
                    }
                    if (workoutsToday.isEmpty() && cardioToday.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO WORKOUTS LOGGED TODAY",
                                color = VitalColors.textMuted,
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
        WorkoutCapsule("Strength", "45 min", Icons.Default.LocalFireDepartment, VitalColors.orange, listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f)),
        WorkoutCapsule("Upper Body", "38 min", Icons.Default.DirectionsRun, VitalColors.orange, listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.65f))
    )
    val sampleCardio = listOf(
        WorkoutCapsule("Running", "5.2 km", Icons.Default.DirectionsBike, VitalColors.orange, listOf(0.2f, 0.4f, 0.35f, 0.5f, 0.45f))
    )
    val weeklyKcal = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val weeklyLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
}