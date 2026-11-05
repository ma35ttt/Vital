package com.vital.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.Equipment
import com.vital.data.entity.Exercise
import com.vital.data.entity.WorkoutSet
import com.vital.domain.PlateCalculator
import com.vital.domain.PlateLoad
import com.vital.ui.LiftViewModel
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.components.formatEditorialNumber
import com.vital.ui.formatLiftVolume
import com.vital.ui.formatLiftWeight
import com.vital.ui.formatRestTime
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Lift, restyled to the editorial-dark hybrid mock.
 *
 * The screen is a single ruled column: a rest hero with a 104 dp progress ring, the
 * selected exercise, a weight/reps grid, the log button, rest presets as underlined
 * tabs, the plate calculator and the session's set list. No cards or wells.
 */
@Composable
fun LiftScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: LiftViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val exercise by viewModel.selectedExercise.collectAsStateWithLifecycle()
    val exerciseSets by viewModel.exerciseSets.collectAsStateWithLifecycle()
    val sessionSets by viewModel.sessionSets.collectAsStateWithLifecycle()
    val lastSet by viewModel.lastSet.collectAsStateWithLifecycle()
    val volume by viewModel.sessionVolume.collectAsStateWithLifecycle()
    val restPresets by viewModel.restPresets.collectAsStateWithLifecycle()
    val restRemaining by viewModel.restRemaining.collectAsStateWithLifecycle()
    val weightText by viewModel.weightText.collectAsStateWithLifecycle()
    val repsText by viewModel.repsText.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showExercisePicker by rememberSaveable { mutableStateOf(false) }
    var showSessionDetail by rememberSaveable { mutableStateOf(false) }

    val weight = weightText.toDoubleOrNull()
    val reps = repsText.toIntOrNull()
    val canLog = exercise != null && weight != null && weight > 0.0 && reps != null && reps > 0

    val presetSeconds = settings.restPresetSec
    val restProgress = if (presetSeconds > 0) {
        (restRemaining.toFloat() / presetSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }
    val exerciseNames = remember(exercises) { exercises.associate { it.id to it.name } }
    val dateLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE dd.MM", Locale.US)).uppercase()
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
                    title = "Lift",
                    dateLabel = dateLabel
                )
            }
            item {
                RestHero(
                    remaining = restRemaining,
                    presetSeconds = presetSeconds,
                    loggedSets = exerciseSets.size,
                    progress = restProgress,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                ExerciseRow(
                    name = exercise?.name,
                    lastSet = lastSet,
                    onClick = { showExercisePicker = true },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
            item {
                WeightRepsGrid(
                    weightText = weightText,
                    repsText = repsText,
                    onWeightChange = viewModel::setWeight,
                    onRepsChange = viewModel::setReps,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            item {
                EditorialButton(
                    text = "Log set",
                    onClick = viewModel::logSet,
                    enabled = canLog,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
            item {
                RestPresetTabs(
                    presets = restPresets,
                    selectedSeconds = presetSeconds,
                    onSelect = viewModel::setRestPreset,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
            item {
                PlateBlock(
                    targetWeight = weight,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
            item {
                SessionBlock(
                    sets = sessionSets,
                    exerciseNames = exerciseNames,
                    volume = volume,
                    onClick = { showSessionDetail = true },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            exercises = exercises,
            selectedId = exercise?.id,
            onSelect = { selected ->
                viewModel.selectExercise(selected)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
    }

    if (showSessionDetail) {
        SessionDetailDialog(
            sets = sessionSets,
            exerciseNames = exerciseNames,
            volume = volume,
            onDelete = viewModel::deleteSet,
            onDismiss = { showSessionDetail = false }
        )
    }
}

// ---- hero ----------------------------------------------------------------------------------

@Composable
private fun RestHero(
    remaining: Int,
    presetSeconds: Int,
    loggedSets: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            EditorialEyebrow(text = "Rest")
            Text(
                text = if (remaining > 0) formatRestTime(remaining) else "0:00",
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 72.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.055).em,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                text = buildAnnotatedString {
                    append("set ")
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(loggedSets.toString())
                    }
                    append(" logged · target ")
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(formatRestTime(presetSeconds))
                    }
                },
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 15.dp)
            )
        }
        RestRing(
            progress = progress,
            modifier = Modifier.size(104.dp)
        )
    }
}

@Composable
private fun RestRing(progress: Float, modifier: Modifier = Modifier) {
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

// ---- exercise ------------------------------------------------------------------------------

@Composable
private fun ExerciseRow(
    name: String?,
    lastSet: WorkoutSet?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name ?: "Select exercise",
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).em,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = lastSet?.let { "${formatLiftWeight(it.weightKg)} × ${it.reps}" } ?: "—",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp
        )
    }
}

// ---- weight / reps -------------------------------------------------------------------------

@Composable
private fun WeightRepsGrid(
    weightText: String,
    repsText: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            FieldCell(
                label = "Weight · kg",
                value = weightText,
                onValueChange = onWeightChange,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
                startPadding = 0.dp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(VitalColors.rule)
            )
            FieldCell(
                label = "Reps",
                value = repsText,
                onValueChange = onRepsChange,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                startPadding = 20.dp,
                modifier = Modifier.weight(1f)
            )
        }
        Hairline()
    }
}

@Composable
private fun FieldCell(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    startPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(
            start = startPadding,
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
        Box(modifier = Modifier.padding(top = 7.dp)) {
            if (value.isEmpty()) {
                Text(
                    text = "—",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.02).em
                ),
                cursorBrush = SolidColor(VitalColors.orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---- rest presets --------------------------------------------------------------------------

@Composable
private fun RestPresetTabs(
    presets: List<Int>,
    selectedSeconds: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            presets.forEach { seconds ->
                val selected = seconds == selectedSeconds
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clickable { onSelect(seconds) }
                ) {
                    Text(
                        text = formatRestTime(seconds),
                        color = if (selected) VitalColors.ink else VitalColors.inkMuted,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (selected) VitalColors.orange else Color.Transparent
                            )
                    )
                }
            }
        }
        Hairline()
    }
}

// ---- plate calculator ----------------------------------------------------------------------

@Composable
private fun PlateBlock(
    targetWeight: Double?,
    modifier: Modifier = Modifier
) {
    val exactLoad: PlateLoad? = targetWeight?.let { PlateCalculator.forTarget(it) }
    val nearestLoad: PlateLoad? =
        if (exactLoad == null) targetWeight?.let { PlateCalculator.nearestLoadable(it) } else null
    val load = exactLoad ?: nearestLoad
    val perSide = load?.perSideKg.orEmpty()

    val result = when {
        perSide.isNotEmpty() -> perSide.joinToString(" + ") { formatLiftWeight(it) }
        targetWeight != null && targetWeight >= PlateCalculator.DEFAULT_BAR_KG -> "Bar"
        else -> "—"
    }
    val caption = when {
        targetWeight == null -> "per side · bar 20 kg"
        else -> "per side · ${if (exactLoad != null) "exact" else "nearest"}"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Plate calculator",
            trailing = caption
        )
        Text(
            text = result,
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).em,
            modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
        )
        Hairline()
    }
}

// ---- session -------------------------------------------------------------------------------

@Composable
private fun SessionBlock(
    sets: List<WorkoutSet>,
    exerciseNames: Map<String, String>,
    volume: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "THIS SESSION",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.16.em
            )
            Text(
                text = "${formatEditorialNumber(volume)} kg",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
        if (sets.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No sets yet",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp
                )
            }
            Hairline()
        } else {
            sets.forEach { set ->
                SessionSetRow(
                    set = set,
                    exerciseName = exerciseNames[set.exerciseId]
                )
            }
        }
    }
}

@Composable
private fun SessionSetRow(
    set: WorkoutSet,
    exerciseName: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set ${set.setIndex}",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.width(92.dp)
            )
            Text(
                text = "${formatLiftWeight(set.weightKg)} × ${set.reps}",
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (exerciseName != null) {
                Text(
                    text = exerciseName,
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
            Text(
                text = formatSetTime(set.updatedAt),
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
        Hairline()
    }
}

private fun formatSetTime(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))

// ---- dialogs -------------------------------------------------------------------------------

@Composable
private fun ExercisePickerDialog(
    exercises: List<Exercise>,
    selectedId: String?,
    onSelect: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(exercises, query) {
        if (query.isBlank()) exercises
        else exercises.filter { it.name.contains(query, ignoreCase = true) }
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = filtered
            .indexOfFirst { it.id == selectedId }
            .coerceAtLeast(0)
    )

    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = "SELECT EXERCISE",
                style = dialogTitleStyle()
            )
            Spacer(Modifier.height(14.dp))
            SearchField(value = query, onValueChange = { query = it })
            Spacer(Modifier.height(10.dp))
            if (filtered.isEmpty()) {
                Text(
                    text = "No match",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(filtered, key = { it.id }) { option ->
                        ExerciseRow(
                            exercise = option,
                            selected = option.id == selectedId,
                            onClick = { onSelect(option) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            DialogButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    color = if (selected) VitalColors.orange else VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exerciseSubtitle(exercise),
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Text(
                    text = "Selected",
                    color = VitalColors.orange,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 9.sp
                )
            }
        }
        Hairline()
    }
}

@Composable
private fun SessionDetailDialog(
    sets: List<WorkoutSet>,
    exerciseNames: Map<String, String>,
    volume: Double,
    onDelete: (WorkoutSet) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = "SESSION",
                style = dialogTitleStyle()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${sets.size} ${if (sets.size == 1) "set" else "sets"} · " +
                    "${formatLiftVolume(volume)} kg",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(14.dp))
            if (sets.isEmpty()) {
                Text(
                    text = "No sets yet",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(sets, key = { it.id }) { set ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exerciseNames[set.exerciseId] ?: "Exercise",
                                        color = VitalColors.ink,
                                        fontFamily = VitalFonts.InterTight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Set ${set.setIndex} · " +
                                            "${formatLiftWeight(set.weightKg)} kg × ${set.reps}",
                                        color = VitalColors.inkMuted,
                                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "Delete",
                                    color = VitalColors.orange,
                                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .clickable { onDelete(set) }
                                        .padding(start = 8.dp, top = 6.dp, bottom = 6.dp)
                                )
                            }
                            Hairline()
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            DialogButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(VitalColors.paper, RoundedCornerShape(4.dp))
            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = "Search",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp
            ),
            cursorBrush = SolidColor(VitalColors.orange),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun DialogShell(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VitalColors.sheet, RoundedCornerShape(12.dp))
            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(VitalColors.orange, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = VitalColors.black,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun dialogTitleStyle(): TextStyle = TextStyle(
    color = VitalColors.inkMuted,
    fontFamily = VitalFonts.InterTight,
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.16.em
)

private fun exerciseSubtitle(exercise: Exercise): String {
    val parts = mutableListOf(exercise.muscleGroup.name)
    if (exercise.equipment != Equipment.NONE) parts += exercise.equipment.name
    return parts.joinToString(" · ")
}
