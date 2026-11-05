package com.vital.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.AiProvider
import com.vital.data.entity.Settings
import com.vital.ui.SettingsViewModel
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.theme.ThemeSpec
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import com.vital.ui.theme.VitalThemes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Settings, restyled to the editorial-dark hybrid mock. The Room writes, the target
 * editor, the name editor, the key reveal and the notification permission jump are
 * unchanged from the M4 rebuild.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onOpenDebugShowcase: (() -> Unit)? = null,
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editingTargets by remember { mutableStateOf(false) }
    var editingKey by remember { mutableStateOf(false) }
    var keyRevealed by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }

    LaunchedEffect(settings.aiApiKeyEncrypted) { keyRevealed = false }

    val openNotificationSettings: () -> Unit = {
        val intent = Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
        runCatching { context.startActivity(intent) }
        Unit
    }

    val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val openOverlaySettings: () -> Unit = {
        val intent = Intent(
            AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        runCatching { context.startActivity(intent) }
        Unit
    }

    // "Display over other apps" can only be granted in system settings, so this deep-links
    // there and re-reads the state when the user comes back.
    var overlayAllowed by remember { mutableStateOf(AndroidSettings.canDrawOverlays(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        overlayAllowed = AndroidSettings.canDrawOverlays(context)
    }

    val dateLabel = remember {
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEE dd.MM", Locale.US))
            .uppercase()
    }

    Box(
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
                    title = "Settings",
                    dateLabel = dateLabel,
                    onBack = onBack
                )
            }
            item {
                TargetsHero(
                    settings = settings,
                    onEdit = { editingTargets = true },
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                TargetsMacroGrid(
                    settings = settings,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }

            item { SectionLabel("Profile", topPadding = 28.dp) }
            item {
                NameRow(
                    currentName = user?.name.orEmpty(),
                    onCommit = viewModel::setDisplayName
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    MetricCell(
                        label = "Body weight",
                        value = formatWeight(settings.bodyWeightKg),
                        unit = "kg",
                        startPadding = 0.dp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(VitalColors.rule)
                    )
                    MetricCell(
                        label = "Rest preset",
                        value = formatRest(settings.restPresetSec),
                        unit = "min",
                        startPadding = 20.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Hairline()
            }

            item { SectionLabel("Appearance", topPadding = 28.dp) }
            item {
                val theme = VitalThemes.byId(settings.themeId)
                ValueRow(
                    label = "Theme",
                    value = theme.name,
                    onClick = { showThemePicker = true },
                    trailing = { Chevron() }
                )
            }
            item {
                val theme = VitalThemes.byId(settings.themeId)
                ValueRow(
                    label = "True black",
                    value = when {
                        theme.isLight -> "Dark themes only"
                        settings.trueBlack -> "Pure #000 on dark themes"
                        else -> "Off"
                    },
                    trailing = {
                        EditorialSwitch(
                            checked = settings.trueBlack,
                            onCheckedChange = viewModel::setTrueBlack,
                            enabled = !theme.isLight
                        )
                    }
                )
            }

            item { SectionLabel("AI coach", topPadding = 28.dp) }
            item {
                ProviderTabs(
                    selected = settings.aiProvider,
                    onSelect = viewModel::setAiProvider
                )
            }
            item {
                val hasKey = !settings.aiApiKeyEncrypted.isNullOrEmpty()
                ValueRow(
                    label = "API key",
                    value = when {
                        !hasKey -> "Tap to add"
                        keyRevealed -> settings.aiApiKeyEncrypted.orEmpty()
                        else -> MASKED_KEY
                    },
                    mono = true,
                    onClick = { editingKey = true },
                    trailing = if (hasKey) {
                        {
                            Text(
                                text = if (keyRevealed) "Hide" else "Show",
                                color = VitalColors.orange,
                                fontFamily = VitalFonts.InterTight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { keyRevealed = !keyRevealed }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    } else {
                        { Chevron() }
                    }
                )
            }

            item { SectionLabel("Security", topPadding = 28.dp) }
            item {
                ValueRow(
                    label = "PIN lock",
                    value = if (user?.pinHash != null) "Set" else "Not set",
                    trailing = {
                        EditorialSwitch(
                            checked = user?.pinHash != null,
                            onCheckedChange = {},
                            enabled = false
                        )
                    }
                )
            }
            item {
                ValueRow(
                    label = "Biometric",
                    value = if (user?.biometricEnabled == true) "On" else "Off",
                    trailing = {
                        EditorialSwitch(
                            checked = user?.biometricEnabled == true,
                            onCheckedChange = {},
                            enabled = false
                        )
                    }
                )
            }
            item {
                ValueRow(
                    label = "Change PIN",
                    value = null,
                    trailing = { Chevron() }
                )
            }

            item { SectionLabel("Notifications", topPadding = 28.dp) }
            item {
                ValueRow(
                    label = "Allow notifications",
                    value = if (notificationsGranted) "Permission granted" else "Not granted",
                    onClick = openNotificationSettings,
                    trailing = {
                        EditorialSwitch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = viewModel::setNotificationsEnabled
                        )
                    }
                )
            }
            item {
                ValueRow(
                    label = "Meal reminders",
                    value = parseTimeList(settings.mealReminderTimesJson).joinToString(" · "),
                    trailing = {
                        EditorialSwitch(
                            checked = settings.mealRemindersEnabled,
                            onCheckedChange = viewModel::setMealRemindersEnabled
                        )
                    }
                )
            }
            item {
                ValueRow(
                    label = "Workout",
                    value = workoutSubtitle(settings),
                    trailing = {
                        EditorialSwitch(
                            checked = settings.workoutRemindersEnabled,
                            onCheckedChange = viewModel::setWorkoutRemindersEnabled
                        )
                    }
                )
            }
            item {
                ValueRow(
                    label = "Water",
                    value = waterSubtitle(settings.waterReminderIntervalMin),
                    trailing = {
                        EditorialSwitch(
                            checked = settings.waterRemindersEnabled,
                            onCheckedChange = viewModel::setWaterRemindersEnabled
                        )
                    }
                )
            }

            item { SectionLabel("Rest timer", topPadding = 28.dp) }
            item {
                ValueRow(
                    label = "Timer chip over other apps",
                    value = if (overlayAllowed) "Allowed" else "Not allowed",
                    onClick = openOverlaySettings,
                    trailing = { Chevron() }
                )
            }

            item { SectionLabel("Data", topPadding = 28.dp) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // EXPORT / IMPORT are drawn to the mock; the pickers arrive with the
                    // DATA step, so neither is wired yet.
                    DataButton(
                        text = "Export JSON",
                        filled = true,
                        modifier = Modifier.weight(1f)
                    )
                    DataButton(
                        text = "Import JSON",
                        filled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "AI keys and PIN are never exported or overwritten by an import.",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                )
                Hairline()
            }

            if (onOpenDebugShowcase != null) {
                item { SectionLabel("Debug", topPadding = 28.dp) }
                item {
                    ValueRow(
                        label = "Debug showcase",
                        value = null,
                        onClick = onOpenDebugShowcase,
                        trailing = { Chevron() }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        if (showThemePicker) {
            ThemePickerDialog(
                currentId = settings.themeId,
                trueBlack = settings.trueBlack,
                onSelect = viewModel::setTheme,
                onDismiss = { showThemePicker = false }
            )
        }
    }

    if (editingTargets) {
        TargetsEditorDialog(
            settings = settings,
            onDismiss = { editingTargets = false },
            onSave = { kcal, protein, carbs, fat ->
                viewModel.setDailyTargets(kcal, protein, carbs, fat)
                editingTargets = false
            }
        )
    }

    if (editingKey) {
        ApiKeyDialog(
            provider = settings.aiProvider,
            currentKey = settings.aiApiKeyEncrypted.orEmpty(),
            onDismiss = { editingKey = false },
            onSave = { key ->
                viewModel.setAiApiKey(key)
                editingKey = false
            }
        )
    }
}

// ---- hero ----------------------------------------------------------------------------------

@Composable
private fun TargetsHero(
    settings: Settings,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        EditorialEyebrow(text = "Daily targets")
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 14.dp)
        ) {
            Text(
                text = formatKcal(settings.kcalTarget),
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 58.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.05).em
            )
            Text(
                text = "kcal",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 9.dp)
            )
        }
        Text(
            text = "tap to edit",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun TargetsMacroGrid(
    settings: Settings,
    modifier: Modifier = Modifier
) {
    val macros = listOf(
        "Protein" to settings.proteinTarget,
        "Carbs" to settings.carbsTarget,
        "Fat" to settings.fatTarget
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            macros.forEachIndexed { index, (label, grams) ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(VitalColors.rule)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = if (index == 0) 0.dp else 20.dp,
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
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 7.dp)
                    ) {
                        Text(
                            text = grams.toString(),
                            color = VitalColors.ink,
                            fontFamily = VitalFonts.InterTight,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).em
                        )
                        Text(
                            text = "g",
                            color = VitalColors.inkMuted,
                            fontFamily = VitalFonts.InterTight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 3.dp, bottom = 3.dp)
                        )
                    }
                }
            }
        }
        Hairline()
    }
}

// ---- shared rows ---------------------------------------------------------------------------

@Composable
private fun SectionLabel(title: String, topPadding: androidx.compose.ui.unit.Dp) {
    EditorialSectionHeader(
        title = title,
        modifier = Modifier.padding(top = topPadding)
    )
}

@Composable
private fun ValueRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    mono: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(
                    text = value,
                    color = VitalColors.inkMuted,
                    fontFamily = if (mono) VitalFonts.JetBrainsMonoRegular else VitalFonts.InterTight,
                    fontSize = if (mono) 12.sp else 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .then(if (trailing == null) Modifier else Modifier.padding(end = 12.dp))
                )
            }
            trailing?.invoke()
        }
        Hairline()
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    unit: String,
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
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 7.dp)
        ) {
            Text(
                text = value,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).em
            )
            Text(
                text = unit,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 3.dp, bottom = 2.dp)
            )
        }
    }
}

/**
 * NAME row. Tapping the value edits in place and commits on Done or focus loss. The mock
 * draws no editor, so the field itself is the value.
 */
@Composable
private fun NameRow(
    currentName: String,
    onCommit: (String) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(currentName) }
    var hadFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun commit() {
        if (!editing) return
        editing = false
        val trimmed = draft.trim()
        if (trimmed.isNotEmpty() && trimmed != currentName) onCommit(trimmed)
    }

    LaunchedEffect(editing) {
        if (editing) focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = VitalColors.ink,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(VitalColors.orange),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    modifier = Modifier
                        .width(220.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (hadFocus && !state.isFocused) commit()
                            hadFocus = state.isFocused
                        }
                )
            } else {
                Text(
                    text = currentName.ifBlank { "—" },
                    color = if (currentName.isBlank()) VitalColors.inkMuted else VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .clickable {
                            draft = currentName
                            editing = true
                        }
                )
                Text(
                    text = "edit",
                    color = VitalColors.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            draft = currentName
                            editing = true
                        }
                        .padding(vertical = 4.dp)
                )
            }
        }
        Hairline()
    }
}

// ---- controls ------------------------------------------------------------------------------

@Composable
private fun EditorialSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 24.dp)
            .alpha(if (enabled) 1f else 0.6f)
            .background(
                color = when {
                    !enabled -> Color.Transparent
                    checked -> VitalColors.orange
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = when {
                    !enabled -> VitalColors.rule
                    checked -> VitalColors.orange
                    else -> VitalColors.ruleStrong
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        // Inset the knob by 4 dp with padding rather than offset, so the border stays 1 dp.
        Spacer(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(16.dp)
                .background(
                    color = when {
                        !enabled -> VitalColors.ruleStrong
                        checked -> VitalColors.onAccent
                        else -> VitalColors.inkMuted
                    },
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun ProviderTabs(
    selected: AiProvider,
    onSelect: (AiProvider) -> Unit
) {
    val options = listOf(
        AiProvider.GEMINI to "Gemini",
        AiProvider.OPENROUTER to "OpenRouter"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            options.forEach { (provider, label) ->
                val isSelected = provider == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clickable { onSelect(provider) }
                ) {
                    Text(
                        text = label,
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
private fun DataButton(
    text: String,
    filled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .then(
                if (filled) {
                    Modifier.background(VitalColors.orange, RoundedCornerShape(4.dp))
                } else {
                    Modifier.border(1.dp, VitalColors.orange, RoundedCornerShape(4.dp))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (filled) VitalColors.onAccent else VitalColors.orange,
            fontFamily = VitalFonts.InterTight,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Chevron() {
    Text(
        text = "›",
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.InterTight,
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

// ---- theme picker --------------------------------------------------------------------------

/**
 * Full-screen theme gallery. Each card previews its own palette directly (not the live
 * tokens), so every card stays stable while the app recolours underneath. Tapping a card
 * applies the theme app-wide and the surrounding chrome — including this dialog — recolours
 * live; Close returns to Settings.
 */
@Composable
private fun ThemePickerDialog(
    currentId: String,
    trueBlack: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Rendered as an in-window overlay rather than a full-screen Dialog. A full-screen Dialog
    // measures its content against the whole display while the dialog window itself sits below the
    // status bar, which clipped the last rows. As an overlay the content is measured against the
    // real page area, so every theme card stays reachable.
    BackHandler { onDismiss() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VitalColors.paper)
            // Consume taps so they cannot reach the Settings page behind the overlay.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    EditorialEyebrow(text = "Appearance")
                    Text(
                        text = "Theme",
                        color = VitalColors.ink,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.03).em,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Text(
                    text = "Close",
                    color = VitalColors.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(8.dp)
                )
            }
            if (trueBlack) {
                Text(
                    text = "True black is on · dark themes use a pure #000 background",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                ThemeGroup(
                    label = "Base themes",
                    themes = VitalThemes.base,
                    currentId = currentId,
                    onSelect = onSelect,
                    topPadding = 22.dp
                )
                ThemeGroup(
                    label = "Ten new themes",
                    themes = VitalThemes.featured,
                    currentId = currentId,
                    onSelect = onSelect,
                    topPadding = 30.dp
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ThemeGroup(
    label: String,
    themes: List<ThemeSpec>,
    currentId: String,
    onSelect: (String) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp
) {
    Column(modifier = Modifier.padding(top = topPadding)) {
        EditorialSectionHeader(title = label, trailing = themes.size.toString())
        themes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                row.forEach { theme ->
                    ThemePreviewCard(
                        theme = theme,
                        selected = theme.id == currentId,
                        onClick = { onSelect(theme.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: ThemeSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = theme.palette
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(p.paper, RoundedCornerShape(14.dp))
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) VitalColors.orange else p.rule,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "KCAL LEFT",
                    color = p.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.18.em
                )
                Text(
                    text = "1,260",
                    color = p.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.04).em,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(1.dp)
                        .background(p.rule)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(1.dp)
                            .background(p.orange)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "of 2,500",
                        color = p.inkMuted,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "50%",
                        color = p.inkMuted,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 9.sp
                    )
                }
            }
            if (selected) {
                Text(
                    text = "✓",
                    color = p.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = theme.name,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (theme.isLight) "LIGHT" else "DARK",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 9.sp
            )
        }
    }
}

// ---- targets dialog ------------------------------------------------------------------------

@Composable
private fun TargetsEditorDialog(
    settings: Settings,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var kcal by remember(settings.kcalTarget) { mutableStateOf(settings.kcalTarget.toString()) }
    var protein by remember(settings.proteinTarget) { mutableStateOf(settings.proteinTarget.toString()) }
    var carbs by remember(settings.carbsTarget) { mutableStateOf(settings.carbsTarget.toString()) }
    var fat by remember(settings.fatTarget) { mutableStateOf(settings.fatTarget.toString()) }

    val kcalValue = kcal.toIntOrNull()
    val proteinValue = protein.toIntOrNull()
    val carbsValue = carbs.toIntOrNull()
    val fatValue = fat.toIntOrNull()
    val valid = kcalValue != null && kcalValue in 1..20000 &&
        proteinValue != null && proteinValue in 0..2000 &&
        carbsValue != null && carbsValue in 0..2000 &&
        fatValue != null && fatValue in 0..2000

    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = "DAILY TARGETS",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.16.em
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Kcal and macro targets. The hero and the day's remaining numbers follow these.",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(16.dp))
            TargetField(label = "Kcal", value = kcal, onValueChange = { kcal = it })
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TargetField("Protein g", protein, { protein = it }, Modifier.weight(1f))
                TargetField("Carbs g", carbs, { carbs = it }, Modifier.weight(1f))
                TargetField("Fat g", fat, { fat = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                DialogButton(
                    text = "Save",
                    onClick = {
                        if (valid) onSave(kcalValue!!, proteinValue!!, carbsValue!!, fatValue!!)
                    },
                    enabled = valid,
                    filled = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(
    provider: AiProvider,
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var draft by remember(currentKey) { mutableStateOf(currentKey) }
    val providerName = if (provider == AiProvider.GEMINI) "Gemini" else "OpenRouter"

    Dialog(onDismissRequest = onDismiss) {
        DialogShell {
            Text(
                text = "AI API KEY",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.16.em
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$providerName key. Stored on this device and used only for your messages.",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .border(1.dp, VitalColors.rule, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = VitalColors.ink,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 12.sp
                    ),
                    cursorBrush = SolidColor(VitalColors.orange),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) {
                            Text(
                                text = "Paste API key...",
                                color = VitalColors.inkMuted,
                                fontFamily = VitalFonts.JetBrainsMonoRegular,
                                fontSize = 12.sp
                            )
                        }
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (currentKey.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Remove key",
                    color = VitalColors.orange,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onSave("") }
                        .padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                DialogButton(
                    text = "Save",
                    onClick = { onSave(draft) },
                    enabled = draft.isNotBlank(),
                    filled = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TargetField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = { next ->
                    if (next.length <= 5 && next.all { it.isDigit() }) onValueChange(next)
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = VitalColors.ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = VitalFonts.InterTight,
                    textAlign = TextAlign.Start
                ),
                cursorBrush = SolidColor(VitalColors.orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Hairline()
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

// ---- helpers -------------------------------------------------------------------------------

private const val MASKED_KEY = "••••••••••••••••••••••"

private fun formatKcal(kcal: Int): String = String.format(Locale.US, "%,d", kcal)

private fun formatWeight(kg: Double): String =
    if (kg % 1.0 == 0.0) kg.toInt().toString() else String.format(Locale.US, "%.1f", kg)

private fun formatRest(seconds: Int): String =
    "%d:%02d".format(Locale.US, seconds / 60, seconds % 60)

private fun workoutSubtitle(settings: Settings): String {
    val days = parseIntList(settings.workoutReminderDaysJson)
        .mapNotNull { day -> dayNames.getOrNull(day - 1) }
    val time = settings.workoutReminderTime.takeIf { it.isNotBlank() }
    return when {
        days.isEmpty() && time == null -> "Off"
        days.isEmpty() -> time.orEmpty()
        else -> "${days.joinToString(" ")} · ${time.orEmpty()}"
    }
}

private val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private fun waterSubtitle(intervalMin: Int): String = when {
    intervalMin <= 0 -> "Off"
    intervalMin % 60 == 0 -> "Every ${intervalMin / 60} h"
    else -> "Every $intervalMin min"
}

/** Reads `["08:00","13:00"]` without pulling in a JSON dependency. */
private fun parseTimeList(json: String): List<String> =
    Regex("\"([0-9]{2}:[0-9]{2})\"").findAll(json).map { it.groupValues[1] }.toList()

private fun parseIntList(json: String): List<Int> =
    Regex("\\d+").findAll(json).map { it.value.toInt() }.toList()
