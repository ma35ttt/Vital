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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

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
                Tag(text = dayTag, backgroundColor = VitalColors.AccentSET)
                Text(
                    text = "SETTINGS",
                    color = VitalColors.PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Tag(text = dateTag, backgroundColor = VitalColors.AccentSET)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp)
            ) {
                // PROFILE Section
                item { SectionHeader(title = "PROFILE", accentColor = VitalColors.AccentSET) }
                item {
                    SettingsCard {
                        SettingRow(
                            icon = { Icon(Icons.Default.Person, "", tint = VitalColors.AccentSET) },
                            label = "DISPLAY NAME",
                            value = "User",
                            onClick = { /* TODO: Edit name */ }
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.Palette, "", tint = VitalColors.AccentSET) },
                            label = "THEME COLOR",
                            value = "SAFETY ORANGE",
                            trailing = {
                                ThemeColorPicker()
                            }
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.Settings, "", tint = VitalColors.AccentSET) },
                            label = "TRUE BLACK (#000000)",
                            trailing = {
                                Switch(
                                    checked = trueBlack,
                                    onCheckedChange = { /* viewModel.setTrueBlack(it) */ },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = VitalColors.AccentSET,
                                        checkedTrackColor = VitalColors.AccentSET.copy(alpha = 0.3f),
                                        uncheckedTrackColor = VitalColors.AccentSET.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        )
                    }
                }

                // TARGETS Section
                item { SectionHeader(title = "TARGETS", accentColor = VitalColors.AccentSET) }
                item {
                    SettingsCard {
                        TargetInputRow(label = "KCAL", value = kcalTarget.toString(), unit = "kcal", icon = Icons.Default.LocalFireDepartment, color = VitalColors.AccentTDY) { /* viewModel.setKcalTarget(it) */ }
                        Divider()
                        TargetInputRow(label = "PROTEIN", value = proteinTarget.toString(), unit = "g", icon = Icons.Default.FitnessCenter, color = VitalColors.MacroProtein) { /* viewModel.setProteinTarget(it) */ }
                        Divider()
                        TargetInputRow(label = "CARBS", value = carbsTarget.toString(), unit = "g", icon = Icons.Default.Grain, color = VitalColors.MacroCarbs) { /* viewModel.setCarbsTarget(it) */ }
                        Divider()
                        TargetInputRow(label = "FAT", value = fatTarget.toString(), unit = "g", icon = Icons.Default.OilBarrel, color = VitalColors.MacroFat) { /* viewModel.setFatTarget(it) */ }
                        Divider()
                        TargetInputRow(label = "BODY WEIGHT", value = bodyWeightKg.toString(), unit = "kg", icon = Icons.Default.MonitorWeight, color = VitalColors.AccentCRD) { /* viewModel.setBodyWeight(it) */ }
                        Divider()
                        TargetInputRow(label = "REST PRESET", value = "${restPresetSec / 60} min", unit = "", icon = Icons.Default.Timer, color = VitalColors.AccentLFT) { /* viewModel.setRestPreset(it) */ }
                    }
                }

                // AI Section
                item { SectionHeader(title = "AI COACH", accentColor = VitalColors.AccentSET) }
                item {
                    SettingsCard {
                        SettingRow(
                            icon = { Icon(Icons.Default.SmartToy, "", tint = VitalColors.AccentSET) },
                            label = "AI PROVIDER",
                            value = when (aiProvider) {
                                "gemini" -> "GEMINI 1.5 FLASH"
                                "openrouter" -> "OPENROUTER (FREE)"
                                else -> "GEMINI 1.5 FLASH"
                            },
                            trailing = {
                                AiProviderPicker(currentProvider = aiProvider) { /* viewModel.setAiProvider(it) */ }
                            }
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.Key, "", tint = VitalColors.AccentSET) },
                            label = "API KEY",
                            value = "••••••••••••••••",
                            trailing = {
                                Text(text = "CHANGE", color = VitalColors.AccentSET, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Show API key dialog */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                                )
                            }
                        )
                    }
                }

                // SECURITY Section
                item { SectionHeader(title = "SECURITY", accentColor = VitalColors.AccentSET) }
                item {
                    SettingsCard {
                        SettingRow(
                            icon = { Icon(Icons.Default.Fingerprint, "", tint = VitalColors.AccentSET) },
                            label = "BIOMETRIC UNLOCK",
                            trailing = {
                                Switch(
                                    checked = showBiometric,
                                    onCheckedChange = { showBiometric = it; /* viewModel.setBiometricEnabled(it) */ },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = VitalColors.AccentSET,
                                        checkedTrackColor = VitalColors.AccentSET.copy(alpha = 0.3f),
                                        uncheckedTrackColor = VitalColors.AccentSET.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.Key, "", tint = VitalColors.AccentSET) },
                            label = "PIN CODE",
                            value = if (showPin) "SET" else "NOT SET",
                            trailing = {
                                Text(text = if (showPin) "CHANGE" else "SET", color = VitalColors.AccentSET, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPin = true; /* TODO: Show PIN dialog */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                                )
                            }
                        )
                    }
                }

                // DATA Section
                item { SectionHeader(title = "DATA", accentColor = VitalColors.AccentSET) }
                item {
                    SettingsCard {
                        SettingRow(
                            icon = { Icon(Icons.Default.Download, "", tint = VitalColors.AccentSET) },
                            label = "EXPORT DATA",
                            value = "JSON (excludes API keys, PIN)",
                            trailing = {
                                Text(text = "EXPORT", color = VitalColors.AccentSET, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Export JSON */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                                )
                            }
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.Upload, "", tint = VitalColors.AccentSET) },
                            label = "IMPORT DATA",
                            value = "JSON",
                            trailing = {
                                Text(text = "IMPORT", color = VitalColors.AccentSET, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Import JSON */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                                )
                            }
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.DeleteForever, "", tint = VitalColors.AccentTDY) },
                            label = "CLEAR ALL DATA",
                            value = "Irreversible",
                            trailing = {
                                Text(text = "CLEAR", color = VitalColors.AccentTDY, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Confirm clear */ }
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp)
                                )
                            }
                        )
                    }
                }

                // ABOUT Section
                item { SectionHeader(title = "ABOUT", accentColor = VitalColors.AccentSET) }
                item {
                    SettingsCard {
                        SettingRow(
                            icon = { Icon(Icons.Default.Info, "", tint = VitalColors.AccentSET) },
                            label = "VERSION",
                            value = "1.0.0 (Debug)"
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.Code, "", tint = VitalColors.AccentSET) },
                            label = "BUILD",
                            value = "Local build"
                        )
                        Divider()
                        SettingRow(
                            icon = { Icon(Icons.Default.Description, "", tint = VitalColors.AccentSET) },
                            label = "LICENSES",
                            trailing = {
                                Text(text = "VIEW", color = VitalColors.AccentSET, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier
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
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VitalColors.Card,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VitalColors.NearBlackRowStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingRow(
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
                color = VitalColors.NearBlackRow,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = label, color = VitalColors.PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                if (value.isNotBlank()) {
                    Text(text = value, color = VitalColors.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun Divider() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(vertical = 4.dp)
    ) {
        drawLine(
            color = VitalColors.NearBlackRowStroke,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun TargetInputRow(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Surface(
                modifier = Modifier.size(40.dp),
                color = color.copy(alpha = 0.2f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = label, tint = color)
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = label, color = VitalColors.PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(80.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedTextColor = VitalColors.PrimaryText,
                    unfocusedTextColor = VitalColors.PrimaryText,
                    cursorColor = color,
                    focusedContainerColor = VitalColors.NearBlackRow,
                    unfocusedContainerColor = VitalColors.NearBlackRow,
                    disabledContainerColor = VitalColors.NearBlackRow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            if (unit.isNotBlank()) {
                Text(text = unit, color = VitalColors.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun ThemeColorPicker() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            VitalColors.AccentTDY to "TDY",
            VitalColors.AccentMLS to "MLS",
            VitalColors.AccentLFT to "LFT",
            VitalColors.AccentCRD to "CRD",
            VitalColors.AccentWKS to "WKS",
            VitalColors.AccentSET to "SET"
        ).forEach { (color, label) ->
            Surface(
                modifier = Modifier.size(28.dp).clickable { /* TODO: Select theme */ },
                color = color,
                shape = androidx.compose.foundation.shape.CircleShape,
                border = androidx.compose.foundation.BorderStroke(2.dp, VitalColors.PrimaryText.copy(alpha = 0.3f))
            ) {}
        }
    }
}

@Composable
fun AiProviderPicker(currentProvider: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("gemini" to "GEMINI", "openrouter" to "OPENROUTER").forEach { (id, label) ->
            val isSelected = currentProvider == id
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clickable { onSelect(id) },
                color = if (isSelected) VitalColors.AccentSET else VitalColors.NearBlackRow,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color.Transparent else VitalColors.NearBlackRowStroke)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.Black else VitalColors.MutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

object SettingsScreenPreviewData {}