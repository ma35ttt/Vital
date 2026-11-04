package com.vital.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.Equipment
import com.vital.data.entity.Exercise
import com.vital.data.entity.WorkoutSet
import com.vital.domain.PlateCalculator
import com.vital.domain.PlateLoad
import com.vital.ui.LiftViewModel
import com.vital.ui.components.FabGlyph
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.insetShadow
import com.vital.ui.formatLiftVolume
import com.vital.ui.formatLiftWeight
import com.vital.ui.formatRestTime
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp

/**
 * Lift, built to docs/measurements/lift.md.
 *
 * The sheet starts at 405 du (ScreenScaffold). The LazyColumn top padding is 8 du, the
 * title row is 25 du and every block is 8 du apart, so the wells land at 454, the presets
 * at 516, the plate panel at 560 and the session row at 727.
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

    var showExercisePicker by rememberSaveable { mutableStateOf(false) }
    var showSessionDetail by rememberSaveable { mutableStateOf(false) }

    val weight = weightText.toDoubleOrNull()
    val reps = repsText.toIntOrNull()
    val canLog = exercise != null && weight != null && weight > 0.0 && reps != null && reps > 0

    val currentSet = exerciseSets.size + 1
    val firstLetter = (currentSet - 1).coerceAtLeast(1)
    val dialLetters = listOf(firstLetter, firstLetter + 1, firstLetter + 2).map { it.toString() }
    val dialHighlight = (currentSet - firstLetter).coerceIn(0, 2)

    val presetSeconds = restPresets.getOrElse(1) { 180 }
    val heroValue = if (restRemaining > 0) formatRestTime(restRemaining) else "0:00"
    val dialInnerLabel = exercise?.name?.uppercase() ?: "SELECT EXERCISE"
    val dialInnerValue = weight?.let { formatLiftWeight(it) } ?: "—"
    val dialInnerUnit = if (weight != null) "kg" else ""
    val dialPillText = if (exercise != null) {
        "SET $currentSet · ×${reps?.toString() ?: "—"}"
    } else {
        "SET —"
    }
    val dialTopPill = "OF ${formatRestTime(presetSeconds)}"

    DesignUnitsProvider {
        ScreenScaffold(
            variant = ScreenVariant.OrangeHero,
            title = "LIFT",
            heroLabel = "REST LEFT",
            heroValue = heroValue,
            onShowMore = onShowSummary,
            dialLetters = dialLetters,
            dialHighlightedIndex = dialHighlight,
            dialInnerLabel = dialInnerLabel,
            dialInnerValue = dialInnerValue,
            dialInnerUnit = dialInnerUnit,
            dialPillText = dialPillText,
            dialTopPill = dialTopPill,
            dialFabGlyph = FabGlyph.Plus
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.duToDp(),
                    end = 16.duToDp(),
                    top = 8.duToDp(),
                    bottom = 16.duToDp()
                ),
                verticalArrangement = Arrangement.spacedBy(8.duToDp())
            ) {
                item {
                    TitleRow(
                        exerciseName = exercise?.name,
                        lastSet = lastSet,
                        onClick = { showExercisePicker = true }
                    )
                }
                item {
                    WeightRepsRow(
                        weightText = weightText,
                        repsText = repsText,
                        onWeightChange = viewModel::setWeight,
                        onRepsChange = viewModel::setReps,
                        canLog = canLog,
                        onLogSet = viewModel::logSet
                    )
                }
                item {
                    RestPresetRow(
                        presets = restPresets,
                        onSelect = viewModel::setRestPreset
                    )
                }
                item {
                    PlatePanel(
                        targetWeight = weight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(159.duToDp())
                    )
                }
                item {
                    SessionRow(
                        setsCount = sessionSets.size,
                        volume = volume,
                        onClick = { showSessionDetail = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.duToDp())
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
                exerciseNames = exercises.associate { it.id to it.name },
                volume = volume,
                onDelete = viewModel::deleteSet,
                onDismiss = { showSessionDetail = false }
            )
        }
    }
}

// ---- title ---------------------------------------------------------------------------------

@Composable
private fun TitleRow(
    exerciseName: String?,
    lastSet: WorkoutSet?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.duToDp())
            .clickable(onClick = onClick)
    ) {
        Text(
            text = exerciseName?.uppercase() ?: "SELECT EXERCISE",
            style = TextStyle(
                color = VitalColors.textPrimary,
                fontSize = 17.duToSp(),
                fontWeight = FontWeight.SemiBold,
                fontFamily = VitalFonts.InterTight,
                lineHeight = 17.duToSp(),
                letterSpacing = 0.01.em,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 1.duToDp())
                .widthIn(max = 250.duToDp())
        )
        Text(
            text = lastSet?.let { "LAST ${formatLiftWeight(it.weightKg)} × ${it.reps}" } ?: "LAST —",
            style = TextStyle(
                color = VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Medium,
                fontFamily = VitalFonts.JetBrainsMonoMedium,
                lineHeight = 10.duToSp(),
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 3.duToDp(), top = 5.duToDp())
        )
    }
}

// ---- weight / reps / log set ---------------------------------------------------------------

@Composable
private fun WeightRepsRow(
    weightText: String,
    repsText: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    canLog: Boolean,
    onLogSet: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.duToDp()),
        horizontalArrangement = Arrangement.spacedBy(8.duToDp())
    ) {
        LiftValueWell(
            label = "WEIGHT KG",
            value = weightText,
            onValueChange = onWeightChange,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            modifier = Modifier.weight(1f)
        )
        LiftValueWell(
            label = "REPS",
            value = repsText,
            onValueChange = onRepsChange,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            modifier = Modifier.weight(1f)
        )
        LogSetButton(
            enabled = canLog,
            onClick = onLogSet,
            modifier = Modifier
                .width(92.duToDp())
                .fillMaxHeight()
        )
    }
}

@Composable
private fun LiftValueWell(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .insetShadow(
                elevation = 2.dp,
                cornerRadius = 10.duToDp(),
                shadowColor = VitalColors.black.copy(alpha = 0.4f)
            ),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(10.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.duToDp(), top = 6.duToDp())
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.12.em,
                    lineHeight = 10.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1
            )
            Spacer(Modifier.height(5.duToDp()))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.duToDp())
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "—",
                        style = TextStyle(
                            color = VitalColors.textMuted,
                            fontSize = 24.duToSp(),
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = VitalFonts.InterTight,
                            lineHeight = 24.duToSp(),
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = VitalColors.textPrimary,
                        fontSize = 24.duToSp(),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = VitalFonts.InterTight,
                        lineHeight = 24.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    cursorBrush = SolidColor(VitalColors.orange),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LogSetButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        color = if (enabled) VitalColors.orange else VitalColors.orange.copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.duToDp())
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "LOG SET",
                style = TextStyle(
                    color = VitalColors.black.copy(alpha = if (enabled) 1f else 0.5f),
                    fontSize = 12.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.08.em,
                    lineHeight = 12.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

// ---- rest presets --------------------------------------------------------------------------

@Composable
private fun RestPresetRow(
    presets: List<Int>,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.duToDp()),
        horizontalArrangement = Arrangement.spacedBy(8.duToDp())
    ) {
        presets.forEachIndexed { index, seconds ->
            RestPresetButton(
                label = formatRestTime(seconds),
                selected = index == 1,
                onClick = { onSelect(seconds) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun RestPresetButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) VitalColors.orange else VitalColors.wellFill,
        shape = RoundedCornerShape(6.duToDp()),
        border = if (selected) null else BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = TextStyle(
                    color = if (selected) VitalColors.black else VitalColors.textPrimary,
                    fontSize = 12.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    lineHeight = 12.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

// ---- plate panel ---------------------------------------------------------------------------

@Composable
private fun PlatePanel(
    targetWeight: Double?,
    modifier: Modifier = Modifier
) {
    val exactLoad: PlateLoad? = targetWeight?.let { PlateCalculator.forTarget(it) }
    val nearestLoad: PlateLoad? =
        if (exactLoad == null) targetWeight?.let { PlateCalculator.nearestLoadable(it) } else null
    val load = exactLoad ?: nearestLoad
    val perSide = load?.perSideKg.orEmpty()
    val isExact = exactLoad != null

    val header = if (targetWeight == null) {
        "PLATES · TARGET — KG · BAR 20 KG"
    } else {
        "PLATES · TARGET ${formatLiftWeight(targetWeight)} KG · BAR 20 KG"
    }

    val result = when {
        perSide.isNotEmpty() -> perSide.joinToString(" + ") { formatLiftWeight(it) }
        targetWeight != null && targetWeight >= PlateCalculator.DEFAULT_BAR_KG -> "BAR"
        else -> "—"
    }

    val caption = when {
        targetWeight == null -> "KG PER SIDE"
        perSide.isEmpty() && targetWeight >= PlateCalculator.DEFAULT_BAR_KG -> "KG PER SIDE · EXACT"
        else -> "KG PER SIDE · ${if (isExact) "EXACT" else "NEAREST"}"
    }

    Surface(
        modifier = modifier.insetShadow(
            elevation = 7.dp,
            cornerRadius = 8.duToDp(),
            shadowColor = VitalColors.panelGreyShadow
        ),
        color = VitalColors.panelGrey,
        shape = RoundedCornerShape(8.duToDp())
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                val u = size.height / 159f
                val barCenterY = 95.5f * u
                val barHeight = 8f * u

                // Bar line, bleeding to both panel edges.
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, barCenterY - barHeight / 2f),
                    size = Size(size.width, barHeight)
                )

                // Collar to the left of the plates.
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(18f * u, 79f * u),
                    size = Size(10f * u, 33f * u),
                    cornerRadius = CornerRadius(2f * u)
                )

                var x = 34f * u
                perSide.forEach { plate ->
                    val plateWidth = if (plate >= 25.0) 16f * u else 13f * u
                    val plateHeight =
                        ((27.0 + 2.5 * plate).coerceIn(24.0, 90.0) * u).toFloat()
                    drawRoundRect(
                        color = if (plate >= 25.0) Color.Black else VitalColors.orange,
                        topLeft = Offset(x, barCenterY - plateHeight / 2f),
                        size = Size(plateWidth, plateHeight),
                        cornerRadius = CornerRadius(3f * u)
                    )
                    x += plateWidth + 2f * u
                }
            }

            Text(
                text = header,
                style = TextStyle(
                    color = VitalColors.black,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.1.em,
                    lineHeight = 10.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.duToDp(), top = 10.duToDp())
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.duToDp(), top = 30.duToDp()),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = result,
                    style = TextStyle(
                        color = VitalColors.textPrimary,
                        fontSize = resultFontSize(result).duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.InterTight,
                        lineHeight = resultFontSize(result).duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.height(40.duToDp()))
                Text(
                    text = caption,
                    style = TextStyle(
                        color = VitalColors.textPrimary,
                        fontSize = 10.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        lineHeight = 10.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private fun resultFontSize(result: String): Float = when {
    result.length <= 7 -> 38f
    result.length <= 10 -> 30f
    else -> 24f
}

// ---- session row ---------------------------------------------------------------------------

@Composable
private fun SessionRow(
    setsCount: Int,
    volume: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(10.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.duToDp(), end = 19.duToDp()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SESSION · $setsCount ${if (setsCount == 1) "SET" else "SETS"}",
                style = TextStyle(
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.12.em,
                    lineHeight = 10.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatLiftVolume(volume),
                    style = TextStyle(
                        color = VitalColors.textPrimary,
                        fontSize = 20.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.InterTight,
                        lineHeight = 20.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1
                )
                Spacer(Modifier.width(3.duToDp()))
                Text(
                    text = "KG",
                    style = TextStyle(
                        color = VitalColors.textMuted,
                        fontSize = 10.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        lineHeight = 10.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier.padding(bottom = 2.duToDp())
                )
            }
        }
    }
}

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

    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = "SELECT EXERCISE",
                style = dialogTitleStyle()
            )
            Spacer(Modifier.height(10.duToDp()))
            SearchField(value = query, onValueChange = { query = it })
            Spacer(Modifier.height(10.duToDp()))
            if (filtered.isEmpty()) {
                Text(
                    text = "NO MATCH",
                    style = TextStyle(
                        color = VitalColors.textMuted,
                        fontSize = 11.duToSp(),
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        lineHeight = 11.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(Modifier.height(12.duToDp()))
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.duToDp()),
                    verticalArrangement = Arrangement.spacedBy(6.duToDp())
                ) {
                    items(filtered, key = { it.id }) { option ->
                        ExerciseRow(
                            exercise = option,
                            selected = option.id == selectedId,
                            onClick = { onSelect(option) }
                        )
                    }
                }
                Spacer(Modifier.height(12.duToDp()))
            }
            DialogButton(
                text = "CLOSE",
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.duToDp())
            .clickable(onClick = onClick),
        color = if (selected) VitalColors.orange.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(8.duToDp()),
        border = BorderStroke(
            1.duToDp(),
            if (selected) VitalColors.orange else VitalColors.wellStroke
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.uppercase(),
                    style = TextStyle(
                        color = if (selected) VitalColors.orange else VitalColors.textPrimary,
                        fontSize = 13.duToSp(),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = VitalFonts.InterTight,
                        lineHeight = 13.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exerciseSubtitle(exercise),
                    style = TextStyle(
                        color = VitalColors.textMuted,
                        fontSize = 9.duToSp(),
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        lineHeight = 9.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Text(
                    text = "SELECTED",
                    style = TextStyle(
                        color = VitalColors.orange,
                        fontSize = 9.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        lineHeight = 9.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
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
            Spacer(Modifier.height(4.duToDp()))
            Text(
                text = "${sets.size} ${if (sets.size == 1) "SET" else "SETS"} · ${formatLiftVolume(volume)} KG",
                style = TextStyle(
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    lineHeight = 10.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            Spacer(Modifier.height(12.duToDp()))
            if (sets.isEmpty()) {
                Text(
                    text = "NO SETS YET",
                    style = TextStyle(
                        color = VitalColors.textMuted,
                        fontSize = 11.duToSp(),
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        lineHeight = 11.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.duToDp()),
                    verticalArrangement = Arrangement.spacedBy(8.duToDp())
                ) {
                    items(sets, key = { it.id }) { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.duToDp()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = (exerciseNames[set.exerciseId] ?: "EXERCISE").uppercase(),
                                    style = TextStyle(
                                        color = VitalColors.textPrimary,
                                        fontSize = 13.duToSp(),
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = VitalFonts.InterTight,
                                        lineHeight = 13.duToSp(),
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "SET ${set.setIndex} · ${formatLiftWeight(set.weightKg)} KG × ${set.reps}",
                                    style = TextStyle(
                                        color = VitalColors.textMuted,
                                        fontSize = 10.duToSp(),
                                        fontFamily = VitalFonts.JetBrainsMonoBold,
                                        lineHeight = 10.duToSp(),
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
                            Text(
                                text = "DELETE",
                                style = TextStyle(
                                    color = VitalColors.orange,
                                    fontSize = 10.duToSp(),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VitalFonts.JetBrainsMonoBold,
                                    lineHeight = 10.duToSp(),
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                modifier = Modifier
                                    .clickable { onDelete(set) }
                                    .padding(start = 8.duToDp(), top = 6.duToDp(), bottom = 6.duToDp())
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.duToDp()))
            DialogButton(
                text = "CLOSE",
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
            .height(40.duToDp())
            .background(VitalColors.black, RoundedCornerShape(8.duToDp()))
            .border(1.duToDp(), VitalColors.wellStroke, RoundedCornerShape(8.duToDp())),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = "SEARCH",
                style = TextStyle(
                    color = VitalColors.textMuted,
                    fontSize = 11.duToSp(),
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    lineHeight = 11.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                modifier = Modifier.padding(horizontal = 12.duToDp())
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontFamily = VitalFonts.InterTight,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            cursorBrush = SolidColor(VitalColors.orange),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.duToDp())
        )
    }
}

@Composable
private fun DialogShell(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(18.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Column(modifier = Modifier.padding(18.duToDp())) {
            content()
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(46.duToDp())
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.orange)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = TextStyle(
                    color = VitalColors.orange,
                    fontSize = 12.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.08.em,
                    lineHeight = 12.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

@Composable
private fun dialogTitleStyle(): TextStyle = TextStyle(
    color = VitalColors.textPrimary,
    fontSize = 13.duToSp(),
    fontWeight = FontWeight.Bold,
    fontFamily = VitalFonts.JetBrainsMonoBold,
    letterSpacing = 0.15.em,
    lineHeight = 13.duToSp(),
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

private fun exerciseSubtitle(exercise: Exercise): String {
    val parts = mutableListOf(exercise.muscleGroup.name)
    if (exercise.equipment != Equipment.NONE) parts += exercise.equipment.name
    return parts.joinToString(" · ")
}
