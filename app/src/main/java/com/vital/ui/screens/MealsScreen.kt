package com.vital.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.SummaryDial
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import com.vital.ui.MealCapsule
import com.vital.ui.components.Tag
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Favorite
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.FabGlyph
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import com.vital.ui.theme.duToSp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun MealsScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val consumedKcal by viewModel.consumedKcal.collectAsStateWithLifecycle(0f)
    val remainingKcal by viewModel.remainingKcal.collectAsStateWithLifecycle(2500f)
    val proteinCurrent by viewModel.proteinCurrent.collectAsStateWithLifecycle(0f)
    val carbsCurrent by viewModel.carbsCurrent.collectAsStateWithLifecycle(0f)
    val fatCurrent by viewModel.fatCurrent.collectAsStateWithLifecycle(0f)
    val proteinTarget by viewModel.proteinTarget.collectAsStateWithLifecycle(180)
    val carbsTarget by viewModel.carbsTarget.collectAsStateWithLifecycle(250)
    val fatTarget by viewModel.fatTarget.collectAsStateWithLifecycle(85)
    val weeklyKcalData by viewModel.weeklyKcalData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels.collectAsStateWithLifecycle(emptyList())
    val mealsToday by viewModel.mealsToday.collectAsStateWithLifecycle(emptyList())
    val kcalTarget by viewModel.kcalTarget.collectAsStateWithLifecycle(2500)

    var searchText by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    ScreenScaffold(
        variant = ScreenVariant.OrangeHero,
        title = "MEALS",
        heroLabel = "KCAL LEFT",
        heroValue = "${remainingKcal.toInt()}",
        onShowMore = onShowSummary,
        // OrangeDial parameters for Meals
        dialLetters = listOf("P", "C", "F"),
        dialHighlightedIndex = 0, // P highlighted
        dialInnerLabel = "EATEN",
        dialInnerValue = "${consumedKcal.toInt()}",
        dialInnerUnit = "", // mock shows no unit inside the Meals disc
        dialPillText = "OF ${kcalTarget}",
        dialTopPill = "LUNCH",
        dialFabGlyph = FabGlyph.Plus
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Four method tiles (54 high, radius 12)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MethodTile(label = "SEARCH", icon = Icons.Default.Search, isSelected = true, modifier = Modifier.weight(1f), onClick = { /* switch to search */ })
                MethodTile(label = "TEXT", icon = Icons.Default.Keyboard, isSelected = false, modifier = Modifier.weight(1f), onClick = { /* switch to text parser */ })
                MethodTile(label = "WEB", icon = Icons.Default.Language, isSelected = false, modifier = Modifier.weight(1f), onClick = { /* switch to web search */ })
                MethodTile(label = "RECIPE", icon = Icons.Default.Restaurant, isSelected = false, modifier = Modifier.weight(1f), onClick = { /* switch to recipe */ })
            }

            // Smart-text field (44 high) with ADD button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(44.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "CHICKEN BREAST COOKED 200G, RICE 150G", color = VitalColors.textMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    visualTransformation = VisualTransformation.None,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = VitalColors.wellFill,
                        focusedContainerColor = VitalColors.wellFill,
                        cursorColor = VitalColors.orange,
                        focusedPlaceholderColor = VitalColors.textMuted,
                        unfocusedPlaceholderColor = VitalColors.textMuted,
                        focusedTextColor = VitalColors.textPrimary,
                        unfocusedTextColor = VitalColors.textPrimary,
                        focusedLeadingIconColor = VitalColors.textMuted,
                        unfocusedLeadingIconColor = VitalColors.textMuted,
                        focusedTrailingIconColor = VitalColors.textMuted,
                        unfocusedTrailingIconColor = VitalColors.textMuted,
                        disabledContainerColor = VitalColors.wellFill,
                        disabledTextColor = VitalColors.textMuted,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        errorTextColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        androidx.compose.material3.Surface(
                            modifier = Modifier
                                .size(51.dp, 30.dp)
                                .padding(end = 8.dp, top = 7.dp)
                                .clickable { /* ADD action */ },
                            color = VitalColors.orange,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ADD",
                                color = VitalColors.black,
                                fontSize = 11.duToSp(),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .wrapContentHeight(Alignment.CenterVertically)
                            )
                        }
                    }
                )
            }

            // Ambiguity pill (shown conditionally - for now always shown as placeholder)
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(top = 8.dp),
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, VitalColors.orange)
            ) {
                Text(
                    text = "RICE 150G · RAW OR COOKED?",
                    color = VitalColors.orange,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .padding(horizontal = 16.dp)
                )
            }

            // Four meal-slot capsule rows
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                val slots = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
                slots.forEachIndexed { index, slotName ->
                    val meal = mealsToday.firstOrNull { it.slot == slotName }
                    if (meal != null) {
                        item {
                            capsuleRow(
                                modifier = Modifier.fillMaxWidth(),
                                icon = { Icon(meal.icon, "", tint = meal.accentColor) },
                                label = meal.name,
                                value = meal.value,
                                sparklineData = meal.sparklineData,
                                accentColor = meal.accentColor
                            )
                        }
                    } else {
                        item {
                            EmptySlotRow(slot = slotName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) VitalColors.orange else VitalColors.wellFill,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.5.dp, VitalColors.wellStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) VitalColors.black else VitalColors.orange,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                color = if (isSelected) VitalColors.black else VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.1.em,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptySlotRow(slot: String) {
    // Dashed border using a Canvas overlay
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clickable { /* TODO: Open log meal for this slot */ }
    ) {
        // Background with rounded corners
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            shape = RoundedCornerShape(21.dp)
        ) {}
        // Dashed border drawn in Canvas
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cornerRadius = 21.dp.toPx()
            val dashLength = 8f
            val dashGap = 8f
            val strokeWidth = 1.5.dp.toPx()
            val color = VitalColors.wellStroke.copy(alpha = 0.5f)
            val width = size.width
            val height = size.height
            val diameter = cornerRadius * 2

            // Top line (left to right, minus corners)
            var x = cornerRadius
            val rightX = width - cornerRadius
            while (x < rightX) {
                val endX = kotlin.math.min(x + dashLength, rightX)
                drawLine(color, Offset(x, 0f), Offset(endX, 0f), strokeWidth)
                x = endX + dashGap
            }
            // Top-right corner arc (0° to 90°)
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(rightX, 0f),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
            // Right line (top to bottom, minus corners)
            var y = cornerRadius
            val bottomY = height - cornerRadius
            while (y < bottomY) {
                val endY = kotlin.math.min(y + dashLength, bottomY)
                drawLine(color, Offset(width, y), Offset(width, endY), strokeWidth)
                y = endY + dashGap
            }
            // Bottom-right corner arc (90° to 180°)
            drawArc(
                color = color,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(rightX, bottomY),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
            // Bottom line (right to left, minus corners)
            x = width - cornerRadius
            val leftX = cornerRadius
            while (x > leftX) {
                val startX = kotlin.math.max(x - dashLength, leftX)
                drawLine(color, Offset(startX, height), Offset(x, height), strokeWidth)
                x = startX - dashGap
            }
            // Bottom-left corner arc (180° to 270°)
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(0f, bottomY),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
            // Left line (bottom to top, minus corners)
            y = height - cornerRadius
            while (y > cornerRadius) {
                val startY = kotlin.math.max(y - dashLength, cornerRadius)
                drawLine(color, Offset(0f, startY), Offset(0f, y), strokeWidth)
                y = startY - dashGap
            }
            // Top-left corner arc (270° to 360° / -90° to 0°)
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(0f, 0f),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
        }
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time-letter circle
            Box(
                modifier = Modifier.size(30.dp),
                contentAlignment = Alignment.Center
            ) {
                // Draw circle with border using Canvas
                androidx.compose.foundation.Canvas(modifier = Modifier.size(30.dp)) {
                    drawCircle(
                        color = VitalColors.iconWellFill,
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width / 2
                    )
                    drawCircle(
                        color = VitalColors.iconWellStroke,
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width / 2,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                Text(
                    text = slot.first().toString(),
                    color = VitalColors.orange,
                    fontSize = 11.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = slot,
                color = VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.1.em
            )
            Text(
                text = "+ LOG $slot",
                color = VitalColors.orange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
