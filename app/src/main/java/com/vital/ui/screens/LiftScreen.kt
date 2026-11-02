package com.vital.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.SummaryDial
import com.vital.ui.components.Sparkline
import com.vital.ui.components.SectionLabel
import com.vital.ui.components.WellRow
import com.vital.ui.components.CapsuleRow
import com.vital.ui.components.GreyPanel
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import com.vital.ui.theme.duToSp
import com.vital.ui.theme.duToDp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import com.vital.ui.WorkoutCapsule
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.FabGlyph

@Composable
fun LiftScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val restTimerActive by viewModel.restTimerActive.collectAsStateWithLifecycle(false)
    val restTimerRemaining by viewModel.restTimerRemaining.collectAsStateWithLifecycle(0)
    val volumeToday by viewModel.volumeToday.collectAsStateWithLifecycle(0f)
    val weeklyVolumeData by viewModel.weeklyVolumeData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels.collectAsStateWithLifecycle(emptyList())
    val workoutsToday by viewModel.workoutsToday.collectAsStateWithLifecycle(emptyList())

    var searchText by remember { mutableStateOf("") }

    ScreenScaffold(
        variant = ScreenVariant.OrangeHero,
        title = "LIFT",
        heroLabel = "REST LEFT",
        heroValue = "2:34",
        onShowMore = onShowSummary,
        // OrangeDial parameters for Lift
        dialLetters = listOf("2", "3", "4"),
        dialHighlightedIndex = 1, // 3 highlighted (0-indexed: 0=2, 1=3, 2=4)
        dialInnerLabel = "BENCH PRESS",
        dialInnerValue = "80",
        dialInnerUnit = "KG",
        dialPillText = "SET 3 · ×8",
        dialTopPill = "OF 3:00",
        dialFabGlyph = FabGlyph.Plus
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise title row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BENCH PRESS",
                    color = VitalColors.textMuted,
                    fontSize = 13.duToSp(),
                    fontWeight = FontWeight.Medium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = "LAST 80 × 8",
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Medium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            // Weight / Reps / Log Set row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Weight well
                WellRow(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "WEIGHT KG",
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "80",
                            color = VitalColors.textPrimary,
                            fontSize = 24.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
                // Reps well
                WellRow(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "REPS",
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "8",
                            color = VitalColors.textPrimary,
                            fontSize = 24.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
                // Log Set button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable { /* Log set */ },
                    color = VitalColors.orange,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "LOG SET",
                        color = VitalColors.black,
                        fontSize = 12.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
            }

            // Rest preset row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RestPresetButton(
                    modifier = Modifier.weight(1f),
                    label = "2:00",
                    isSelected = false,
                    onClick = { /* Set rest preset */ }
                )
                RestPresetButton(
                    modifier = Modifier.weight(1f),
                    label = "3:00",
                    isSelected = true,
                    onClick = { /* Set rest preset */ }
                )
                RestPresetButton(
                    modifier = Modifier.weight(1f),
                    label = "4:00",
                    isSelected = false,
                    onClick = { /* Set rest preset */ }
                )
            }

            // Plate panel
            SectionLabel(title = "PLATES")
            GreyPanel(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(159.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "PLATES · TARGET 80 KG · BAR 20 KG",
                        color = VitalColors.textPrimary,
                        fontSize = 10.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 0.24.em,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                    // Bar line
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        drawLine(
                            color = VitalColors.black,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                            strokeWidth = 8.dp.toPx()
                        )
                    }
                    // Plate visualization placeholder
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 25 kg plate (black, taller)
                        Surface(
                            modifier = Modifier
                                .width(16.dp)
                                .height(80.dp),
                            color = VitalColors.black,
                            shape = RoundedCornerShape(4.dp)
                        ) {}
                        // 5 kg plate (orange, shorter)
                        Surface(
                            modifier = Modifier
                                .width(13.dp)
                                .height(40.dp),
                            color = VitalColors.orange,
                            shape = RoundedCornerShape(4.dp)
                        ) {}
                    }
                    // Result
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "30 KG PER SIDE · NEAREST",
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // Session row
            CapsuleRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                icon = { Icon(Icons.Default.LocalFireDepartment, "", tint = VitalColors.orange) },
                label = "SESSION",
                value = "3 SETS",
                secondaryText = "1,880 KG",
                sparklineData = listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f),
                onClick = null
            )

            // Workouts section
            SectionLabel(title = "WORKOUTS")

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(workoutsToday) { index, workout ->
                    CapsuleRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(workout.icon, "", tint = workout.accentColor) },
                        label = workout.name,
                        value = workout.value,
                        sparklineData = workout.sparklineData,
                        onClick = null
                    )
                }

                if (workoutsToday.isEmpty()) {
                    item {
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

@Composable
fun RestPresetButton(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) VitalColors.orange else VitalColors.wellFill,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.5.dp, if (isSelected) Color.Transparent else VitalColors.wellStroke)
    ) {
        Text(
            text = label,
            color = if (isSelected) VitalColors.black else VitalColors.textPrimary,
            fontSize = 12.duToSp(),
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(horizontal = 16.dp)
        )
    }
}

object LiftScreenPreviewData {
    val sampleWorkouts = listOf(
        WorkoutCapsule("Bench Press", "3 × 10 @ 100kg", Icons.Default.LocalFireDepartment, VitalColors.orange, listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f)),
        WorkoutCapsule("Squat", "4 × 8 @ 140kg", Icons.Default.LocalFireDepartment, VitalColors.orange, listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.65f)),
        WorkoutCapsule("Deadlift", "3 × 5 @ 180kg", Icons.Default.LocalFireDepartment, VitalColors.orange, listOf(0.5f, 0.7f, 0.6f, 0.8f, 0.75f))
    )
    val weeklyVolume = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val weeklyLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
}