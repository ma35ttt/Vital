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
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timer
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
import com.vital.ui.components.QuickStat
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke

@Composable
fun CardioScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

    val cardioKcalToday by viewModel.cardioKcalToday.collectAsStateWithLifecycle(0f)
    val weeklyCardioData by viewModel.weeklyCardioData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels.collectAsStateWithLifecycle(emptyList())
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
                Tag(text = dayTag, backgroundColor = VitalColors.AccentCRD)
                Text(
                    text = "CARDIO",
                    color = VitalColors.PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Tag(text = dateTag, backgroundColor = VitalColors.AccentCRD)
            }

            // Cardio type selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CardioTypeButton(modifier = Modifier.weight(1f), label = "RUN", icon = Icons.Default.DirectionsRun, isSelected = true, accentColor = VitalColors.AccentCRD)
                CardioTypeButton(modifier = Modifier.weight(1f), label = "BIKE", icon = Icons.Default.DirectionsBike, isSelected = false, accentColor = VitalColors.AccentCRD)
                CardioTypeButton(modifier = Modifier.weight(1f), label = "WALK", icon = Icons.Default.DirectionsWalk, isSelected = false, accentColor = VitalColors.AccentCRD)
                CardioTypeButton(modifier = Modifier.weight(1f), label = "OTHER", icon = Icons.Default.Timer, isSelected = false, accentColor = VitalColors.AccentCRD)
            }

            // Treadmill OCR button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { /* TODO: Open ML Kit OCR scanner */ },
                    color = VitalColors.NearBlackRow,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, VitalColors.NearBlackRowStroke)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Surface(
                                modifier = Modifier.size(40.dp),
                                color = VitalColors.AccentCRD.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "OCR", tint = VitalColors.AccentCRD)
                                }
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(text = "SCAN TREADMILL", color = VitalColors.PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "OCR: duration, distance, speed, incline, kcal", color = VitalColors.MutedText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Scan", tint = VitalColors.AccentCRD)
                    }
                }
            }

            // Hero Card on Accent Fill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(280.dp),
                color = VitalColors.AccentCRD,
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
                            text = "CARDIO KCAL",
                            backgroundColor = Color.Black.copy(alpha = 0.3f),
                            textColor = Color.Black
                        )
                        Text(
                            text = "NOW",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Big Kcal Display
                    BigKcalDisplay(
                        remaining = cardioKcalToday,
                        accentColor = Color.Black
                    )

                    // Quick stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickStat(label = "DIST", value = "0.0 km", color = Color.Black)
                        QuickStat(label = "TIME", value = "0:00", color = Color.Black)
                        QuickStat(label = "AVG HR", value = "--", color = Color.Black)
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
                            dataPoints = weeklyCardioData,
                            labels = weeklyLabels,
                            accentColor = Color.Black,
                            showTooltip = true,
                            tooltipIndex = weeklyCardioData.lastIndex,
                            tooltipValue = weeklyCardioData.lastOrNull() ?: 0f
                        )
                    }
                }
            }

            // CARDIO Section
            SectionHeader(
                title = "CARDIO",
                accentColor = VitalColors.AccentCRD,
                showMoreAction = onShowSummary
            )

            // Cardio capsules
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(cardioToday) { index, cardio ->
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
                if (cardioToday.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO CARDIO LOGGED TODAY",
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
fun CardioTypeButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { /* TODO: Select cardio type */ },
        color = if (isSelected) accentColor else VitalColors.NearBlackRow,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, if (isSelected) Color.Transparent else VitalColors.NearBlackRowStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else accentColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = label,
                color = if (isSelected) Color.Black else accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

data class CardioCapsule(
    val name: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color,
    val sparklineData: List<Float>
)

object CardioScreenPreviewData {
    val sampleCardio = listOf(
        CardioCapsule("Morning Run", "5.2 km • 28:45 • 420 kcal", Icons.Default.DirectionsRun, VitalColors.AccentCRD, listOf(0.2f, 0.4f, 0.35f, 0.5f, 0.45f)),
        CardioCapsule("Evening Bike", "15.0 km • 42:10 • 380 kcal", Icons.Default.DirectionsBike, VitalColors.AccentCRD, listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f))
    )
    val weeklyCardio = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val weeklyLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
}