package com.vital.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import com.vital.ui.DaySummary
import com.vital.ui.SummaryCardio
import com.vital.ui.SummaryLift
import com.vital.ui.SummaryViewModel
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialMacroGrid
import com.vital.ui.components.EditorialRuledRow
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.formatEditorialNumber
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val DAY_SECONDS = 86_400f

@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val day by viewModel.summary.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = LocalTime.now()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VitalColors.paper)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            EditorialHeader(
                title = "Summary",
                dateLabel = day.date.format(DateTimeFormatter.ofPattern("EEE dd.MM", Locale.US)).uppercase(),
                onBack = onBack,
                backLabel = "Back",
                modifier = Modifier.padding(top = 18.dp)
            )

            DayRing(
                day = day,
                now = now,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            )

            RingLegend(day = day)

            if (day.sessionCount > 0) {
                EditorialSectionHeader(
                    title = "Sessions",
                    trailing = day.sessionCount.toString()
                )
                day.lifts.forEach { lift -> LiftRow(lift) }
                day.cardios.forEach { cardio -> CardioRow(cardio) }
            }

            EditorialSectionHeader(
                title = "Totals",
                trailing = "of target"
            )
            EditorialMacroGrid(
                protein = day.protein,
                carbs = day.carbs,
                fat = day.fat,
                proteinTarget = day.proteinTarget,
                carbsTarget = day.carbsTarget,
                fatTarget = day.fatTarget
            )

            Verdict(day = day, now = now)

            EditorialButton(
                text = "Share day",
                onClick = { shareDay(context, day) },
                modifier = Modifier.padding(top = 22.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun DayRing(
    day: DaySummary,
    now: LocalTime,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(240.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val ringRadius = size.minDimension / 2f - 20.dp.toPx()

                // 24 hour ticks, major every six hours.
                for (hour in 0 until 24) {
                    val angle = (hour / 24f * 360f - 90f) * (Math.PI / 180.0)
                    val major = hour % 6 == 0
                    val inner = ringRadius - (if (major) 6.dp else 2.dp).toPx()
                    val outer = ringRadius + 4.dp.toPx()
                    drawLine(
                        color = if (major) VitalColors.navInactive else VitalColors.ruleStrong,
                        start = Offset(
                            cx + inner * cos(angle).toFloat(),
                            cy + inner * sin(angle).toFloat()
                        ),
                        end = Offset(
                            cx + outer * cos(angle).toFloat(),
                            cy + outer * sin(angle).toFloat()
                        ),
                        strokeWidth = (if (major) 1.2.dp else 0.8.dp).toPx()
                    )
                }

                drawCircle(
                    color = VitalColors.rule,
                    radius = ringRadius,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Lift: the outer arc.
                day.lifts.forEach { lift ->
                    val start = timeFraction(lift.startedAt) * 360f - 90f
                    val sweep = ((lift.endedAt - lift.startedAt) / 86_400_000f * 360f)
                        .coerceAtLeast(2f)
                    drawArc(
                        color = VitalColors.orange,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(cx - ringRadius, cy - ringRadius),
                        size = Size(ringRadius * 2f, ringRadius * 2f),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Cardio: the inner, quieter arc.
                val cardioRadius = ringRadius - 12.dp.toPx()
                day.cardios.forEach { cardio ->
                    val start = timeFraction(cardio.startedAt) * 360f - 90f
                    val sweep = (cardio.durationSec / 86_400f * 360f).coerceAtLeast(2f)
                    drawArc(
                        color = VitalColors.orange.copy(alpha = 0.45f),
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(cx - cardioRadius, cy - cardioRadius),
                        size = Size(cardioRadius * 2f, cardioRadius * 2f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Meals: one dot per meal slot.
                day.meals.forEach { meal ->
                    val angle = timeFraction(meal.at) * 360f - 90f
                    val radians = angle * (Math.PI / 180.0)
                    drawCircle(
                        color = VitalColors.ink,
                        radius = 3.2.dp.toPx(),
                        center = Offset(
                            cx + ringRadius * cos(radians).toFloat(),
                            cy + ringRadius * sin(radians).toFloat()
                        )
                    )
                }

                // Now marker.
                val nowAngle = (now.toSecondOfDay() / DAY_SECONDS * 360f - 90f) * (Math.PI / 180.0)
                val markerInner = ringRadius - 16.dp.toPx()
                val markerOuter = ringRadius + 12.dp.toPx()
                drawLine(
                    color = VitalColors.orange,
                    start = Offset(
                        cx + markerInner * cos(nowAngle).toFloat(),
                        cy + markerInner * sin(nowAngle).toFloat()
                    ),
                    end = Offset(
                        cx + markerOuter * cos(nowAngle).toFloat(),
                        cy + markerOuter * sin(nowAngle).toFloat()
                    ),
                    strokeWidth = 1.dp.toPx()
                )
            }

            HourLabel("00", Modifier.align(Alignment.TopCenter))
            HourLabel("06", Modifier.align(Alignment.CenterEnd))
            HourLabel("12", Modifier.align(Alignment.BottomCenter))
            HourLabel("18", Modifier.align(Alignment.CenterStart))

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatEditorialNumber(day.eatenKcal),
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.05).em
                )
                EditorialEyebrow(
                    text = "Eaten",
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = if (day.leftKcal >= 0) {
                        "${formatEditorialNumber(day.leftKcal)} left"
                    } else {
                        "${formatEditorialNumber(abs(day.leftKcal))} over"
                    },
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun HourLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        color = VitalColors.navInactive,
        fontFamily = VitalFonts.JetBrainsMonoRegular,
        fontSize = 8.sp,
        letterSpacing = 0.06.em,
        modifier = modifier
    )
}

@Composable
private fun RingLegend(day: DaySummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!day.hasAnything) {
            Text(
                text = "A quiet day so far",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 10.sp
            )
            return@Row
        }

        if (day.meals.isNotEmpty()) {
            LegendItem(label = if (day.mealCount == 1) "1 meal" else "${day.mealCount} meals") {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(VitalColors.ink, CircleShape)
                )
            }
        }
        if (day.lifts.isNotEmpty()) {
            LegendItem(label = "Lift ${day.liftMinutes} min") {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(4.dp)
                        .background(VitalColors.orange, CircleShape)
                )
            }
        }
        if (day.cardios.isNotEmpty()) {
            LegendItem(label = "Cardio ${day.cardioMinutes} min") {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .background(VitalColors.orange.copy(alpha = 0.45f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    marker: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 9.dp)
    ) {
        marker()
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun LiftRow(lift: SummaryLift) {
    EditorialRuledRow(
        leading = "Lift",
        title = lift.title,
        trailing = if (lift.durationMin >= 1) {
            "${lift.durationMin} min"
        } else {
            if (lift.setCount == 1) "1 set" else "${lift.setCount} sets"
        }
    )
}

@Composable
private fun CardioRow(cardio: SummaryCardio) {
    EditorialRuledRow(
        leading = "Cardio",
        title = cardioLabel(cardio.type),
        trailing = cardio.distanceKm?.let { formatDistance(it) } ?: "${cardio.durationMin} min"
    )
}

@Composable
private fun Verdict(day: DaySummary, now: LocalTime) {
    val (strong, rest) = when {
        !day.hasAnything -> "" to "Nothing logged yet"
        day.leftKcal < 0 -> formatEditorialNumber(abs(day.leftKcal)) to " over target"
        else -> formatEditorialNumber(day.leftKcal) to " left · ${openSlot(now)}"
    }

    Row(
        modifier = Modifier.padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(VitalColors.orange, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = VitalColors.ink,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(strong)
                }
                append(rest)
            },
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp
        )
    }
}

private fun openSlot(now: LocalTime): String = when (now.hour) {
    in 0..10 -> "breakfast open"
    in 11..15 -> "lunch open"
    in 16..20 -> "dinner open"
    else -> "snacks open"
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

private fun timeFraction(millis: Long): Float {
    val time = Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.toSecondOfDay() / DAY_SECONDS
}

private fun shareDay(context: Context, day: DaySummary) {
    val text = buildString {
        appendLine("Vital · ${day.date}")
        appendLine(
            "Eaten ${formatEditorialNumber(day.eatenKcal)} of " +
                "${formatEditorialNumber(day.targetKcal.toDouble())} kcal"
        )
        appendLine(
            "P ${formatEditorialNumber(day.protein)}/${day.proteinTarget} g · " +
                "C ${formatEditorialNumber(day.carbs)}/${day.carbsTarget} g · " +
                "F ${formatEditorialNumber(day.fat)}/${day.fatTarget} g"
        )
        day.lifts.forEach { lift ->
            appendLine("Lift · ${lift.title} · ${lift.durationMin} min · ${formatEditorialNumber(lift.volumeKg)} kg")
        }
        day.cardios.forEach { cardio ->
            val distance = cardio.distanceKm?.let { " · ${formatDistance(it)}" }.orEmpty()
            appendLine("Cardio · ${cardioLabel(cardio.type)} · ${cardio.durationMin} min$distance")
        }
        if (!day.hasAnything) append("Nothing logged yet.")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text.trim())
    }
    context.startActivity(Intent.createChooser(intent, "Share day"))
}
