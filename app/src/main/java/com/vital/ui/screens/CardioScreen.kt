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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
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
import com.vital.ui.components.QuickStat
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
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.FabGlyph

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
    val cardioKcalToday by viewModel.cardioKcalToday.collectAsStateWithLifecycle(0f)
    val weeklyCardioData by viewModel.weeklyCardioData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels.collectAsStateWithLifecycle(emptyList())
    val cardioToday by viewModel.cardioToday.collectAsStateWithLifecycle(emptyList())

    var selectedCardioType by remember { mutableStateOf(0) } // 0=RUN, 1=BIKE, 2=WALK, 3=OTHER
    var durationText by remember { mutableStateOf("32:10") }
    var distanceText by remember { mutableStateOf("3.22") }
    var speedText by remember { mutableStateOf("6.0") }
    var inclineText by remember { mutableStateOf("2.0") }

    ScreenScaffold(
        variant = ScreenVariant.OrangeHero,
        title = "CARDIO",
        heroLabel = "CARDIO KCAL",
        heroValue = "187",
        onShowMore = onShowSummary,
        // OrangeDial parameters for Cardio
        dialLetters = listOf("R", "B", "W"),
        dialHighlightedIndex = 0, // R (Run) highlighted
        dialInnerLabel = "SPEED",
        dialInnerValue = "6.0",
        dialInnerUnit = "KM/H",
        dialPillText = "INCL 2.0%",
        dialTopPill = "32:10",
        dialFabGlyph = FabGlyph.Camera
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type tiles row (RUN, BIKE, WALK, OTHER)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardioTypeTile(
                    modifier = Modifier.weight(1f),
                    label = "RUN",
                    icon = Icons.Default.DirectionsRun,
                    isSelected = selectedCardioType == 0,
                    onClick = { selectedCardioType = 0 }
                )
                CardioTypeTile(
                    modifier = Modifier.weight(1f),
                    label = "BIKE",
                    icon = Icons.Default.DirectionsBike,
                    isSelected = selectedCardioType == 1,
                    onClick = { selectedCardioType = 1 }
                )
                CardioTypeTile(
                    modifier = Modifier.weight(1f),
                    label = "WALK",
                    icon = Icons.Default.DirectionsWalk,
                    isSelected = selectedCardioType == 2,
                    onClick = { selectedCardioType = 2 }
                )
                CardioTypeTile(
                    modifier = Modifier.weight(1f),
                    label = "OTHER",
                    icon = Icons.Default.Timer,
                    isSelected = selectedCardioType == 3,
                    onClick = { selectedCardioType = 3 }
                )
            }

            // Scan row
            WellRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val centerV = Alignment.CenterVertically
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = centerV
                ) {
                    // Camera icon well
                    Surface(
                        modifier = Modifier.size(30.dp),
                        color = VitalColors.iconWellFill,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, VitalColors.iconWellStroke)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "OCR",
                                tint = VitalColors.orange,
                                modifier = Modifier.size(17.dp, 14.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                    ) {
                        Text(
                            text = "SCAN TREADMILL",
                            color = VitalColors.textPrimary,
                            fontSize = 14.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Text(
                            text = "ON-DEVICE OCR",
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Medium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // Four fields row: DURATION, DISTANCE, SPEED, INCLINE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardioField(
                    modifier = Modifier.weight(1f),
                    label = "DURATION",
                    value = durationText,
                    unit = "",
                    hint = "32:10"
                )
                CardioField(
                    modifier = Modifier.weight(1f),
                    label = "DISTANCE",
                    value = distanceText,
                    unit = "KM",
                    hint = "3.22"
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardioField(
                    modifier = Modifier.weight(1f),
                    label = "SPEED",
                    value = speedText,
                    unit = "KM/H",
                    hint = "6.0"
                )
                CardioField(
                    modifier = Modifier.weight(1f),
                    label = "INCLINE",
                    value = inclineText,
                    unit = "%",
                    hint = "2.0"
                )
            }

            // Footer note
            Text(
                text = "ACSM EST · VALID ≤ 6 KM/H",
                color = VitalColors.orange,
                fontSize = 11.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentWidth(Alignment.Start)
            )

            // SAVE button
            Surface(
                modifier = Modifier
                    .width(80.dp)
                    .height(36.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.End),
                color = VitalColors.orange,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SAVE",
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

            // RECENT section label
            SectionLabel(title = "RECENT")

            // Recent capsules
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(cardioToday) { index, cardio ->
                    CapsuleRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(cardio.icon, "", tint = cardio.accentColor) },
                        label = cardio.name,
                        value = cardio.value,
                        sparklineData = cardio.sparklineData,
                        onClick = null
                    )
                }

                if (cardioToday.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO CARDIO LOGGED TODAY",
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
fun CardioTypeTile(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) VitalColors.orange else VitalColors.wellFill,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (isSelected) Color.Transparent else VitalColors.wellStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) VitalColors.black else VitalColors.orange,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = label,
                color = if (isSelected) VitalColors.black else VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun CardioField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    hint: String
) {
    WellRow(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                color = VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = VitalColors.textPrimary,
                    fontSize = 15.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = " $unit",
                        color = VitalColors.textMuted,
                        fontSize = 10.duToSp(),
                        fontWeight = FontWeight.Medium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
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
        CardioCapsule("Morning Run", "5.2 km • 28:45 • 420 kcal", Icons.Default.DirectionsRun, VitalColors.orange, listOf(0.2f, 0.4f, 0.35f, 0.5f, 0.45f)),
        CardioCapsule("Evening Bike", "15.0 km • 42:10 • 380 kcal", Icons.Default.DirectionsBike, VitalColors.orange, listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f))
    )
    val weeklyCardio = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val weeklyLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
}