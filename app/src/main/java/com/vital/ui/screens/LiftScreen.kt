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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Timer
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
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.SummaryDial
import com.vital.ui.WorkoutCapsule
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import androidx.compose.material.icons.Icons
import com.vital.ui.components.Tag
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.filled.LocalFireDepartment

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
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

    val volumeToday by viewModel.volumeToday.collectAsStateWithLifecycle(0f)
    val weeklyVolumeData by viewModel.weeklyVolumeData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels.collectAsStateWithLifecycle(emptyList())
    val workoutsToday by viewModel.workoutsToday.collectAsStateWithLifecycle(emptyList())
    val restTimerActive by viewModel.restTimerActive.collectAsStateWithLifecycle(false)
    val restTimerRemaining by viewModel.restTimerRemaining.collectAsStateWithLifecycle(0)

    var searchText by remember { mutableStateOf("") }

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
                Tag(text = dayTag, backgroundColor = VitalColors.AccentLFT)
                Text(
                    text = "LIFT",
                    color = VitalColors.PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Tag(text = dateTag, backgroundColor = VitalColors.AccentLFT)
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "SEARCH EXERCISES...", color = VitalColors.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    visualTransformation = VisualTransformation.None,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = VitalColors.NearBlackRow,
                        focusedContainerColor = VitalColors.NearBlackRow,
                        cursorColor = VitalColors.AccentLFT,
                        focusedPlaceholderColor = VitalColors.MutedText,                         unfocusedPlaceholderColor = VitalColors.MutedText,
                        focusedTextColor = VitalColors.PrimaryText,                         unfocusedTextColor = VitalColors.PrimaryText,
                        focusedLeadingIconColor = VitalColors.MutedText,                         unfocusedLeadingIconColor = VitalColors.MutedText,
                        focusedTrailingIconColor = VitalColors.MutedText,                         unfocusedTrailingIconColor = VitalColors.MutedText,
                        disabledContainerColor = VitalColors.NearBlackRow,
                        disabledTextColor = VitalColors.MutedText,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        errorTextColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = VitalColors.MutedText) },
                    trailingIcon = {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add exercise", tint = VitalColors.AccentLFT, modifier = Modifier
                            .fillMaxSize()
                            .clickable { /* TODO: Open exercise picker */ }
                            .padding(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Hero Card on Accent Fill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(280.dp),
                color = VitalColors.AccentLFT,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: label and timer/rest indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Tag(
                            text = "VOLUME TODAY",
                            backgroundColor = Color.Black.copy(alpha = 0.3f),
                            textColor = Color.Black
                        )
                        if (restTimerActive) {
                            Surface(
                                modifier = Modifier.padding(end = 8.dp),
                                color = VitalColors.AccentTDY,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "REST ${restTimerRemaining / 60}:${"%02d".format(restTimerRemaining % 60)}",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Big Volume Display
                    BigKcalDisplay(
                        remaining = volumeToday,
                        unit = "KG",
                        accentColor = Color.Black
                    )

                    // Rest timer presets row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RestTimerPresetButton(modifier = Modifier.weight(1f), label = "2:00", seconds = 120, accentColor = Color.Black)
                        RestTimerPresetButton(modifier = Modifier.weight(1f), label = "3:00", seconds = 180, accentColor = Color.Black)
                        RestTimerPresetButton(modifier = Modifier.weight(1f), label = "4:00", seconds = 240, accentColor = Color.Black)
                    }

                    // Hero Line Graph (inset grey panel)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(top = 8.dp),
                        color = VitalColors.InsetGreyPanel,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        HeroLineGraph(
                            modifier = Modifier.fillMaxSize(),
                            dataPoints = weeklyVolumeData,
                            labels = weeklyLabels,
                            accentColor = Color.Black,
                            showTooltip = true,
                            tooltipIndex = weeklyVolumeData.lastIndex,
                            tooltipValue = weeklyVolumeData.lastOrNull() ?: 0f
                        )
                    }
                }
            }

            // WORKOUT Section
            SectionHeader(
                title = "WORKOUT",
                accentColor = VitalColors.AccentLFT,
                showMoreAction = onShowSummary
            )

            // Workout capsules
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(workoutsToday) { index, workout ->
                    capsuleRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(workout.icon, "", tint = workout.accentColor) },
                        label = workout.name,
                        value = workout.value,
                        sparklineData = workout.sparklineData,
                        accentColor = workout.accentColor
                    )
                }

                // Empty state
                if (workoutsToday.isEmpty()) {
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

@Composable
fun RestTimerPresetButton(
    modifier: Modifier = Modifier,
    label: String,
    seconds: Int,
    accentColor: Color
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { /* TODO: Start rest timer */ },
        color = Color.Black.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}


object LiftScreenPreviewData {
    val sampleWorkouts = listOf(
        WorkoutCapsule("Bench Press", "3 × 10 @ 100kg", Icons.Default.LocalFireDepartment, VitalColors.AccentLFT, listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f)),
        WorkoutCapsule("Squat", "4 × 8 @ 140kg", Icons.Default.LocalFireDepartment, VitalColors.AccentLFT, listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.65f)),
        WorkoutCapsule("Deadlift", "3 × 5 @ 180kg", Icons.Default.LocalFireDepartment, VitalColors.AccentLFT, listOf(0.5f, 0.7f, 0.6f, 0.8f, 0.75f))
    )
    val weeklyVolume = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val weeklyLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
}