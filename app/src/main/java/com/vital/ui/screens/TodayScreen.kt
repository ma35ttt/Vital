package com.vital.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.vital.data.entity.CardioType
import com.vital.ui.TodayCardio
import com.vital.ui.TodayLift
import com.vital.ui.TodayState
import com.vital.ui.TodayViewModel
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialMacroGrid
import com.vital.ui.components.EditorialRuledRow
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.components.formatEditorialNumber
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Today, restyled to the editorial-dark hybrid mock and backed by Room through
 * [TodayViewModel]: day totals, the trailing-week kcal line and today's sessions.
 */
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
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
                    title = "Today",
                    dateLabel = dateLabel
                )
            }
            item {
                TodayHero(
                    state = state,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                EditorialMacroGrid(
                    protein = state.protein,
                    carbs = state.carbs,
                    fat = state.fat,
                    proteinTarget = state.proteinTarget,
                    carbsTarget = state.carbsTarget,
                    fatTarget = state.fatTarget,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                WeekSection(
                    state = state,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                WorkoutsSection(
                    state = state,
                    onNavigateToLift = onNavigateToLift,
                    onNavigateToCardio = onNavigateToCardio,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
        }
    }
}

// ---- hero ----------------------------------------------------------------------------------

@Composable
private fun TodayHero(
    state: TodayState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            EditorialEyebrow(text = "Kcal left")
            Text(
                text = formatEditorialNumber(state.leftKcal),
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 72.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.055).em,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                text = buildAnnotatedString {
                    append("of ")
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(formatEditorialNumber(state.targetKcal.toDouble()))
                    }
                    append(" · ")
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append("${state.eatenPct}%")
                    }
                    append(" eaten")
                },
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 15.dp)
            )
        }
        TodayRing(
            progress = (state.eatenPct / 100f).coerceIn(0f, 1f),
            modifier = Modifier.size(104.dp)
        )
    }
}

@Composable
private fun TodayRing(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 5.dp.toPx()
        val radius = size.minDimension / 2f - stroke / 2f
        val topLeft = Offset(
            size.width / 2f - radius,
            size.height / 2f - radius
        )
        val arcSize = Size(radius * 2f, radius * 2f)

        drawCircle(
            color = VitalColors.ruleStrong,
            radius = radius,
            style = Stroke(width = stroke)
        )
        if (progress > 0f) {
            drawArc(
                color = VitalColors.orange,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

// ---- week ----------------------------------------------------------------------------------

@Composable
private fun WeekSection(
    state: TodayState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "This week",
            trailing = "${formatEditorialNumber(state.weekTotal)} kcal"
        )
        WeekChart(
            points = state.week.map { it.kcal },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            state.week.forEach { point ->
                Text(
                    text = point.date.dayOfWeek.name.take(3),
                    color = VitalColors.navInactive,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 8.sp,
                    letterSpacing = 0.06.em
                )
            }
        }
    }
}

@Composable
private fun WeekChart(
    points: List<Double>,
    modifier: Modifier = Modifier
) {
    val max = remember(points) {
        points.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    }
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val stepX = size.width / (points.size - 1)
        val topPad = 4.dp.toPx()
        val bottomPad = 2.dp.toPx()
        val usable = size.height - topPad - bottomPad

        fun x(index: Int) = stepX * index
        fun y(value: Double) =
            size.height - bottomPad - (value / max).toFloat() * usable

        val line = Path()
        points.forEachIndexed { index, value ->
            if (index == 0) line.moveTo(x(index), y(value))
            else line.lineTo(x(index), y(value))
        }
        drawPath(
            path = line,
            color = VitalColors.ruleStrong,
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // The segment into today is the only orange part, so the accent stays a highlight.
        val last = points.lastIndex
        val todaySegment = Path().apply {
            moveTo(x(last - 1), y(points[last - 1]))
            lineTo(x(last), y(points[last]))
        }
        drawPath(
            path = todaySegment,
            color = VitalColors.orange,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = VitalColors.orange,
            radius = 3.dp.toPx(),
            center = Offset(x(last), y(points[last]))
        )
    }
}

// ---- workouts ------------------------------------------------------------------------------

@Composable
private fun WorkoutsSection(
    state: TodayState,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Workouts",
            trailing = if (state.workoutCount == 1) "1 today" else "${state.workoutCount} today"
        )
        if (state.workoutCount == 0) {
            Text(
                text = "Nothing logged yet",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 13.dp)
            )
            Hairline()
        } else {
            state.lifts.forEach { lift ->
                LiftRow(lift = lift, onClick = onNavigateToLift)
            }
            state.cardios.forEach { cardio ->
                CardioRow(cardio = cardio, onClick = onNavigateToCardio)
            }
        }
    }
}

@Composable
private fun LiftRow(lift: TodayLift, onClick: () -> Unit) {
    EditorialRuledRow(
        leading = "Lift",
        title = lift.title,
        trailing = if (lift.durationMin >= 1) "${lift.durationMin} min" else "—",
        onClick = onClick
    )
}

@Composable
private fun CardioRow(cardio: TodayCardio, onClick: () -> Unit) {
    EditorialRuledRow(
        leading = "Cardio",
        title = cardioLabel(cardio.type),
        trailing = cardio.distanceKm?.let { formatDistance(it) } ?: "${cardio.durationMin} min",
        onClick = onClick
    )
}

private fun cardioLabel(type: CardioType): String = when (type) {
    CardioType.RUNNING -> "Run"
    CardioType.WALKING -> "Walk"
    CardioType.CYCLING -> "Ride"
    CardioType.ROWING -> "Row"
    CardioType.SWIMMING -> "Swim"
    CardioType.ELLIPTICAL -> "Elliptical"
    CardioType.STAIR_CLIMBER -> "Stairs"
    CardioType.OTHER -> "Cardio"
}

private fun formatDistance(km: Double): String {
    val rounded = Math.round(km * 100.0) / 100.0
    val text = if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
    return "$text km"
}
