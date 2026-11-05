package com.vital.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.vital.ui.MealMethod
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
 * Meals, restyled to the editorial-dark hybrid mock. The smart-text / search logging
 * logic and the Room writes are unchanged from the M4 rebuild.
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
    val method by viewModel.method.collectAsStateWithLifecycle()
    val parsed by viewModel.parsed.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val eatenKcal by viewModel.eatenKcal.collectAsStateWithLifecycle()

    var pendingSlot by remember { mutableStateOf<MealSlot?>(null) }
    var ambiguitySlot by remember { mutableStateOf<MealSlot?>(null) }
    var showAmbiguity by remember { mutableStateOf(false) }
    var logFoodTarget by remember { mutableStateOf<Food?>(null) }
    var detailSlot by remember { mutableStateOf<MealSlot?>(null) }

    val target = settings.kcalTarget
    val remaining = (target - eatenKcal).coerceAtLeast(0.0)
    val progress = if (target > 0) (eatenKcal / target).toFloat().coerceIn(0f, 1f) else 0f

    val protein = meals.sumOf { it.proteinG }
    val carbs = meals.sumOf { it.carbsG }
    val fat = meals.sumOf { it.fatG }

    val add: () -> Unit = {
        val slot = pendingSlot ?: viewModel.defaultSlot()
        if (parsed.items.isNotEmpty()) {
            if (parsed.ambiguities.isNotEmpty()) {
                ambiguitySlot = slot
                showAmbiguity = true
            } else {
                viewModel.logParsed(slot)
                pendingSlot = null
            }
        }
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
                MealMethodTabs(
                    selected = method,
                    onSelect = viewModel::setMethod,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
            item {
                SmartTextRow(
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    onAdd = add,
                    modifier = Modifier.padding(top = 0.dp)
                )
            }
            if (parsed.ambiguities.isNotEmpty()) {
                item {
                    Text(
                        text = parsed.ambiguities.first().question.uppercase(),
                        color = VitalColors.orange,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.04.em,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clickable {
                                ambiguitySlot = pendingSlot ?: viewModel.defaultSlot()
                                showAmbiguity = true
                            }
                    )
                }
            }
            if (method == MealMethod.SEARCH && query.isNotBlank() && searchResults.isNotEmpty()) {
                items(searchResults, key = { it.id }) { food ->
                    FoodResultRow(food = food, onClick = { logFoodTarget = food })
                }
            }
            item {
                MealSlotsSection(
                    meals = meals,
                    eatenKcal = eatenKcal,
                    onSlotClick = { slot, entries ->
                        if (entries.isEmpty()) {
                            pendingSlot = slot
                            viewModel.setMethod(MealMethod.TEXT)
                        } else {
                            detailSlot = slot
                        }
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

    if (showAmbiguity && parsed.ambiguities.isNotEmpty()) {
        val ambiguity = parsed.ambiguities.first()
        AmbiguityDialog(
            question = ambiguity.question,
            onChoose = { variant ->
                viewModel.logParsed(
                    slot = ambiguitySlot ?: viewModel.defaultSlot(),
                    overrides = mapOf(ambiguity.foodName to variant)
                )
                showAmbiguity = false
                pendingSlot = null
            },
            onDismiss = { showAmbiguity = false }
        )
    }

    logFoodTarget?.let { food ->
        LogFoodDialog(
            food = food,
            defaultSlot = viewModel.defaultSlot(),
            onDismiss = { logFoodTarget = null },
            onSave = { grams, slot ->
                viewModel.logFood(food, grams, slot)
                logFoodTarget = null
            }
        )
    }

    detailSlot?.let { slot ->
        SlotDetailDialog(
            slot = slot,
            entries = meals.filter { it.mealSlot == slot },
            onDelete = viewModel::deleteEntry,
            onDismiss = { detailSlot = null }
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
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

// ---- method tabs ---------------------------------------------------------------------------

@Composable
private fun MealMethodTabs(
    selected: MealMethod,
    onSelect: (MealMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            MealMethod.entries.forEach { option ->
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

private fun MealMethod.label(): String = when (this) {
    MealMethod.SEARCH -> "Search"
    MealMethod.TEXT -> "Text"
    MealMethod.WEB -> "Web"
    MealMethod.RECIPE -> "Recipe"
}

// ---- smart text ----------------------------------------------------------------------------

@Composable
private fun SmartTextRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "chicken breast cooked 200g, rice 150g",
                        color = VitalColors.inkMuted,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
            Text(
                text = "Add",
                color = VitalColors.orange,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onAdd)
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
            )
        }
        Hairline()
    }
}

@Composable
private fun FoodResultRow(food: Food, onClick: () -> Unit) {
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
                text = if (food.variant == FoodVariant.NONE) {
                    food.name
                } else {
                    "${food.name} · ${food.variant.name.lowercase()}"
                },
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatEditorialNumber(food.kcalPer100)} / 100 g",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
        Hairline()
    }
}

// ---- slots ---------------------------------------------------------------------------------

@Composable
private fun MealSlotsSection(
    meals: List<MealLogEntry>,
    eatenKcal: Double,
    onSlotClick: (MealSlot, List<MealLogEntry>) -> Unit,
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
                onClick = { onSlotClick(slot, entries) }
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
private fun LogFoodDialog(
    food: Food,
    defaultSlot: MealSlot,
    onDismiss: () -> Unit,
    onSave: (Double, MealSlot) -> Unit
) {
    var grams by remember { mutableStateOf("100") }
    var slot by remember { mutableStateOf(defaultSlot) }
    val gramsValue = grams.toDoubleOrNull()
    val valid = gramsValue != null && gramsValue > 0.0

    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = food.name,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).em
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${formatEditorialNumber(food.kcalPer100)} kcal / 100 g",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(16.dp))
            NumberField(label = "Grams", value = grams, onValueChange = { grams = it })
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Slot",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MealSlot.entries.forEach { option ->
                    val isSelected = option == slot
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .clickable { slot = option }
                    ) {
                        Text(
                            text = option.label(),
                            color = if (isSelected) VitalColors.ink else VitalColors.inkMuted,
                            fontFamily = VitalFonts.JetBrainsMonoRegular,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
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
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton(
                    "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                DialogButton(
                    "Save",
                    onClick = { if (valid) onSave(gramsValue!!, slot) },
                    enabled = valid,
                    filled = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SlotDetailDialog(
    slot: MealSlot,
    entries: List<MealLogEntry>,
    onDelete: (MealLogEntry) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = slot.label().uppercase(),
                style = dialogTitleStyle()
            )
            Spacer(Modifier.height(10.dp))
            if (entries.isEmpty()) {
                Text(
                    text = "Nothing logged",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp
                )
            } else {
                entries.forEach { entry ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
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
                                    text = "${entry.grams.roundToInt()} g · " +
                                        "${formatEditorialNumber(entry.kcal)} kcal",
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
                                    .clickable { onDelete(entry) }
                                    .padding(start = 8.dp, top = 6.dp, bottom = 6.dp)
                            )
                        }
                        Hairline()
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            DialogButton(
                "Close",
                onClick = onDismiss,
                filled = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 11.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = { next ->
                    if (next.length <= 6 && next.all { it.isDigit() || it == '.' }) {
                        onValueChange(next)
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(VitalColors.orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Hairline()
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
                !enabled -> VitalColors.black.copy(alpha = 0.5f)
                filled -> VitalColors.black
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
