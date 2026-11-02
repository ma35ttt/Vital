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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.font.FontFamily
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.Sparkline
import com.vital.ui.components.SummaryDial
import com.vital.ui.components.Tag
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
fun WeeksScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val period by viewModel.weeksPeriod.collectAsStateWithLifecycle(WeeksPeriod.L)
    val weeklyMacroData by viewModel.weeklyMacroData.collectAsStateWithLifecycle(emptyList())
    val weeklyVolumeData by viewModel.weeklyVolumeData.collectAsStateWithLifecycle(emptyList())
    val weeklyWeightData by viewModel.weeklyWeightData.collectAsStateWithLifecycle(emptyList())
    val weeklyPrData by viewModel.weeklyPrData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels12.collectAsStateWithLifecycle(emptyList())

    // Current adherence value for display (last data point * 100)
    val currentAdherence = (weeklyMacroData.lastOrNull() ?: 0f) * 100

    ScreenScaffold(
        variant = ScreenVariant.BlackPage,
        title = "WEEKS"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero card - orange, radius ~18, at content top (y 98 from screen top)
            // Content left/right edge 16 from ScreenScaffold, but design says 12/380 for weeks
            // ScreenScaffold BlackPage uses 12dp horizontal padding, so we use that
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(326.duToDp()),
                color = VitalColors.orange,
                shape = RoundedCornerShape(18.duToDp())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 23.dp), // 26-12=14 from left edge, 121-98=23 from top
                    horizontalAlignment = Alignment.Start
                ) {
                    // Tag: ADHERENCE · 4 WEEKS
                    Text(
                        text = "ADHERENCE · 4 WEEKS",
                        color = VitalColors.black,
                        fontSize = 11.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // L / M / H switch row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp), // y 113 - y 98 = 15, but accounting for tag height
                            horizontalArrangement = Arrangement.End
                    ) {
                        WeeksPeriodSwitch(
                            currentPeriod = period,
                            onPeriodChange = { viewModel.setWeeksPeriod(it) }
                        )
                    }

                    // Giant number with % superscript
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = currentAdherence.toInt().toString(),
                            color = VitalColors.black,
                            fontSize = 94.duToSp(),
                            fontWeight = FontWeight.Normal, // JetBrains Mono 300
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = -0.06.em
                        )
                        Text(
                            text = "%",
                            color = VitalColors.black,
                            fontSize = 36.duToSp(),
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                        )
                    }

                    // P / C / F labels and values
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Labels row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            MacroLabel("PROTEIN", modifier = Modifier.width(113.duToDp()))
                            MacroLabel("CARBS", modifier = Modifier.width(113.duToDp()))
                            MacroLabel("FAT", modifier = Modifier.width(113.duToDp()))
                        }
                        // Values row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            MacroValue("97%", modifier = Modifier.width(113.duToDp()))
                            MacroValue("88%", modifier = Modifier.width(113.duToDp()))
                            MacroValue("91%", modifier = Modifier.width(113.duToDp()))
                        }
                    }

                    // Chart panel - grey with inset shadow
                    GreyPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(122.duToDp())
                            .padding(top = 8.dp) // 5 du gap from value row bottom
                    ) {
                        WeeksChartPanel(
                            modifier = Modifier.fillMaxSize(),
                            macroData = weeklyMacroData,
                            labels = weeklyLabels,
                            currentPeriod = period
                        )
                    }
                }
            }

            // TRENDS section
            SectionLabel(title = "TRENDS")

            // Show More button for trends (dark style)
            Surface(
                modifier = Modifier
                    .width(123.dp)
                    .height(42.dp)
                    .align(Alignment.End)
                    .padding(bottom = 8.dp),
                color = Color.Transparent,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.5.dp, VitalColors.chipDarkStroke)
            ) {
                Text(
                    text = "SHOW MORE",
                    color = VitalColors.textPrimary,
                    fontSize = 12.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.22.em,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }

            // Three capsule rows
            // Row 1: BODY WEIGHT
            CapsuleRow(
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.MonitorWeight, "", tint = VitalColors.orange) },
                label = "BODY WEIGHT",
                value = "68.4 kg",
                secondaryText = "−0.6 · 4W",
                sparklineData = weeklyWeightData,
                onClick = null
            )

            // Row 2: WEEKLY VOLUME
            CapsuleRow(
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.FitnessCenter, "", tint = VitalColors.orange) },
                label = "WEEKLY VOLUME",
                value = "18.4 t",
                secondaryText = "PEAK 21.2",
                sparklineData = weeklyVolumeData,
                onClick = null
            )

            // Row 3: BENCH PR
            CapsuleRow(
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.TrendingUp, "", tint = VitalColors.orange) },
                label = "BENCH PR",
                value = "100 × 5",
                secondaryText = "+5 KG",
                sparklineData = weeklyPrData,
                onClick = null
            )
        }
    }
}

@Composable
private fun WeeksPeriodSwitch(
    currentPeriod: WeeksPeriod,
    onPeriodChange: (WeeksPeriod) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        WeeksPeriod.values().forEach { p ->
            val isSelected = (p == currentPeriod)
            Surface(
                modifier = Modifier
                    .width(95.duToDp())
                    .height(31.duToDp())
                    .clickable { onPeriodChange(p) },
                color = if (isSelected) VitalColors.black else VitalColors.chipDark,
                shape = RoundedCornerShape(6.duToDp()),
                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else VitalColors.chipDarkStroke)
            ) {
                Text(
                    text = p.label,
                    color = if (isSelected) VitalColors.orange else VitalColors.textPrimary,
                    fontSize = 14.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun MacroLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = VitalColors.black.copy(alpha = 0.7f),
        fontSize = 10.duToSp(),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.Start)
    )
}

@Composable
private fun MacroValue(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = VitalColors.black,
        fontSize = 20.duToSp(),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.Start)
    )
}

@Composable
private fun WeeksChartPanel(
    modifier: Modifier = Modifier,
    macroData: List<Float>,
    labels: List<String>,
    currentPeriod: WeeksPeriod
) {
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        if (macroData.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 12.dp.toPx()
        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding
        val barWidth = graphWidth / 12f // 12 bars max
        val barRadius = 3.dp.toPx()

        // Determine number of bars based on period
        val numBars = when (currentPeriod) {
            WeeksPeriod.L -> 12
            WeeksPeriod.M -> 4
            WeeksPeriod.H -> 7
        }
        val dataToShow = macroData.take(numBars)
        val labelsToShow = labels.take(numBars)

        val minValue = dataToShow.minOrNull() ?: 0f
        val maxValue = dataToShow.maxOrNull() ?: 1f
        val range = if (maxValue > minValue) maxValue - minValue else 1f

        // Draw bars
        dataToShow.forEachIndexed { index, value ->
            val x = padding + index * barWidth + (barWidth - 16.dp.toPx()) / 2
            val normalizedValue = (value - minValue) / range
            val barHeight = normalizedValue * graphHeight
            val y = padding + graphHeight - barHeight

            val isCurrentWeek = (index == numBars - 1 && currentPeriod == WeeksPeriod.L)
            val barColor = if (isCurrentWeek) VitalColors.orange else Color(0xFF171A1D) // #171A1D

            // Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(16.dp.toPx(), barHeight),
                cornerRadius = CornerRadius(barRadius, barRadius)
            )

            // Black stroke for current week bar
            if (isCurrentWeek) {
                drawRoundRect(
                    color = VitalColors.black,
                    topLeft = Offset(x - 2.dp.toPx(), y - 2.dp.toPx()),
                    size = Size(20.dp.toPx(), barHeight + 4.dp.toPx()),
                    cornerRadius = CornerRadius(barRadius + 2.dp.toPx(), barRadius + 2.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Tooltip pill for current week (W12)
        if (currentPeriod == WeeksPeriod.L && dataToShow.isNotEmpty()) {
            val lastIndex = numBars - 1
            val lastValue = dataToShow[lastIndex]
            val lastX = padding + lastIndex * barWidth + 8.dp.toPx() // center of bar
            val pillWidth = 81.dp.toPx()
            val pillHeight = 22.dp.toPx()
            val pillX = (lastX - pillWidth / 2).coerceIn(padding, width - padding - pillWidth)
            val pillY = padding + 10.dp.toPx() // near top of panel

            // Pill background
            drawRoundRect(
                color = VitalColors.black,
                topLeft = Offset(pillX, pillY),
                size = Size(pillWidth, pillHeight),
                cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx())
            )

            // Text (simplified - would need TextMeasurer for precise centering)
            // For now just draw the pill, text rendering would need more setup

            // Leader line from pill right edge to bar left edge
            val lineStartX = (pillX + pillWidth).coerceAtMost(lastX - 2.dp.toPx())
            drawLine(
                color = VitalColors.black,
                start = Offset(lineStartX, pillY + pillHeight / 2),
                end = Offset(lastX, pillY + pillHeight / 2),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

enum class WeeksPeriod(val label: String) {
    L("12W"),
    M("4W"),
    H("7D")
}

object WeeksScreenPreviewData {
    val weeklyMacro = listOf(0.85f, 0.92f, 0.88f, 0.95f, 0.91f, 0.94f, 0.93f, 0.89f, 0.96f, 0.90f, 0.94f, 0.92f)
    val weeklyVolume = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f, 0.48f, 0.58f, 0.50f, 0.62f, 0.57f)
    val weeklyWeight = listOf(0.5f, 0.48f, 0.49f, 0.47f, 0.46f, 0.45f, 0.44f, 0.43f, 0.42f, 0.41f, 0.40f, 0.39f)
    val weeklyPrs = listOf(0.3f, 0.35f, 0.4f, 0.42f, 0.45f, 0.48f, 0.5f, 0.52f, 0.55f, 0.58f, 0.6f, 0.62f)
    val weeklyLabels12 = listOf("W-11", "W-10", "W-9", "W-8", "W-7", "W-6", "W-5", "W-4", "W-3", "W-2", "W-1", "THIS")
}