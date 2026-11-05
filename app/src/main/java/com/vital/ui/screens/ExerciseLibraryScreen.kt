package com.vital.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.Equipment
import com.vital.data.entity.Exercise
import com.vital.ui.ExerciseLibraryViewModel
import com.vital.ui.MuscleFilter
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.Hairline
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The exercise library: the whole bundled catalog, filtered by muscle group and searched by
 * name, with a detail sheet that can hand a movement straight to Lift.
 */
@Composable
fun ExerciseLibraryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenLift: () -> Unit = {},
    viewModel: ExerciseLibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            EditorialHeader(
                title = "Exercises",
                dateLabel = dateLabel,
                onBack = onBack,
                eyebrow = "Library"
            )
            Text(
                text = state.countLabel,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                letterSpacing = 0.02.em,
                modifier = Modifier.padding(top = 14.dp)
            )
            Spacer(Modifier.height(16.dp))
            LibrarySearchField(
                value = state.query,
                onValueChange = viewModel::setQuery
            )
            Spacer(Modifier.height(18.dp))
            FilterTabs(
                selected = state.filter,
                onSelect = viewModel::setFilter
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            if (state.visible.isEmpty()) {
                item {
                    Text(
                        text = "No exercises match.",
                        color = VitalColors.inkMuted,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 22.dp)
                    )
                }
            } else {
                items(state.visible, key = { it.id }) { exercise ->
                    Box(Modifier.animateItem()) {
                        ExerciseLibraryRow(
                            exercise = exercise,
                            onClick = { viewModel.open(exercise) }
                        )
                    }
                }
            }
        }
    }

    selected?.let { exercise ->
        ExerciseDetailDialog(
            exercise = exercise,
            onUseInLift = {
                viewModel.useInLift(exercise)
                onOpenLift()
            },
            onDismiss = viewModel::dismiss
        )
    }
}

// ---- search + filters ----------------------------------------------------------------------

@Composable
private fun LibrarySearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(VitalColors.sheet, RoundedCornerShape(10.dp))
            .border(1.dp, VitalColors.rule, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = "Search exercises",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
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
                .padding(horizontal = 14.dp)
        )
    }
}

@Composable
private fun FilterTabs(
    selected: MuscleFilter,
    onSelect: (MuscleFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            MuscleFilter.entries.forEach { option ->
                val isSelected = option == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clickable { onSelect(option) }
                ) {
                    Text(
                        text = option.label.uppercase(),
                        color = if (isSelected) VitalColors.ink else VitalColors.navInactive,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 11.sp,
                        letterSpacing = 0.06.em,
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

// ---- rows ----------------------------------------------------------------------------------

@Composable
private fun ExerciseLibraryRow(
    exercise: Exercise,
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
                .heightIn(min = 52.dp)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = exerciseDetailLine(exercise),
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 9.sp,
                    letterSpacing = 0.05.em,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (exercise.isCompound) {
                CompoundTag()
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = "›",
                color = VitalColors.navInactive,
                fontFamily = VitalFonts.InterTight,
                fontSize = 16.sp
            )
        }
        Hairline()
    }
}

@Composable
private fun CompoundTag() {
    Box(
        modifier = Modifier
            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = "COMPOUND",
            color = VitalColors.orange,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.em
        )
    }
}

// ---- detail dialog -------------------------------------------------------------------------

@Composable
private fun ExerciseDetailDialog(
    exercise: Exercise,
    onUseInLift: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = "EXERCISE",
                style = dialogTitleStyle()
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = exercise.name,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).em
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = exerciseDetailLine(exercise),
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 10.sp,
                letterSpacing = 0.05.em
            )
            Spacer(Modifier.height(16.dp))

            val instructions = exercise.instructions?.trim().orEmpty()
            if (instructions.isEmpty()) {
                Text(
                    text = "No instructions for this movement yet.",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = instructions,
                        color = VitalColors.ink,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            DialogButton(
                text = "Use in Lift",
                onClick = onUseInLift,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ---- shared dialog styling -----------------------------------------------------------------

@Composable
private fun DialogShell(content: @Composable () -> Unit) {
    // Settle the card in with a slight scale + fade instead of popping.
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f)
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Read the entrance value in the layer so each frame only redraws.
                val p = entrance.value
                val scale = 0.94f + 0.06f * p
                scaleX = scale
                scaleY = scale
                alpha = p.coerceIn(0f, 1f)
            }
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
            color = VitalColors.onAccent,
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

/** "CHEST · BARBELL · 180s" — rest is dropped for movements with no timer (cardio). */
private fun exerciseDetailLine(exercise: Exercise): String {
    val parts = mutableListOf(exercise.muscleGroup.name)
    if (exercise.equipment != Equipment.NONE) parts += exercise.equipment.name
    if (exercise.defaultRestSec > 0) parts += "${exercise.defaultRestSec}s"
    return parts.joinToString(" · ")
}
