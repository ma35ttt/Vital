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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.Tag
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.SectionLabel
import com.vital.ui.components.WellRow
import com.vital.ui.components.CapsuleRow
import com.vital.ui.components.GreyPanel
import com.vital.ui.components.VitalChip
import com.vital.ui.components.VitalToggle
import com.vital.ui.theme.duToSp
import com.vital.ui.theme.duToDp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onOpenDebugShowcase: (() -> Unit)? = null,
    viewModel: MainViewModel = viewModel()
) {
    val trueBlack by viewModel.trueBlack.collectAsStateWithLifecycle(false)
    val aiProvider by viewModel.aiProvider.collectAsStateWithLifecycle("gemini")
    val kcalTarget by viewModel.kcalTarget.collectAsStateWithLifecycle(2500)
    val proteinTarget by viewModel.proteinTarget.collectAsStateWithLifecycle(180)
    val carbsTarget by viewModel.carbsTarget.collectAsStateWithLifecycle(250)
    val fatTarget by viewModel.fatTarget.collectAsStateWithLifecycle(85)
    val bodyWeightKg by viewModel.bodyWeightKg.collectAsStateWithLifecycle(80f)
    val restPresetSec by viewModel.restPresetSec.collectAsStateWithLifecycle(180)

    var showBiometric by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }

    ScreenScaffold(
        variant = ScreenVariant.BlackPage,
        title = "SETTINGS"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Content left/right edge is 12 and 380 (369 wide), not 16
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 0.dp, bottom = 100.dp) // bottom padding for nav
            ) {
                // DAILY TARGETS hero card - orange, radius ~18, at content top (y 98 from screen top)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(228.duToDp()),
                        color = VitalColors.orange,
                        shape = RoundedCornerShape(18.duToDp())
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 23.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // DAILY TARGETS tag
                            Text(
                                text = "DAILY TARGETS",
                                color = VitalColors.black,
                                fontSize = 11.duToSp(),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            // Kcal target row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 26.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = kcalTarget.toString(),
                                    color = VitalColors.black,
                                    fontSize = 76.duToSp(),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = -0.045.em
                                )
                                Text(
                                    text = "KCAL",
                                    color = VitalColors.black,
                                    fontSize = 17.duToSp(),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            // P / C / F labels and values
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 30.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                // Labels row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    MacroLabel("PROTEIN", modifier = Modifier.width(113.duToDp()))
                                    MacroLabel("CARBS", modifier = Modifier.width(113.duToDp()))
                                    MacroLabel("FAT", modifier = Modifier.width(113.duToDp()))
                                }
                                // Values row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    MacroValue("${proteinTarget} g", modifier = Modifier.width(113.duToDp()))
                                    MacroValue("${carbsTarget} g", modifier = Modifier.width(113.duToDp()))
                                    MacroValue("${fatTarget} g", modifier = Modifier.width(113.duToDp()))
                                }
                            }

                            // Macro-share bar
                            GreyPanel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.duToDp())
                                    .padding(top = 8.dp)
                            ) {
                                // Text: P 29% at x 36, C 40% centred, F 31% right-aligned to x 357
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "P ${(proteinTarget * 4 * 100 / (kcalTarget.toFloat())).toInt()}%",
                                        color = VitalColors.textPrimary,
                                        fontSize = 11.duToSp(),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "C ${(carbsTarget * 4 * 100 / (kcalTarget.toFloat())).toInt()}%",
                                        color = VitalColors.textPrimary,
                                        fontSize = 11.duToSp(),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "F ${(fatTarget * 9 * 100 / (kcalTarget.toFloat())).toInt()}%",
                                        color = VitalColors.textPrimary,
                                        fontSize = 11.duToSp(),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // PROFILE Section
                item { SectionLabel(title = "PROFILE") }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth()) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Person, "", tint = VitalColors.orange) },
                            label = "DISPLAY NAME",
                            value = "User",
                            onClick = { /* TODO: Edit name */ }
                        )
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth()) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Settings, "", tint = VitalColors.orange) },
                            label = "TRUE BLACK",
                            value = "PURE #000 FOR AMOLED",
                            trailing = {
                                VitalToggle(
                                    checked = trueBlack,
                                    onCheckedChange = { /* viewModel.setTrueBlack(it) */ }
                                )
                            }
                        )
                    }
                }

                // Two wells side by side: BODY WEIGHT and REST PRESET
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WellRow(modifier = Modifier.weight(1f).height(57.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "BODY WEIGHT",
                                    color = VitalColors.textMuted,
                                    fontSize = 10.duToSp(),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = bodyWeightKg.toString(),
                                        color = VitalColors.textPrimary,
                                        fontSize = 20.duToSp(),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = " KG",
                                        color = VitalColors.textMuted,
                                        fontSize = 10.duToSp(),
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        WellRow(modifier = Modifier.weight(1f).height(57.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "REST PRESET",
                                    color = VitalColors.textMuted,
                                    fontSize = 10.duToSp(),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${restPresetSec / 60}:${"%02d".format(restPresetSec % 60)}",
                                        color = VitalColors.textPrimary,
                                        fontSize = 20.duToSp(),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = " MIN",
                                        color = VitalColors.textMuted,
                                        fontSize = 10.duToSp(),
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // AI COACH Section
                item { SectionLabel(title = "AI COACH") }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                        // Segmented control
                        Row(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("gemini" to "GEMINI", "openrouter" to "OPENROUTER").forEach { (id, label) ->
                                val isSelected = aiProvider == id
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp)
                                        .clickable { /* viewModel.setAiProvider(id) */ },
                                    color = if (isSelected) VitalColors.orange else VitalColors.wellFill,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else VitalColors.wellStroke)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) VitalColors.black else VitalColors.textMuted,
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
                        }
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "••••••••••••••••",
                                color = VitalColors.textPrimary,
                                fontSize = 13.duToSp(),
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "SHOW",
                                color = VitalColors.orange,
                                fontSize = 10.duToSp(),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Show API key dialog */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // SECURITY Section
                item { SectionLabel(title = "SECURITY") }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(47.dp)) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Fingerprint, "", tint = VitalColors.orange) },
                            label = "BIOMETRIC UNLOCK",
                            trailing = {
                                VitalToggle(
                                    checked = showBiometric,
                                    onCheckedChange = { showBiometric = it; /* viewModel.setBiometricEnabled(it) */ }
                                )
                            }
                        )
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(47.dp)) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Key, "", tint = VitalColors.orange) },
                            label = "PIN CODE",
                            value = if (showPin) "SET" else "NOT SET",
                            trailing = {
                                Text(text = if (showPin) "CHANGE" else "SET", color = VitalColors.orange, fontSize = 12.duToSp(), fontWeight = FontWeight.Bold, modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPin = true; /* TODO: Show PIN dialog */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                                )
                            }
                        )
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(47.dp)) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Settings, "", tint = VitalColors.orange) },
                            label = "CHANGE PIN",
                            trailing = {
                                Icon(Icons.Default.Key, "", tint = VitalColors.textMuted, modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                }

                // NOTIFICATIONS Section
                item { SectionLabel(title = "NOTIFICATIONS") }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Info, "", tint = VitalColors.orange) },
                            label = "ALLOW NOTIFICATIONS",
                            value = "PERMISSION GRANTED",
                            trailing = {
                                VitalToggle(
                                    checked = true,
                                    onCheckedChange = { /* TODO: Link to system page */ }
                                )
                            }
                        )
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Restaurant, "", tint = VitalColors.orange) },
                            label = "MEAL REMINDERS",
                            value = "08:30 · 13:00 · 19:30",
                            trailing = {
                                VitalToggle(
                                    checked = true,
                                    onCheckedChange = { /* TODO */ }
                                )
                            }
                        )
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        SettingRow(
                            icon = { Icon(Icons.Default.FitnessCenter, "", tint = VitalColors.orange) },
                            label = "WORKOUT",
                            value = "MON WED FRI · 18:00",
                            trailing = {
                                VitalToggle(
                                    checked = true,
                                    onCheckedChange = { /* TODO */ }
                                )
                            }
                        )
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        SettingRow(
                            icon = { Icon(Icons.Default.LocalFireDepartment, "", tint = VitalColors.orange) },
                            label = "WATER",
                            value = "EVERY 2 H",
                            trailing = {
                                VitalToggle(
                                    checked = false,
                                    onCheckedChange = { /* TODO */ }
                                )
                            }
                        )
                    }
                }

                // DATA Section
                item { SectionLabel(title = "DATA") }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // EXPORT JSON - orange fill
                        WellRow(modifier = Modifier.weight(1f).height(47.dp)) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "EXPORT JSON",
                                    color = VitalColors.black,
                                    fontSize = 12.duToSp(),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { /* TODO: Export JSON */ }
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                        // IMPORT JSON - orange outline
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(47.dp)
                                .clickable { /* TODO: Import JSON */ },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(2.dp, VitalColors.orange)
                        ) {
                            Text(
                                text = "IMPORT JSON",
                                color = VitalColors.orange,
                                fontSize = 12.duToSp(),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .wrapContentHeight(Alignment.CenterVertically)
                            )
                        }
                    }
                }
                item {
                    // Note (2 lines) - at least 10-11 du text size
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 17.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "API keys and PIN are never exported",
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "and never overwritten by an import.",
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // WIDGETS Section - ON HOLD (do not build per brief 0A)
                // item { SectionLabel(title = "WIDGETS") }
                // item { /* Widget tiles - on hold */ }

                // ABOUT Section
                item { SectionLabel(title = "ABOUT") }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth()) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Info, "", tint = VitalColors.orange) },
                            label = "VERSION",
                            value = "1.0.0 (Debug)"
                        )
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth()) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Code, "", tint = VitalColors.orange) },
                            label = "BUILD",
                            value = "Local build"
                        )
                    }
                }
                onOpenDebugShowcase?.let { openShowcase ->
                    item {
                        WellRow(modifier = Modifier.fillMaxWidth()) {
                            SettingRow(
                                icon = { Icon(Icons.Default.BugReport, "", tint = VitalColors.orange) },
                                label = "DEBUG SHOWCASE",
                                trailing = {
                                    Text(
                                        text = "OPEN",
                                        color = VitalColors.orange,
                                        fontSize = 12.duToSp(),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                },
                                onClick = openShowcase
                            )
                        }
                    }
                }
                item {
                    WellRow(modifier = Modifier.fillMaxWidth()) {
                        SettingRow(
                            icon = { Icon(Icons.Default.Description, "", tint = VitalColors.orange) },
                            label = "LICENSES",
                            trailing = {
                                Text(text = "VIEW", color = VitalColors.orange, fontSize = 12.duToSp(), fontWeight = FontWeight.Bold, modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Show licenses */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String = "",
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = { onClick?.invoke() }),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Surface(
                modifier = Modifier.size(40.dp),
                color = VitalColors.iconWellFill,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, VitalColors.iconWellStroke)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = label, color = VitalColors.textPrimary, fontSize = 12.duToSp(), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
                if (value.isNotBlank()) {
                    Text(text = value, color = VitalColors.textMuted, fontSize = 14.duToSp(), fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
                }
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun MacroLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = VitalColors.black.copy(alpha = 0.7f),
        fontSize = 10.duToSp(),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.Start)
    )
}

@Composable
private fun MacroValue(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = VitalColors.black,
        fontSize = 20.duToSp(),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.Start)
    )
}

object SettingsScreenPreviewData {}