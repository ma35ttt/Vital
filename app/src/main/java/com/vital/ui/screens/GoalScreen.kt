package com.vital.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.ActivityLevel
import com.vital.data.entity.GoalType
import com.vital.data.entity.Settings
import com.vital.data.entity.Sex
import com.vital.domain.BodyGoalPlan
import com.vital.domain.bodyGoalPlan
import com.vital.ui.GoalViewModel
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialRuledRow
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val LB_PER_KG = 2.2046226218

/** The mock's healthy-band green; semantic, so it stays outside the single-accent palette. */
private val HealthyGreen = Color(0xFF2FA36B)

/**
 * The body-goal hub: where the user is, where they're headed, and the daily plan derived from
 * their height, age, sex and cardio level. "Edit goal" opens the same fields inline; saving also
 * applies the target kcal and macros so Today and Meals follow the new plan.
 */
@Composable
fun GoalScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var editing by rememberSaveable { mutableStateOf(false) }
    var imperial by rememberSaveable { mutableStateOf(false) }
    var draft by remember { mutableStateOf(settings) }

    // Entering edit always starts from what is actually stored.
    LaunchedEffect(editing) {
        if (editing) draft = settings
    }

    val plan = remember(settings) { bodyGoalPlan(settings) }
    val previewPlan = remember(draft) { bodyGoalPlan(draft) }

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
                title = if (editing) "Edit goal" else "Goal",
                dateLabel = "BODY",
                onBack = onBack,
                backLabel = "More",
                modifier = Modifier.padding(top = 18.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FieldLabel("Units")
                Segmented(
                    options = listOf("Metric", "Imperial"),
                    selected = if (imperial) 1 else 0,
                    onSelect = { imperial = it == 1 },
                    modifier = Modifier.width(170.dp)
                )
            }

            if (editing) {
                EditPlan(
                    draft = draft,
                    preview = previewPlan,
                    imperial = imperial,
                    onChange = { draft = it },
                    onSave = {
                        viewModel.saveGoal(draft)
                        editing = false
                    },
                    onCancel = { editing = false }
                )
            } else {
                ReadPlan(
                    settings = settings,
                    plan = plan,
                    imperial = imperial,
                    onEdit = { editing = true }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ReadPlan(
    settings: Settings,
    plan: BodyGoalPlan,
    imperial: Boolean,
    onEdit: () -> Unit
) {
    val nowText = formatWeight(settings.bodyWeightKg, imperial)
    val goalText = settings.goalWeightKg?.let { formatWeight(it, imperial) }
    val toGo = plan.kgToGo?.let { formatWeight(abs(it), imperial) } ?: "—"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            FieldLabel("Now → goal", color = VitalColors.inkMuted)
            Text(
                text = if (goalText != null) "$nowText → $goalText" else nowText,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).em,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            FieldLabel("To go", color = VitalColors.inkMuted)
            Text(
                text = toGo,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).em,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }

    Text(
        text = bmiLine(plan),
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.JetBrainsMonoRegular,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 6.dp)
    )

    HealthyRangeBar(
        heightCm = settings.heightCm,
        nowKg = settings.bodyWeightKg,
        goalKg = settings.goalWeightKg,
        plan = plan,
        imperial = imperial,
        modifier = Modifier.padding(top = 20.dp)
    )

    EditorialSectionHeader(title = "Your plan", trailing = "estimate ±10%")
    EditorialRuledRow(leading = null, title = "Target", trailing = "${plan.targetKcal} kcal")
    EditorialRuledRow(
        leading = null,
        title = "Maintenance",
        trailing = "${plan.maintenanceKcal} kcal"
    )
    EditorialRuledRow(
        leading = null,
        title = "Activity",
        trailing = activityLabel(settings.activityLevel)
    )
    EditorialRuledRow(leading = null, title = "Macros", trailing = macroLine(plan))
    if (settings.goalWeightKg != null) {
        EditorialRuledRow(
            leading = null,
            title = "Pace",
            trailing = paceLine(plan, settings.goalRateKgPerWeek, imperial)
        )
    }

    Text(
        text = "Maintenance is an estimate (±10%). We tune it as you log weight and meals.",
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.JetBrainsMonoRegular,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        modifier = Modifier.padding(top = 14.dp)
    )

    EditorialButton(
        text = "Edit goal",
        onClick = onEdit,
        modifier = Modifier.padding(top = 22.dp)
    )
    GhostButton(
        text = "Log weight",
        onClick = onEdit,
        modifier = Modifier.padding(top = 10.dp)
    )
}

@Composable
private fun EditPlan(
    draft: Settings,
    preview: BodyGoalPlan,
    imperial: Boolean,
    onChange: (Settings) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    EditorialSectionHeader(title = "You")
    Segmented(
        options = listOf("Male", "Female"),
        selected = if (draft.sex == Sex.MALE) 0 else 1,
        onSelect = { onChange(draft.copy(sex = if (it == 0) Sex.MALE else Sex.FEMALE)) }
    )
    StepperRow(
        label = "Age",
        value = "${draft.age} yr",
        onMinus = { onChange(draft.copy(age = (draft.age - 1).coerceAtLeast(13))) },
        onPlus = { onChange(draft.copy(age = (draft.age + 1).coerceAtMost(100))) }
    )
    StepperRow(
        label = "Height",
        value = formatHeight(draft.heightCm, imperial),
        onMinus = {
            onChange(draft.copy(heightCm = (draft.heightCm - 1.0).coerceAtLeast(120.0)))
        },
        onPlus = {
            onChange(draft.copy(heightCm = (draft.heightCm + 1.0).coerceAtMost(230.0)))
        }
    )
    StepperRow(
        label = "Current weight",
        value = formatWeight(draft.bodyWeightKg, imperial),
        onMinus = {
            onChange(draft.copy(bodyWeightKg = (draft.bodyWeightKg - 0.5).coerceAtLeast(30.0)))
        },
        onPlus = {
            onChange(draft.copy(bodyWeightKg = (draft.bodyWeightKg + 0.5).coerceAtMost(300.0)))
        }
    )

    EditorialSectionHeader(title = "Goal")
    ChoiceChips(
        options = listOf("Lose fat", "Hold", "Build"),
        selected = when (draft.goalType) {
            GoalType.LOSE -> 0
            GoalType.MAINTAIN -> 1
            GoalType.GAIN -> 2
        },
        onSelect = {
            onChange(
                draft.copy(
                    goalType = when (it) {
                        0 -> GoalType.LOSE
                        1 -> GoalType.MAINTAIN
                        else -> GoalType.GAIN
                    }
                )
            )
        }
    )
    StepperRow(
        label = "Goal weight",
        value = draft.goalWeightKg?.let { formatWeight(it, imperial) } ?: "Not set",
        onMinus = {
            draft.goalWeightKg?.let {
                onChange(draft.copy(goalWeightKg = (it - 0.5).coerceAtLeast(30.0)))
            }
        },
        onPlus = {
            val next = draft.goalWeightKg?.let { (it + 0.5).coerceAtMost(300.0) }
                ?: defaultGoalWeight(draft)
            onChange(draft.copy(goalWeightKg = next))
        },
        onClear = if (draft.goalWeightKg != null) {
            { onChange(draft.copy(goalWeightKg = null)) }
        } else {
            null
        }
    )
    if (draft.goalType != GoalType.MAINTAIN) {
        StepperRow(
            label = "Pace",
            value = formatRate(draft.goalRateKgPerWeek, imperial),
            onMinus = {
                onChange(
                    draft.copy(
                        goalRateKgPerWeek = (draft.goalRateKgPerWeek - 0.25).coerceAtLeast(0.25)
                    )
                )
            },
            onPlus = {
                onChange(
                    draft.copy(
                        goalRateKgPerWeek = (draft.goalRateKgPerWeek + 0.25).coerceAtMost(1.0)
                    )
                )
            }
        )
    }

    EditorialSectionHeader(title = "Cardio")
    ChoiceChips(
        options = listOf("None", "Light", "Moderate", "High"),
        selected = when (draft.activityLevel) {
            ActivityLevel.NONE -> 0
            ActivityLevel.LIGHT -> 1
            ActivityLevel.MODERATE -> 2
            ActivityLevel.HIGH -> 3
        },
        onSelect = {
            onChange(
                draft.copy(
                    activityLevel = when (it) {
                        0 -> ActivityLevel.NONE
                        1 -> ActivityLevel.LIGHT
                        2 -> ActivityLevel.MODERATE
                        else -> ActivityLevel.HIGH
                    }
                )
            )
        }
    )

    EditorialRuledRow(
        leading = null,
        title = "Maintenance",
        trailing = "${preview.maintenanceKcal} kcal"
    )
    EditorialRuledRow(leading = null, title = "Target", trailing = "${preview.targetKcal} kcal")
    EditorialRuledRow(leading = null, title = "Macros", trailing = macroLine(preview))

    Text(
        text = "Maintenance is an estimate (±10%). We tune it as you log weight and meals.",
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.JetBrainsMonoRegular,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        modifier = Modifier.padding(top = 14.dp)
    )

    EditorialButton(
        text = "Save goal",
        onClick = onSave,
        modifier = Modifier.padding(top = 22.dp)
    )
    GhostButton(
        text = "Cancel",
        onClick = onCancel,
        modifier = Modifier.padding(top = 10.dp)
    )
}

/**
 * A healthy-weight band derived from height, with the current and goal weights plotted on it.
 * No body illustration: the bar is the figure.
 */
@Composable
private fun HealthyRangeBar(
    heightCm: Double,
    nowKg: Double,
    goalKg: Double?,
    plan: BodyGoalPlan,
    imperial: Boolean,
    modifier: Modifier = Modifier
) {
    val range = plan.healthyRange
    // The bar is a fixed BMI 15–35 window, not a zoom on the user's data. That keeps the
    // healthy band a consistent middle slice and puts NOW/GOAL where the design expects.
    val metres = heightCm / 100.0
    val square = metres * metres
    val bmiLow = 15.0 * square
    val bmiHigh = 35.0 * square
    val markerMin = listOfNotNull(nowKg, goalKg).minOrNull() ?: nowKg
    val markerMax = listOfNotNull(nowKg, goalKg).maxOrNull() ?: nowKg
    val contextSpan = (bmiHigh - bmiLow).coerceAtLeast(1.0)
    val lo = if (markerMin < bmiLow) markerMin - contextSpan * 0.02 else bmiLow
    val hi = if (markerMax > bmiHigh) markerMax + contextSpan * 0.02 else bmiHigh
    val span = (hi - lo).coerceAtLeast(1.0)

    fun frac(value: Double): Float = ((value - lo) / span).toFloat().coerceIn(0f, 1f)

    val fMin = frac(range.minKg)
    val fMax = frac(range.maxKg)
    val fNow = frac(nowKg)
    val fGoal = goalKg?.let { frac(it) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            FieldLabel("Healthy at ${formatHeight(heightCm, imperial)}", color = VitalColors.inkMuted)
            Text(
                text = formatWeightRange(range.minKg, range.maxKg, imperial),
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .padding(top = 6.dp)
        ) {
            val trackWidth = maxWidth
            val barY = 34.dp
            val labelWidth = 88.dp

            MarkerLabel(
                label = "NOW",
                value = formatWeight(nowKg, imperial),
                x = (trackWidth * fNow - labelWidth / 2f)
                    .coerceIn(0.dp, (trackWidth - labelWidth).coerceAtLeast(0.dp)),
                y = 0.dp,
                width = labelWidth,
                color = VitalColors.inkMuted
            )
            if (fGoal != null) {
                MarkerLabel(
                    label = "GOAL",
                    value = formatWeight(goalKg!!, imperial),
                    x = (trackWidth * fGoal - labelWidth / 2f)
                        .coerceIn(0.dp, (trackWidth - labelWidth).coerceAtLeast(0.dp)),
                    y = barY + 12.dp,
                    width = labelWidth,
                    color = VitalColors.orange
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .offset(y = barY)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(fMin.coerceAtLeast(0.005f))
                        .fillMaxHeight()
                        .background(VitalColors.rule)
                )
                Box(
                    modifier = Modifier
                        .weight((fMax - fMin).coerceAtLeast(0.005f))
                        .fillMaxHeight()
                        .background(HealthyGreen.copy(alpha = 0.55f))
                )
                Box(
                    modifier = Modifier
                        .weight((1f - fMax).coerceAtLeast(0.005f))
                        .fillMaxHeight()
                        .background(VitalColors.rule)
                )
            }

            MarkerDot(
                fraction = fNow,
                color = VitalColors.inkMuted,
                width = trackWidth,
                y = barY
            )
            if (fGoal != null) {
                MarkerDot(
                    fraction = fGoal,
                    color = VitalColors.orange,
                    width = trackWidth,
                    y = barY
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LegendDot(label = "HEALTHY", color = HealthyGreen.copy(alpha = 0.55f))
            LegendDot(label = "NOW", color = VitalColors.inkMuted)
            if (goalKg != null) LegendDot(label = "GOAL", color = VitalColors.orange)
        }
    }
}

@Composable
private fun MarkerLabel(
    label: String,
    value: String,
    x: Dp,
    y: Dp,
    width: Dp,
    color: Color
) {
    Row(
        modifier = Modifier
            .offset(x = x, y = y)
            .width(width),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color,
            fontFamily = VitalFonts.JetBrainsMonoMedium,
            fontSize = 8.sp,
            letterSpacing = 0.1.em
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MarkerDot(fraction: Float, color: Color, width: Dp, y: Dp) {
    val dotSize = 12.dp
    val ring = 3.dp
    Box(
        modifier = Modifier
            .offset(
                x = (width * fraction - dotSize / 2f)
                    .coerceIn(0.dp, (width - dotSize).coerceAtLeast(0.dp)),
                y = y - dotSize / 2f
            )
            .size(dotSize)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            // Paper ring first, then the fill: a crisp disc like the mock, with no halo.
            drawCircle(color = VitalColors.paper, radius = radius)
            drawCircle(color = color, radius = radius - ring.toPx())
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 9.sp,
            letterSpacing = 0.04.em,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

@Composable
private fun FieldLabel(text: String, color: Color = VitalColors.inkMuted) {
    Text(
        text = text.uppercase(),
        color = color,
        fontFamily = VitalFonts.InterTight,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.16.em
    )
}

@Composable
private fun Segmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(9.dp))
    ) {
        options.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (index == selected) VitalColors.orange else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (index == selected) VitalColors.onAccent else VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ChoiceChips(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEachIndexed { index, label ->
            val on = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (on) VitalColors.orange.copy(alpha = 0.10f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (on) VitalColors.orange else VitalColors.ruleStrong,
                        RoundedCornerShape(9.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (on) VitalColors.orange else VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (onClear != null) {
                Text(
                    text = "Clear",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable(onClick = onClear)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            StepButton(symbol = "−", onClick = onMinus)
            Text(
                text = value,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(92.dp)
            )
            StepButton(symbol = "+", onClick = onPlus)
        }
        Hairline()
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .border(1.dp, VitalColors.ruleStrong, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = VitalColors.orange,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun defaultGoalWeight(settings: Settings): Double = when (settings.goalType) {
    GoalType.LOSE -> (settings.bodyWeightKg - 5.0).coerceAtLeast(30.0)
    GoalType.GAIN -> (settings.bodyWeightKg + 5.0).coerceAtMost(300.0)
    GoalType.MAINTAIN -> settings.bodyWeightKg
}

private fun macroLine(plan: BodyGoalPlan): String =
    "${plan.macros.proteinG.roundToInt()}P · " +
        "${plan.macros.carbsG.roundToInt()}C · " +
        "${plan.macros.fatG.roundToInt()}F"

private fun bmiLine(plan: BodyGoalPlan): String {
    val now = String.format(Locale.US, "%.1f", plan.bmiNow)
    val goal = plan.bmiGoal?.let { String.format(Locale.US, "%.1f", it) }
    return if (goal != null) "BMI $now now · $goal at goal" else "BMI $now"
}

private fun paceLine(plan: BodyGoalPlan, rateKgPerWeek: Double, imperial: Boolean): String {
    val weeks = plan.weeks?.let { " · $it weeks" } ?: ""
    return formatRate(rateKgPerWeek, imperial) + weeks
}

private fun activityLabel(level: ActivityLevel): String = when (level) {
    ActivityLevel.NONE -> "None"
    ActivityLevel.LIGHT -> "Light cardio"
    ActivityLevel.MODERATE -> "Moderate cardio"
    ActivityLevel.HIGH -> "High cardio"
}

private fun formatWeight(kg: Double, imperial: Boolean, decimals: Int = 1): String {
    val value = if (imperial) kg * LB_PER_KG else kg
    val text = String.format(Locale.US, "%.${decimals}f", value)
        .let { if (decimals > 0) it.removeSuffix(".0") else it }
    return "$text ${if (imperial) "lb" else "kg"}"
}

private fun formatWeightRange(minKg: Double, maxKg: Double, imperial: Boolean): String {
    fun value(kg: Double): String =
        (if (imperial) kg * LB_PER_KG else kg).roundToInt().toString()
    return "${value(minKg)}–${value(maxKg)} ${if (imperial) "lb" else "kg"}"
}

private fun formatRate(kgPerWeek: Double, imperial: Boolean): String {
    val value = if (imperial) kgPerWeek * LB_PER_KG else kgPerWeek
    val text = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    return "$text ${if (imperial) "lb" else "kg"}/wk"
}

private fun formatHeight(cm: Double, imperial: Boolean): String {
    if (!imperial) return "${cm.roundToInt()} cm"
    val totalInches = (cm / 2.54).roundToInt()
    return "${totalInches / 12}'${totalInches % 12}\""
}