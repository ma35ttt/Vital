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
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Keyboard
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
import androidx.compose.material.icons.Icons
import com.vital.ui.components.Tag
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Favorite

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
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

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

    var searchText by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VitalColors.Background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tag(text = dayTag, backgroundColor = VitalColors.AccentMLS)
                Text(
                    text = "MEALS",
                    color = VitalColors.PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Tag(text = dateTag, backgroundColor = VitalColors.AccentMLS)
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "SEARCH FOODS...", color = VitalColors.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    visualTransformation = VisualTransformation.None,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = VitalColors.NearBlackRow,
                        focusedContainerColor = VitalColors.NearBlackRow,
                        cursorColor = VitalColors.AccentMLS,
                        focusedPlaceholderColor = VitalColors.MutedText,                         unfocusedPlaceholderColor = VitalColors.MutedText,
                        focusedTextColor = VitalColors.PrimaryText,                         unfocusedTextColor = VitalColors.PrimaryText,
                        focusedLeadingIconColor = VitalColors.MutedText,                         unfocusedLeadingIconColor = VitalColors.MutedText,
                        focusedTrailingIconColor = VitalColors.MutedText,                         unfocusedTrailingIconColor = VitalColors.MutedText,
                        disabledContainerColor = VitalColors.NearBlackRow,
                        disabledTextColor = VitalColors.MutedText,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        errorTextColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = VitalColors.MutedText) },
                    trailingIcon = {
                        Row(modifier = Modifier.padding(end = 8.dp)) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.PhotoCamera, contentDescription = "Photo", tint = VitalColors.AccentMLS, modifier = Modifier
                                .fillMaxSize()
                                .clickable { /* TODO: Open food photo sheet */ }
                                .padding(16.dp)
                            )
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Keyboard, contentDescription = "Text entry", tint = VitalColors.AccentMLS, modifier = Modifier
                                .fillMaxSize()
                                .clickable { /* TODO: Open smart text parser */ }
                                .padding(16.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Hero Card on Accent Fill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(280.dp),
                color = VitalColors.AccentMLS,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: label and "Now"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Tag(
                            text = "KCAL CONSUMED",
                            backgroundColor = Color.Black.copy(alpha = 0.3f),
                            textColor = Color.Black
                        )
                        Text(
                            text = "NOW",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Big Kcal Display
                    BigKcalDisplay(
                        remaining = consumedKcal,
                        accentColor = Color.Black
                    )

                    // Macro Row
                    MacroRow(
                        protein = proteinCurrent,
                        carbs = carbsCurrent,
                        fat = fatCurrent,
                        proteinTarget = proteinTarget,
                        carbsTarget = carbsTarget,
                        fatTarget = fatTarget
                    )

                    // Hero Line Graph (inset grey panel)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(top = 8.dp),
                        color = VitalColors.InsetGreyPanel,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        HeroLineGraph(
                            modifier = Modifier.fillMaxSize(),
                            dataPoints = weeklyKcalData,
                            labels = weeklyLabels,
                            accentColor = Color.Black,
                            showTooltip = true,
                            tooltipIndex = weeklyKcalData.lastIndex,
                            tooltipValue = weeklyKcalData.lastOrNull() ?: 0f
                        )
                    }
                }
            }

            // MEALS Section
            SectionHeader(
                title = "MEALS",
                accentColor = VitalColors.AccentMLS,
                showMoreAction = onShowSummary
            )

            // Meal capsules
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(mealsToday) { index, meal ->
                    capsuleRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(meal.icon, "", tint = meal.accentColor) },
                        label = meal.name,
                        value = meal.value,
                        sparklineData = meal.sparklineData,
                        accentColor = meal.accentColor
                    )
                }

                // Empty state
                if (mealsToday.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO MEALS LOGGED TODAY",
                                color = VitalColors.MutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MealCapsule(
    val name: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color,
    val sparklineData: List<Float>
)

object MealsScreenPreviewData {
    val sampleMeals = listOf(
        MealCapsule("Breakfast", "520 kcal", Icons.Default.Favorite, VitalColors.AccentMLS, listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f)),
        MealCapsule("Lunch", "890 kcal", Icons.Default.Favorite, VitalColors.AccentMLS, listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.65f)),
        MealCapsule("Dinner", "720 kcal", Icons.Default.Favorite, VitalColors.AccentMLS, listOf(0.3f, 0.4f, 0.5f, 0.45f, 0.5f)),
        MealCapsule("Snacks", "180 kcal", Icons.Default.Favorite, VitalColors.AccentMLS, listOf(0.1f, 0.2f, 0.15f, 0.25f, 0.2f))
    )
}
