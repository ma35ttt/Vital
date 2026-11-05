package com.vital.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Weeks, restyled to the editorial-dark hybrid mock: adherence number over a rule,
 * a bar chart and three sparkline trends. Still driven by the sample data in
 * [MainViewModel].
 */
@Composable
fun WeeksScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val period by viewModel.weeksPeriod.collectAsStateWithLifecycle(WeeksPeriod.L)
    val weeklyMacroData by viewModel.weeklyMacroData.collectAsStateWithLifecycle(emptyList())
    val weeklyVolumeData by viewModel.weeklyVolumeData.collectAsStateWithLifecycle(emptyList())
    val weeklyWeightData by viewModel.weeklyWeightData.collectAsStateWithLifecycle(emptyList())
    val weeklyPrData by viewModel.weeklyPrData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels12.collectAsStateWithLifecycle(emptyList())

    val adherence = ((weeklyMacroData.lastOrNull() ?: 0f).toDouble() * 100)
        .coerceIn(0.0, 100.0)
    val days = when (period) {
        WeeksPeriod.L -> 84
        WeeksPeriod.M -> 28
        WeeksPeriod.H -> 7
    }
    val adherentDays = (adherence / 100 * days).roundToInt()
    val barCount = when (period) {
        WeeksPeriod.L -> 12
        WeeksPeriod.M -> 4
        WeeksPeriod.H -> 7
    }
    val dateLabel = remember {
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEE dd.MM", Locale.US))
            .uppercase()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VitalColors.paper)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 18.dp,
                bottom = 24.dp
            )
        ) {
            item {
                EditorialHeader(
                    title = "Weeks",
                    dateLabel = dateLabel,
                    onBack = onBack
                )
            }
            item {
                AdherenceHero(
                    adherence = adherence,
                    periodLabel = periodDescription(period),
                    adherentDays = adherentDays,
                    totalDays = days,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                PeriodTabs(
                    selected = period,
                    onSelect = viewModel::setWeeksPeriod,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
            item {
                AdherenceMacros(
                    values = weeklyMacroData,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
            item {
                WeeklyBars(
                    values = weeklyMacroData,
                    labels = weeklyLabels,
                    barCount = barCount,
                    periodLabel = period.label,
                    modifier = Modifier.padding(top = 0.dp)
                )
            }
            item {
                Trends(
                    weight = weeklyWeightData,
                    volume = weeklyVolumeData,
                    prs = weeklyPrData,
                    modifier = Modifier.padding(top = 0.dp)
                )
            }
        }
    }
}

private fun periodDescription(period: WeeksPeriod): String = when (period) {
    WeeksPeriod.L -> "12 weeks"
    WeeksPeriod.M -> "4 weeks"
    WeeksPeriod.H -> "7 days"
}

// ---- hero ----------------------------------------------------------------------------------

@Composable
private fun AdherenceHero(
    adherence: Double,
    periodLabel: String,
    adherentDays: Int,
    totalDays: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "ADHERENCE",
            color = VitalColors.orange,
            fontFamily = VitalFonts.InterTight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.18.em
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 14.dp)
        ) {
            Text(
                text = adherence.roundToInt().toString(),
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 72.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.055).em
            )
            Text(
                text = "%",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 2.dp, bottom = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(1.dp)
                .background(VitalColors.rule)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((adherence / 100f).toFloat().coerceIn(0f, 1f))
                    .background(VitalColors.orange)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = periodLabel,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(adherentDays.toString())
                    }
                    append(" of ")
                    append(totalDays.toString())
                    append(" days")
                },
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
    }
}

// ---- period tabs ---------------------------------------------------------------------------

@Composable
private fun PeriodTabs(
    selected: WeeksPeriod,
    onSelect: (WeeksPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            WeeksPeriod.entries.forEach { option ->
                val isSelected = option == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clickable { onSelect(option) }
                ) {
                    Text(
                        text = option.label,
                        color = if (isSelected) VitalColors.ink else VitalColors.inkMuted,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (isSelected) VitalColors.orange else Color.Transparent
                            )
                    )
                }
            }
        }
        Hairline()
    }
}

// ---- macro grid ----------------------------------------------------------------------------

@Composable
private fun AdherenceMacros(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val cells = listOf("Protein", "Carbs", "Fat")
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            cells.forEachIndexed { index, label ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(VitalColors.rule)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = if (index == 0) 0.dp else 20.dp,
                            top = 15.dp,
                            bottom = 15.dp
                        )
                ) {
                    Text(
                        text = label,
                        color = VitalColors.inkMuted,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = values.getOrNull(index)
                            ?.let { "${(it * 100).roundToInt()}" }
                            ?: "—",
                        color = VitalColors.ink,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).em,
                        modifier = Modifier.padding(top = 7.dp)
                    )
                }
            }
        }
        Hairline()
    }
}

// ---- weekly bars ---------------------------------------------------------------------------

@Composable
private fun WeeklyBars(
    values: List<Float>,
    labels: List<String>,
    barCount: Int,
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    val data = remember(values, barCount) { values.take(barCount) }
    val tickLabels = remember(labels, barCount) {
        when (barCount) {
            12 -> labels.take(10).filterIndexed { index, _ -> index % 2 == 0 } + "THIS"
            7 -> (6 downTo 1).map { "D-$it" } + "TODAY"
            else -> labels.takeLast(barCount)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Adherence by week",
            trailing = periodLabel
        )
        BarChart(
            values = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(top = 14.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tickLabels.forEach { label ->
                Text(
                    text = label,
                    color = VitalColors.navInactive,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 8.sp,
                    letterSpacing = 0.06.em
                )
            }
        }
        Hairline(modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun BarChart(values: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val slot = size.width / values.size
        val barWidth = (slot * 0.55f).coerceAtMost(16.dp.toPx())
        val gap = (slot - barWidth) / 2f
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0001f } ?: 1f

        values.forEachIndexed { index, raw ->
            // Keep a floor so low weeks stay legible, then stretch across the real range.
            val value = 0.45f + 0.55f * ((raw - min) / range).coerceIn(0f, 1f)
            val barHeight = (value * size.height).coerceAtLeast(3.dp.toPx())
            val x = index * slot + gap
            drawRect(
                color = if (index == values.lastIndex) {
                    VitalColors.orange
                } else {
                    VitalColors.ruleStrong
                },
                topLeft = Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

// ---- trends --------------------------------------------------------------------------------

@Composable
private fun Trends(
    weight: List<Float>,
    volume: List<Float>,
    prs: List<Float>,
    modifier: Modifier = Modifier
) {
    val weightValue = weight.lastOrNull()?.let {
        String.format(Locale.US, "%.1f kg", 80.0 - it * 1.5)
    } ?: "— kg"
    val volumeValue = volume.lastOrNull()?.let {
        String.format(Locale.US, "%.1f t", it * 10.0)
    } ?: "— t"
    val prValue = prs.lastOrNull()?.let {
        String.format(Locale.US, "%.1f kg", 80.0 + it * 20.0)
    } ?: "— kg"

    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Trends",
            trailing = "4 weeks"
        )
        TrendRow(label = "Body weight", value = weightValue, spark = weight)
        TrendRow(label = "Volume", value = volumeValue, spark = volume)
        TrendRow(label = "Bench PR", value = prValue, spark = prs)
    }
}

@Composable
private fun TrendRow(label: String, value: String, spark: List<Float>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 14.dp)
            )
            Sparkline(
                values = spark.takeLast(6),
                modifier = Modifier.size(width = 66.dp, height = 22.dp)
            )
        }
        Hairline()
    }
}

@Composable
private fun Sparkline(values: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (values.size - 1)

        fun y(value: Float) =
            size.height - 2.dp.toPx() - ((value - min) / range) * (size.height - 4.dp.toPx())

        val path = Path()
        values.forEachIndexed { index, value ->
            if (index == 0) path.moveTo(0f, y(value)) else path.lineTo(index * stepX, y(value))
        }
        drawPath(
            path = path,
            color = VitalColors.ruleStrong,
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawCircle(
            color = VitalColors.orange,
            radius = 2.4.dp.toPx(),
            center = Offset(size.width, y(values.last()))
        )
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
