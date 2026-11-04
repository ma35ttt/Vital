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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.Food
import com.vital.data.entity.FoodVariant
import com.vital.data.entity.MealLogEntry
import com.vital.data.entity.MealSlot
import com.vital.ui.MealMethod
import com.vital.ui.MealsViewModel
import com.vital.ui.components.FabGlyph
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Meals, built to design/tabs/meals.md.
 *
 * The sheet starts at 405 du (ScreenScaffold), so the method tiles sit at 416 du: 3 du of
 * LazyColumn top padding after the scaffold's 8 du sheet padding. Tiles, field, pill and
 * slot rows are all 8 du apart, which is the measured pitch in the mock.
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

    val remaining = (settings.kcalTarget - eatenKcal).coerceAtLeast(0.0)

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

    ScreenScaffold(
        variant = ScreenVariant.OrangeHero,
        title = "MEALS",
        heroLabel = "KCAL LEFT",
        heroValue = formatKcal(remaining),
        onShowMore = onShowSummary,
        dialLetters = listOf("P", "C", "F"),
        dialHighlightedIndex = 0,
        dialInnerLabel = "EATEN",
        dialInnerValue = formatKcal(eatenKcal),
        dialInnerUnit = "",
        dialPillText = "OF ${formatKcal(settings.kcalTarget)}",
        dialTopPill = viewModel.defaultSlot().name,
        dialFabGlyph = FabGlyph.Plus
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.duToDp(),
                end = 16.duToDp(),
                top = 3.duToDp(),
                bottom = 16.duToDp()
            ),
            verticalArrangement = Arrangement.spacedBy(8.duToDp())
        ) {
            item {
                MethodTiles(
                    selected = method,
                    onSelect = viewModel::setMethod
                )
            }

            item {
                SmartTextField(
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    onAdd = add
                )
            }

            if (parsed.ambiguities.isNotEmpty()) {
                item {
                    AmbiguityPill(
                        text = parsed.ambiguities.first().question,
                        onClick = {
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

            items(MealSlot.values().toList(), key = { it.name }) { slot ->
                val entries = meals.filter { it.mealSlot == slot }
                SlotRow(
                    slot = slot,
                    entries = entries,
                    onClick = {
                        if (entries.isEmpty()) {
                            pendingSlot = slot
                            viewModel.setMethod(MealMethod.TEXT)
                        } else {
                            detailSlot = slot
                        }
                    }
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

// ---- method tiles -------------------------------------------------------------------------

@Composable
private fun MethodTiles(
    selected: MealMethod,
    onSelect: (MealMethod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.duToDp())
    ) {
        MethodTile("SEARCH", Icons.Default.Search, selected == MealMethod.SEARCH, Modifier.weight(1f)) {
            onSelect(MealMethod.SEARCH)
        }
        MethodTile("TEXT", Icons.Default.Keyboard, selected == MealMethod.TEXT, Modifier.weight(1f)) {
            onSelect(MealMethod.TEXT)
        }
        MethodTile("WEB", Icons.Default.Language, selected == MealMethod.WEB, Modifier.weight(1f)) {
            onSelect(MealMethod.WEB)
        }
        MethodTile("RECIPE", Icons.Default.Restaurant, selected == MealMethod.RECIPE, Modifier.weight(1f)) {
            onSelect(MealMethod.RECIPE)
        }
    }
}

@Composable
private fun MethodTile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(54.duToDp())
            .clickable(onClick = onClick),
        color = if (selected) VitalColors.orange else VitalColors.wellFill,
        shape = RoundedCornerShape(12.duToDp()),
        border = if (selected) BorderStroke(0.duToDp(), Color.Transparent)
        else BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) VitalColors.black else VitalColors.orange,
                modifier = Modifier.size(15.duToDp())
            )
            Spacer(Modifier.height(4.duToDp()))
            Text(
                text = label,
                color = if (selected) VitalColors.black else Color(0xFFD5D5D5),
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                letterSpacing = 0.1.em,
                lineHeight = 10.duToSp()
            )
        }
    }
}

// ---- smart text ---------------------------------------------------------------------------

@Composable
private fun SmartTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.duToDp())
            .background(VitalColors.wellFill, RoundedCornerShape(10.duToDp()))
            .border(1.5f.duToDp(), VitalColors.wellStroke, RoundedCornerShape(10.duToDp()))
    ) {
        if (query.isEmpty()) {
            Text(
                text = "chicken breast cooked 200g, rice 150g",
                color = Color(0xFFD3D3D3),
                fontSize = 14.duToSp(),
                fontWeight = FontWeight.Medium,
                fontFamily = VitalFonts.InterTight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.duToDp(), end = 76.duToDp())
            )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                color = VitalColors.textPrimary,
                fontSize = 14.duToSp(),
                fontWeight = FontWeight.Medium,
                fontFamily = VitalFonts.InterTight,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            cursorBrush = SolidColor(VitalColors.orange),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(start = 16.duToDp(), end = 76.duToDp())
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.duToDp())
        ) {
            Surface(
                modifier = Modifier
                    .size(51.duToDp(), 30.duToDp())
                    .clickable(onClick = onAdd),
                color = VitalColors.orange,
                shape = RoundedCornerShape(8.duToDp())
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "ADD",
                        color = VitalColors.black,
                        fontSize = 11.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        lineHeight = 11.duToSp()
                    )
                }
            }
        }
    }
}

@Composable
private fun AmbiguityPill(text: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .height(24.duToDp())
                .wrapContentWidth(Alignment.Start)
                .clickable(onClick = onClick),
            color = Color.Transparent,
            shape = RoundedCornerShape(12.duToDp()),
            border = BorderStroke(1.5f.duToDp(), VitalColors.orange)
        ) {
            Box(
                modifier = Modifier
                    .height(24.duToDp())
                    .padding(horizontal = 16.duToDp()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = VitalColors.orange,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.02.em,
                    lineHeight = 10.duToSp()
                )
            }
        }
    }
}

@Composable
private fun FoodResultRow(food: Food, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.duToDp())
            .clickable(onClick = onClick),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(21.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (food.variant == FoodVariant.NONE) food.name
                else "${food.name} · ${food.variant.name}",
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontWeight = FontWeight.Medium,
                fontFamily = VitalFonts.InterTight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatKcal(food.kcalPer100)} / 100G",
                color = VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontFamily = VitalFonts.JetBrainsMonoBold,
                lineHeight = 10.duToSp()
            )
        }
    }
}

// ---- slot rows ----------------------------------------------------------------------------

@Composable
private fun SlotRow(
    slot: MealSlot,
    entries: List<MealLogEntry>,
    onClick: () -> Unit
) {
    if (entries.isEmpty()) {
        EmptySlotRow(slot = slot, onClick = onClick)
        return
    }

    val kcal = entries.sumOf { it.kcal }
    val time = formatTime(entries.first().updatedAt)
    val summary = entries.joinToString(" · ") { "${it.foodName} ${it.grams.roundToInt()}g" }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.duToDp())
            .clickable(onClick = onClick),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(21.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 7.duToDp(), end = 16.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlotCircle(letter = slot.name.first().toString())
            Spacer(Modifier.width(11.duToDp()))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${slot.name} · $time",
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.1.em,
                    lineHeight = 10.duToSp()
                )
                Spacer(Modifier.height(3.duToDp()))
                Text(
                    text = summary,
                    color = Color(0xFFD8D8D8),
                    fontSize = 13.duToSp(),
                    fontWeight = FontWeight.Medium,
                    fontFamily = VitalFonts.InterTight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.duToSp()
                )
            }
            Spacer(Modifier.width(8.duToDp()))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatKcal(kcal),
                    color = VitalColors.textPrimary,
                    fontSize = 19.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.InterTight,
                    lineHeight = 19.duToSp()
                )
                Spacer(Modifier.width(3.duToDp()))
                Text(
                    text = "KCAL",
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    lineHeight = 10.duToSp(),
                    modifier = Modifier.padding(bottom = 2.duToDp())
                )
            }
        }
    }
}

@Composable
private fun EmptySlotRow(slot: MealSlot, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.duToDp())
            .clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val u = size.height / 42f
            val strokeWidth = 1.5f * u
            drawRoundRect(
                color = VitalColors.wellStroke.copy(alpha = 0.75f),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                cornerRadius = CornerRadius(size.height / 2 - strokeWidth / 2),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(8f * u, 8f * u)
                    )
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 7.duToDp(), end = 16.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlotCircle(letter = slot.name.first().toString())
            Spacer(Modifier.width(11.duToDp()))
            Column {
                Text(
                    text = slot.name,
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.1.em,
                    lineHeight = 10.duToSp()
                )
                Spacer(Modifier.height(3.duToDp()))
                Text(
                    text = "+ LOG ${slot.name}",
                    color = VitalColors.orange,
                    fontSize = 14.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.InterTight,
                    lineHeight = 14.duToSp()
                )
            }
        }
    }
}

@Composable
private fun SlotCircle(letter: String) {
    Box(modifier = Modifier.size(30.duToDp()), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 1.5f * (size.width / 30f)
            drawCircle(
                color = VitalColors.iconWellFill,
                center = Offset(size.width / 2, size.height / 2),
                radius = size.width / 2
            )
            drawCircle(
                color = VitalColors.iconWellStroke,
                center = Offset(size.width / 2, size.height / 2),
                radius = size.width / 2 - stroke / 2,
                style = Stroke(width = stroke)
            )
        }
        Text(
            text = letter,
            color = VitalColors.orange,
            fontSize = 11.duToSp(),
            fontWeight = FontWeight.Bold,
            fontFamily = VitalFonts.JetBrainsMonoBold,
            lineHeight = 11.duToSp()
        )
    }
}

// ---- dialogs ------------------------------------------------------------------------------

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
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                letterSpacing = 0.15.em
            )
            Spacer(Modifier.height(6.duToDp()))
            Text(
                text = question,
                color = VitalColors.textMuted,
                fontSize = 11.duToSp(),
                fontFamily = VitalFonts.JetBrainsMonoBold
            )
            Spacer(Modifier.height(16.duToDp()))
            Row(horizontalArrangement = Arrangement.spacedBy(10.duToDp())) {
                DialogButton("RAW", onClick = { onChoose(FoodVariant.RAW) }, modifier = Modifier.weight(1f))
                DialogButton(
                    "COOKED",
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
                text = food.name.uppercase(),
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                letterSpacing = 0.12.em
            )
            Spacer(Modifier.height(4.duToDp()))
            Text(
                text = "${formatKcal(food.kcalPer100)} KCAL / 100G",
                color = VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontFamily = VitalFonts.JetBrainsMonoBold
            )
            Spacer(Modifier.height(14.duToDp()))
            NumberField(label = "GRAMS", value = grams, onValueChange = { grams = it })
            Spacer(Modifier.height(12.duToDp()))
            Text(
                text = "SLOT",
                color = VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontFamily = VitalFonts.JetBrainsMonoBold
            )
            Spacer(Modifier.height(6.duToDp()))
            Row(horizontalArrangement = Arrangement.spacedBy(6.duToDp())) {
                MealSlot.values().forEach { option ->
                    SlotChip(
                        slot = option,
                        selected = option == slot,
                        onClick = { slot = option },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(16.duToDp()))
            Row(horizontalArrangement = Arrangement.spacedBy(10.duToDp())) {
                DialogButton("CANCEL", onClick = onDismiss, modifier = Modifier.weight(1f))
                DialogButton(
                    "SAVE",
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
                text = slot.name,
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                letterSpacing = 0.15.em
            )
            Spacer(Modifier.height(10.duToDp()))
            if (entries.isEmpty()) {
                Text(
                    text = "EMPTY",
                    color = VitalColors.textMuted,
                    fontSize = 11.duToSp(),
                    fontFamily = VitalFonts.JetBrainsMonoBold
                )
            } else {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.duToDp()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.foodName,
                                color = VitalColors.textPrimary,
                                fontSize = 13.duToSp(),
                                fontFamily = VitalFonts.InterTight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${entry.grams.roundToInt()} G · ${formatKcal(entry.kcal)} KCAL",
                                color = VitalColors.textMuted,
                                fontSize = 10.duToSp(),
                                fontFamily = VitalFonts.JetBrainsMonoBold
                            )
                        }
                        Text(
                            text = "DELETE",
                            color = VitalColors.orange,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = VitalFonts.JetBrainsMonoBold,
                            modifier = Modifier
                                .clickable { onDelete(entry) }
                                .padding(start = 8.duToDp(), top = 6.duToDp(), bottom = 6.duToDp())
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.duToDp()))
            DialogButton("CLOSE", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
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
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = VitalColors.textMuted,
            fontSize = 10.duToSp(),
            fontFamily = VitalFonts.JetBrainsMonoBold
        )
        Spacer(Modifier.height(5.duToDp()))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.duToDp())
                .background(VitalColors.black, RoundedCornerShape(8.duToDp()))
                .border(1.duToDp(), VitalColors.wellStroke, RoundedCornerShape(8.duToDp())),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = { next ->
                    if (next.length <= 6 && next.all { it.isDigit() || it == '.' }) onValueChange(next)
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = VitalColors.textPrimary,
                    fontSize = 22.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.InterTight,
                    textAlign = TextAlign.Center,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                cursorBrush = SolidColor(VitalColors.orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.duToDp())
            )
        }
    }
}

@Composable
private fun SlotChip(
    slot: MealSlot,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(30.duToDp())
            .clickable(onClick = onClick),
        color = if (selected) VitalColors.orange else Color.Transparent,
        shape = RoundedCornerShape(8.duToDp()),
        border = if (selected) null else BorderStroke(1.duToDp(), VitalColors.wellStroke)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = slot.name.take(1),
                color = if (selected) VitalColors.black else VitalColors.textMuted,
                fontSize = 11.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold
            )
        }
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
    Surface(
        modifier = modifier
            .height(46.duToDp())
            .clickable(enabled = enabled, onClick = onClick),
        color = if (filled) VitalColors.orange else Color.Transparent,
        shape = RoundedCornerShape(10.duToDp()),
        border = if (filled) null else BorderStroke(1.5f.duToDp(), VitalColors.orange)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = when {
                    !enabled -> VitalColors.textMuted
                    filled -> VitalColors.black
                    else -> VitalColors.orange
                },
                fontSize = 12.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                letterSpacing = 0.08.em
            )
        }
    }
}

// ---- formatting ---------------------------------------------------------------------------

private fun formatKcal(value: Number): String =
    String.format(Locale.US, "%,d", value.toDouble().roundToInt())

private fun formatTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
