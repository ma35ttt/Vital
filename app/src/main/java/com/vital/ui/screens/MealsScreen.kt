package com.vital.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import com.vital.data.entity.Food
import com.vital.data.entity.FoodVariant
import com.vital.data.entity.MealLogEntry
import com.vital.data.entity.MealSlot
import com.vital.domain.ParseResult
import com.vital.ui.FoodChoice
import com.vital.ui.MealsViewModel
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.components.formatEditorialNumber
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Meals, restyled to the editorial-dark hybrid mock. Logging goes through one sheet:
 * search (or a parsed quick add), then grams + slot in the same sheet. The Room writes are
 * unchanged from the M4 rebuild.
 */
@Composable
fun MealsScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MealsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val parsed by viewModel.parsed.collectAsStateWithLifecycle()
    val choices by viewModel.choices.collectAsStateWithLifecycle()
    val recent by viewModel.recentFoods.collectAsStateWithLifecycle()
    val eatenKcal by viewModel.eatenKcal.collectAsStateWithLifecycle()

    // The sheet is open for one slot at a time; `selected` is the quantity step.
    var sheetSlot by remember { mutableStateOf<MealSlot?>(null) }
    var selected by remember { mutableStateOf<FoodChoice?>(null) }
    var ambiguitySlot by remember { mutableStateOf<MealSlot?>(null) }

    val target = settings.kcalTarget
    val remaining = (target - eatenKcal).coerceAtLeast(0.0)
    val progress = if (target > 0) (eatenKcal / target).toFloat().coerceIn(0f, 1f) else 0f

    val protein = meals.sumOf { it.proteinG }
    val carbs = meals.sumOf { it.carbsG }
    val fat = meals.sumOf { it.fatG }

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
                    title = "Meals",
                    dateLabel = dateLabel
                )
            }
            item {
                MealsHero(
                    eaten = eatenKcal,
                    target = target,
                    remaining = remaining,
                    progress = progress,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                MealSlotsSection(
                    meals = meals,
                    eatenKcal = eatenKcal,
                    onSlotClick = { slot ->
                        viewModel.setQuery("")
                        selected = null
                        sheetSlot = slot
                    },
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                MacroSummarySection(
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    proteinTarget = settings.proteinTarget,
                    carbsTarget = settings.carbsTarget,
                    fatTarget = settings.fatTarget,
                    slots = meals.map { it.mealSlot }.distinct().size,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
        }
    }

    val slot = sheetSlot
    if (slot != null) {
        AddFoodSheet(
            slot = slot,
            entries = meals.filter { it.mealSlot == slot },
            query = query,
            onQueryChange = viewModel::setQuery,
            parsed = parsed,
            choices = choices,
            recent = recent,
            gramsPerUnitFor = viewModel::gramsPerUnitFor,
            selected = selected,
            onSelect = { selected = it },
            onBack = {
                selected = null
                viewModel.setQuery("")
            },
            onDismiss = {
                sheetSlot = null
                selected = null
                viewModel.setQuery("")
            },
            onLog = { food, grams, targetSlot ->
                viewModel.logFood(food, grams, targetSlot)
                sheetSlot = null
                selected = null
            },
            onQuickAdd = {
                if (parsed.ambiguities.isNotEmpty()) {
                    // Keep the query so the ambiguity question survives the sheet closing.
                    ambiguitySlot = slot
                    sheetSlot = null
                } else {
                    viewModel.logParsed(slot)
                    sheetSlot = null
                }
            },
            onDelete = viewModel::deleteEntry
        )
    }

    if (ambiguitySlot != null && parsed.ambiguities.isNotEmpty()) {
        val ambiguity = parsed.ambiguities.first()
        AmbiguityDialog(
            question = ambiguity.question,
            onChoose = { variant ->
                viewModel.logParsed(
                    slot = ambiguitySlot!!,
                    overrides = mapOf(ambiguity.foodName to variant)
                )
                ambiguitySlot = null
                sheetSlot = null
                selected = null
            },
            onDismiss = {
                ambiguitySlot = null
                viewModel.setQuery("")
            }
        )
    }
}

// ---- hero ----------------------------------------------------------------------------------

@Composable
private fun MealsHero(
    eaten: Double,
    target: Int,
    remaining: Double,
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Fill the eaten bar towards the current fraction instead of snapping when a meal is logged.
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animated.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialEyebrow(text = "Eaten")
        Text(
            text = formatEditorialNumber(eaten),
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 72.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.055).em,
            modifier = Modifier.padding(top = 14.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .height(1.dp)
                .background(VitalColors.rule)
                // Draw the fill instead of laying it out, so growth only redraws this bar.
                .drawBehind {
                    val value = animated.value
                    drawRect(
                        color = VitalColors.orange,
                        size = Size(size.width * value, size.height)
                    )
                }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = buildAnnotatedString {
                    append("of ")
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(formatEditorialNumber(target.toDouble()))
                    }
                },
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(formatEditorialNumber(remaining))
                    }
                    append(" left")
                },
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
    }
}

// ---- slots ---------------------------------------------------------------------------------

@Composable
private fun MealSlotsSection(
    meals: List<MealLogEntry>,
    eatenKcal: Double,
    onSlotClick: (MealSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Meals",
            trailing = "${formatEditorialNumber(eatenKcal)} kcal"
        )
        MealSlot.entries.forEach { slot ->
            val entries = meals.filter { it.mealSlot == slot }
            MealSlotRow(
                slot = slot,
                entries = entries,
                onClick = { onSlotClick(slot) }
            )
        }
    }
}

@Composable
private fun MealSlotRow(
    slot: MealSlot,
    entries: List<MealLogEntry>,
    onClick: () -> Unit
) {
    val filled = entries.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slot.label(),
                color = if (filled) VitalColors.inkMuted else VitalColors.navInactive,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.width(92.dp)
            )
            if (filled) {
                Text(
                    text = entries.joinToString(" · ") { it.foodName },
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatEditorialNumber(entries.sumOf { it.kcal }),
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp
                )
            } else {
                Text(
                    text = "+ Log ${slot.label().lowercase()}",
                    color = VitalColors.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Hairline()
    }
}

private fun MealSlot.label(): String = when (this) {
    MealSlot.BREAKFAST -> "Breakfast"
    MealSlot.LUNCH -> "Lunch"
    MealSlot.DINNER -> "Dinner"
    MealSlot.SNACK -> "Snacks"
}

// ---- macros --------------------------------------------------------------------------------

@Composable
private fun MacroSummarySection(
    protein: Double,
    carbs: Double,
    fat: Double,
    proteinTarget: Int,
    carbsTarget: Int,
    fatTarget: Int,
    slots: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Today",
            trailing = if (slots == 1) "1 slot" else "$slots slots"
        )
        MacroRow("Protein", protein, proteinTarget)
        MacroRow("Carbs", carbs, carbsTarget)
        MacroRow("Fat", fat, fatTarget)
    }
}

@Composable
private fun MacroRow(label: String, value: Double, target: Int) {
    val percent = if (target > 0) (value / target * 100).roundToInt() else 0
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.width(92.dp)
            )
            Text(
                text = "${formatEditorialNumber(value)} g",
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$percent%",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
        Hairline()
    }
}

// ---- add sheet -----------------------------------------------------------------------------

/**
 * One sheet for the whole add flow. It opens in the search step (or shows the slot's existing
 * entries on top) and steps to grams + slot in the same sheet, so search / text / web / recipe
 * are no longer separate modes.
 */
@Composable
private fun AddFoodSheet(
    slot: MealSlot,
    entries: List<MealLogEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    parsed: ParseResult,
    choices: List<FoodChoice>,
    recent: List<Food>,
    gramsPerUnitFor: (Food) -> Double?,
    selected: FoodChoice?,
    onSelect: (FoodChoice) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onLog: (Food, Double, MealSlot) -> Unit,
    onQuickAdd: () -> Unit,
    onDelete: (MealLogEntry) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VitalColors.sheet,
        dragHandle = { SheetHandle() },
        // The default only pads the bottom, so a tall sheet (keyboard up, long list) slides
        // its header under the status bar. Add the top inset back to keep it clear.
        contentWindowInsets = {
            BottomSheetDefaults.windowInsets.union(
                WindowInsets.statusBars.only(WindowInsetsSides.Top)
            )
        }
    ) {
        if (selected == null) {
            SearchStep(
                slot = slot,
                entries = entries,
                query = query,
                onQueryChange = onQueryChange,
                parsed = parsed,
                choices = choices,
                recent = recent,
                onSelect = onSelect,
                onDismiss = onDismiss,
                onQuickAdd = onQuickAdd,
                onDelete = onDelete
            )
        } else {
            QuantityStep(
                slot = slot,
                choice = selected,
                gramsPerUnitFor = gramsPerUnitFor,
                onBack = onBack,
                onDismiss = onDismiss,
                onLog = onLog
            )
        }
    }
}

@Composable
private fun SearchStep(
    slot: MealSlot,
    entries: List<MealLogEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    parsed: ParseResult,
    choices: List<FoodChoice>,
    recent: List<Food>,
    onSelect: (FoodChoice) -> Unit,
    onDismiss: () -> Unit,
    onQuickAdd: () -> Unit,
    onDelete: (MealLogEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 30.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditorialEyebrow(text = "Add to ${slot.label()}")
                Text(
                    text = "✕",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        if (entries.isNotEmpty()) {
            item { SheetLabel("Logged", Modifier.padding(top = 10.dp)) }
            items(entries, key = { it.id }) { entry ->
                Box(Modifier.animateItem()) {
                    EntryRow(entry = entry, onDelete = { onDelete(entry) })
                }
            }
        }

        item { SearchField(query = query, onQueryChange = onQueryChange, modifier = Modifier.padding(top = 18.dp)) }

        if (parsed.items.isNotEmpty()) {
            item {
                val total = parsed.items.sumOf { it.kcal }
                val summary = parsed.items.joinToString(" · ") {
                    "${it.name} ${formatEditorialNumber(it.grams)} g"
                }
                QuickAddRow(
                    summary = summary,
                    kcal = total,
                    onClick = onQuickAdd,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }

        if (query.isNotBlank()) {
            item { SheetLabel("Results", Modifier.padding(top = 20.dp)) }
            if (choices.isEmpty()) {
                item {
                    Text(
                        text = "No match. Try a shorter word.",
                        color = VitalColors.inkMuted,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(choices, key = { it.name.lowercase() }) { choice ->
                    Box(Modifier.animateItem()) {
                        ChoiceRow(choice = choice, onClick = { onSelect(choice) })
                    }
                }
            }
        } else if (recent.isNotEmpty()) {
            item { SheetLabel("Recent", Modifier.padding(top = 22.dp)) }
            items(recent, key = { it.id }) { food ->
                Box(Modifier.animateItem()) {
                    ChoiceRow(
                        choice = FoodChoice(food.name, mapOf(food.variant to food)),
                        onClick = { onSelect(FoodChoice(food.name, mapOf(food.variant to food))) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStep(
    slot: MealSlot,
    choice: FoodChoice,
    gramsPerUnitFor: (Food) -> Double?,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onLog: (Food, Double, MealSlot) -> Unit
) {
    val initial = remember(choice) { choice.default }
    var variant by remember(choice) { mutableStateOf(initial.variant) }
    val active = choice.variants[variant] ?: initial
    var grams by remember(choice) { mutableStateOf("100") }
    var chosenSlot by remember(slot) { mutableStateOf(slot) }

    val gramsValue = grams.toDoubleOrNull()
    val valid = gramsValue != null && gramsValue > 0.0
    val factor = (gramsValue ?: 0.0) / 100.0
    val serving = gramsPerUnitFor(active)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 30.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹ ${choice.name.uppercase()}",
                    color = VitalColors.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.16.em,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onBack)
                        .padding(vertical = 4.dp)
                )
                Text(
                    text = "✕",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        if (choice.needsVariant) {
            item {
                VariantToggle(
                    selected = variant,
                    onSelect = { variant = it },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        item {
            GramsRow(
                grams = grams,
                onGramsChange = { grams = it },
                food = active,
                factor = factor,
                modifier = Modifier.padding(top = 18.dp)
            )
        }

        item {
            PresetRow(
                grams = grams,
                serving = serving,
                onPreset = { grams = formatPreset(it) },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item { SheetLabel("Meal", Modifier.padding(top = 22.dp)) }
        item {
            MealTabs(
                selected = chosenSlot,
                onSelect = { chosenSlot = it }
            )
        }

        item {
            PrimaryButton(
                text = if (valid) {
                    "Add ${formatEditorialNumber(gramsValue!!)} g · " +
                        "${formatEditorialNumber(active.kcalPer100 * factor)} kcal"
                } else {
                    "Enter grams"
                },
                enabled = valid,
                onClick = { if (valid) onLog(active, gramsValue!!, chosenSlot) },
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}

@Composable
private fun SheetLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.InterTight,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.16.em,
        modifier = modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .width(36.dp)
            .height(2.dp)
            .background(VitalColors.ruleStrong)
    )
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp)
            .heightIn(min = 46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = VitalColors.inkMuted,
            modifier = Modifier.width(19.dp)
        )
        Box(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search 600+ foods",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(VitalColors.orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Text(
                text = "✕",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onQueryChange("") }
                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun QuickAddRow(
    summary: String,
    kcal: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VitalColors.orange.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "QUICK ADD · PARSED",
                color = VitalColors.orange,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.10.em
            )
            Text(
                text = summary,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatEditorialNumber(kcal),
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 12.sp
            )
            Text(
                text = "kcal",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun ChoiceRow(choice: FoodChoice, onClick: () -> Unit) {
    val food = choice.default
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = choice.name,
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatEditorialNumber(food.proteinPer100)} P · " +
                        "${formatEditorialNumber(food.carbsPer100)} C · " +
                        "${formatEditorialNumber(food.fatPer100)} F / 100 g",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatEditorialNumber(food.kcalPer100),
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 12.sp
                )
                Text(
                    text = "kcal",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
        Hairline()
    }
}

@Composable
private fun EntryRow(entry: MealLogEntry, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.foodName,
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${entry.grams.roundToInt()} g · ${formatEditorialNumber(entry.kcal)} kcal",
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
                    .clickable(onClick = onDelete)
                    .padding(start = 8.dp, top = 6.dp, bottom = 6.dp)
            )
        }
        Hairline()
    }
}

@Composable
private fun VariantToggle(
    selected: FoodVariant,
    onSelect: (FoodVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(FoodVariant.RAW, FoodVariant.COOKED).forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .then(
                        if (isSelected) {
                            Modifier.background(VitalColors.orange, RoundedCornerShape(4.dp))
                        } else {
                            Modifier.border(1.dp, VitalColors.orange, RoundedCornerShape(4.dp))
                        }
                    )
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (isSelected) VitalColors.onAccent else VitalColors.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun GramsRow(
    grams: String,
    onGramsChange: (String) -> Unit,
    food: Food,
    factor: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        BasicTextField(
            value = grams,
            onValueChange = { next ->
                if (next.length <= 6 && next.all { it.isDigit() || it == '.' }) {
                    onGramsChange(next)
                }
            },
            singleLine = true,
            textStyle = TextStyle(
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 52.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.03).em
            ),
            cursorBrush = SolidColor(VitalColors.orange),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.width(112.dp)
        )
        Text(
            text = "g",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)
        )
        Spacer(Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .widthIn(min = 140.dp)
                .padding(bottom = 4.dp)
        ) {
            Text(
                text = "${formatEditorialNumber(food.kcalPer100 * factor)} kcal",
                color = VitalColors.orange,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 13.sp
            )
            Text(
                text = "${formatEditorialNumber(food.proteinPer100 * factor)} P · " +
                    "${formatEditorialNumber(food.carbsPer100 * factor)} C · " +
                    "${formatEditorialNumber(food.fatPer100 * factor)} F",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

@Composable
private fun PresetRow(
    grams: String,
    serving: Double?,
    onPreset: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = buildList {
        add(50.0)
        add(100.0)
        add(150.0)
        if (serving != null) add(serving) else add(200.0)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { preset ->
            val isSelected = grams.toDoubleOrNull() == preset
            val border = if (isSelected) VitalColors.orange else VitalColors.ruleStrong
            val label = if (preset == serving) "1 serving" else "${preset.roundToInt()} g"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .border(1.dp, border, RoundedCornerShape(10.dp))
                    .clickable { onPreset(preset) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) VitalColors.orange else VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MealTabs(
    selected: MealSlot,
    onSelect: (MealSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            MealSlot.entries.forEach { option ->
                val isSelected = option == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clickable { onSelect(option) }
                ) {
                    Text(
                        text = option.label(),
                        color = if (isSelected) VitalColors.ink else VitalColors.inkMuted,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) VitalColors.orange else Color.Transparent)
                    )
                }
            }
        }
        Hairline()
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                if (enabled) VitalColors.orange else VitalColors.orange.copy(alpha = 0.35f),
                RoundedCornerShape(4.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) VitalColors.onAccent else VitalColors.onAccent.copy(alpha = 0.5f),
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatPreset(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString()
    else value.toString()

// ---- dialogs -------------------------------------------------------------------------------

@Composable
private fun AmbiguityDialog(
    question: String,
    onChoose: (FoodVariant) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = "RAW OR COOKED?",
                style = dialogTitleStyle()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = question,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton(
                    "Raw",
                    onClick = { onChoose(FoodVariant.RAW) },
                    modifier = Modifier.weight(1f)
                )
                DialogButton(
                    "Cooked",
                    onClick = { onChoose(FoodVariant.COOKED) },
                    filled = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .then(
                if (filled) {
                    Modifier.background(
                        if (enabled) VitalColors.orange else VitalColors.orange.copy(alpha = 0.35f),
                        RoundedCornerShape(4.dp)
                    )
                } else {
                    Modifier.border(1.dp, VitalColors.orange, RoundedCornerShape(4.dp))
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = when {
                !enabled -> VitalColors.onAccent.copy(alpha = 0.5f)
                filled -> VitalColors.onAccent
                else -> VitalColors.orange
            },
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
