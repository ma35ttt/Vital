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
import androidx.compose.ui.geometry.Size
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.ui.WeeksPeriod
import com.vital.ui.WeeksState
import com.vital.ui.WeeksViewModel
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Weeks, restyled to the editorial-dark hybrid mock and backed by [WeeksViewModel]:
 * real adherence, macro hit rates, weekly bars and trailing trends from Room.
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
    viewModel: WeeksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                    state = state,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                PeriodTabs(
                    selected = state.period,
                    onSelect = viewModel::setPeriod,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
            item {
                AdherenceMacros(
                    state = state,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
            item {
                WeeklyBars(
                    state = state,
                    modifier = Modifier.padding(top = 0.dp)
                )
            }
            item {
                Trends(
                    state = state,
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
    state: WeeksState,
    modifier: Modifier = Modifier
) {
    val pct = state.adherencePct
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
                text = pct.toString(),
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
                    .fillMaxWidth((pct / 100f).coerceIn(0f, 1f))
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
                text = periodDescription(state.period),
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(state.trackedDays.toString())
                    }
                    append(" of ")
                    append(state.period.days.toString())
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
    state: WeeksState,
    modifier: Modifier = Modifier
) {
    val cells = listOf(
        "Protein" to state.snapshot.proteinPct,
        "Carbs" to state.snapshot.carbsPct,
        "Fat" to state.snapshot.fatPct
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            cells.forEachIndexed { index, (label, value) ->
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
                        text = "$value%",
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
    state: WeeksState,
    modifier: Modifier = Modifier
) {
    val bars = state.snapshot.bars
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Adherence by ${if (state.period == WeeksPeriod.H) "day" else "week"}",
            trailing = state.period.label
        )
        BarChart(
            values = bars,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(top = 14.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            bars.indices.forEach { index ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (tickVisible(state.period, index, bars.size)) {
                        Text(
                            text = tickLabel(state.period, index, bars.size),
                            color = VitalColors.navInactive,
                            fontFamily = VitalFonts.JetBrainsMonoRegular,
                            fontSize = 8.sp,
                            letterSpacing = 0.06.em
                        )
                    }
                }
            }
        }
        Hairline(modifier = Modifier.padding(top = 10.dp))
    }
}

private fun tickVisible(period: WeeksPeriod, index: Int, count: Int): Boolean = when (period) {
    WeeksPeriod.L -> index % 2 == 0 || index == count - 1
    else -> true
}

private fun tickLabel(period: WeeksPeriod, index: Int, count: Int): String = when (period) {
    WeeksPeriod.L -> if (index == count - 1) "THIS" else "W-${count - 1 - index}"
    WeeksPeriod.M -> if (index == count - 1) "THIS" else "W-${count - 1 - index}"
    WeeksPeriod.H -> if (index == count - 1) "TODAY" else "D-${count - 1 - index}"
}

@Composable
private fun BarChart(values: List<Int>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val slot = size.width / values.size
        val barWidth = (slot * 0.55f).coerceAtMost(16.dp.toPx())
        val gap = (slot - barWidth) / 2f
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0 } ?: 1

        values.forEachIndexed { index, raw ->
            // Keep a floor so low weeks stay legible, then stretch across the real range.
            val norm = 0.45f + 0.55f * ((raw - min).toFloat() / range)
            val barHeight = (norm * size.height).coerceAtLeast(3.dp.toPx())
            drawRect(
                color = if (index == values.lastIndex) {
                    VitalColors.orange
                } else {
                    VitalColors.ruleStrong
                },
                topLeft = Offset(index * slot + gap, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

// ---- trends --------------------------------------------------------------------------------

@Composable
private fun Trends(
    state: WeeksState,
    modifier: Modifier = Modifier
) {
    val weightValue = state.bodyWeightKg?.let {
        String.format(Locale.US, "%.1f kg", it)
    } ?: "— kg"
    val volumeValue = if (state.volumeKg > 0) {
        String.format(Locale.US, "%.1f t", state.volumeTonnes)
    } else {
        "— t"
    }
    val prValue = state.benchPrKg?.let {
        String.format(Locale.US, "%.1f kg", it)
    } ?: "— kg"

    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Trends",
            trailing = when (state.period) {
                WeeksPeriod.L -> "12 weeks"
                WeeksPeriod.M -> "4 weeks"
                WeeksPeriod.H -> "7 days"
            }
        )
        TrendRow(label = "Body weight", value = weightValue, spark = state.weightSeries)
        TrendRow(label = "Volume · 30 d", value = volumeValue, spark = state.volumeSeries)
        TrendRow(label = "Bench PR", value = prValue, spark = state.prSeries)
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
                values = spark.takeLast(8),
                modifier = Modifier.size(width = 66.dp, height = 22.dp)
            )
        }
        Hairline()
    }
}

@Composable
private fun Sparkline(values: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val meaningful = values.count { it != 0f }
        if (values.size < 2 || meaningful < 2) return@Canvas
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
