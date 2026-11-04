package com.vital.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.AiProvider
import com.vital.data.entity.Settings
import com.vital.ui.SettingsViewModel
import com.vital.ui.components.GreyPanel
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.VitalToggle
import com.vital.ui.components.WellRow
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp
import java.util.Locale

// Settings, built to design/tabs/settings.md (settings-top.png / settings-scrolled.png).
//
// Geometry: ScreenScaffold's BlackPage variant already places content at x 12..380 (369 wide)
// and y 98, and reserves the bottom 100 du for the nav. Everything below is measured from that
// content box, so this file adds no horizontal padding of its own and every offset is in du.
//
// Two deviations from the mock are deliberate and recorded in NOTES.md:
//  1. The two-line notification rows are 54 du high with a 62 du pitch (settings.md FIX 0A.6),
//     not the 46/53 the mock shows, so every section below NOTIFICATIONS sits 27 du lower.
//  2. The hero kcal number is Inter Tight 700 76 du (md "~76") with -0.055em tracking, which
//     reproduces the measured 198 du ink span of "2,500"; the md's KCAL size of 17 is an EST
//     and the ink cap measures 20.5 du, which is what is used here.
private const val HERO_HEIGHT = 228f
private const val SECTION_LABEL_HEIGHT = 13f
private const val ROW_ONE_LINE = 47f
private const val ROW_TWO_LINE = 54f
private const val ROW_PROFILE = 57f
private const val ROW_KEY = 48f
private const val ROW_BUTTON = 47f

private const val GAP_AFTER_HERO = 20.7f
private const val GAP_LABEL_TO_ROW = 11.3f
private const val GAP_BETWEEN_ROWS = 8f // settings.md FIX 0A.6
private const val GAP_BETWEEN_CELLS = 7f // BODY WEIGHT 12..193, REST PRESET 200..381
private const val GAP_PROFILE_TO_LABEL = 17.7f
private const val GAP_BEFORE_ROW = 10.3f
private const val GAP_AFTER_LABEL = 12.3f
private const val GAP_BEFORE_LABEL = 16.7f
private const val GAP_TIGHT_ROWS = 6f
private const val GAP_NOTIFICATION_ROWS = 8f
private const val GAP_LABEL_TO_BUTTONS = 9.3f
private const val GAP_BUTTONS_TO_NOTE = 7.7f

private const val INSET_ROW_TEXT = 16f
private const val INSET_SECTION_LABEL = 5f
private const val INSET_TOGGLE = 15f
private const val INSET_CHEVRON = 19f
private const val INSET_KEY_TEXT = 17f

private val DAY_NAMES = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

/**
 * Settings tab.
 *
 * [modifier] is deliberately not applied: ScreenScaffold fills the window and anchors its own
 * content (y 98, bottom 100 du) and header, while the Scaffold in VitalNavigation already passes
 * its inner padding, which would double both insets. The parameter stays for call-site
 * compatibility and is reported as a follow-up in NOTES.md.
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
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editingTargets by remember { mutableStateOf(false) }
    var keyRevealed by remember { mutableStateOf(false) }

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

    ScreenScaffold(
        variant = ScreenVariant.BlackPage,
        title = "SETTINGS"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // ---- DAILY TARGETS hero ------------------------------------------------
            DailyTargetsHero(
                settings = settings,
                onEdit = { editingTargets = true }
            )

            Spacer(Modifier.height(GAP_AFTER_HERO.duToDp()))
            SettingsSectionLabel("PROFILE")

            // ---- PROFILE -----------------------------------------------------------
            Spacer(Modifier.height(GAP_LABEL_TO_ROW.duToDp()))
            NameRow(
                currentName = user?.name.orEmpty(),
                onCommit = viewModel::setDisplayName
            )

            Spacer(Modifier.height(GAP_BETWEEN_ROWS.duToDp()))
            SettingsTwoLineRow(
                label = "TRUE BLACK",
                sub = "PURE #000 FOR AMOLED",
                trailing = {
                    VitalToggle(
                        checked = settings.trueBlack,
                        onCheckedChange = viewModel::setTrueBlack
                    )
                }
            )

            Spacer(Modifier.height(GAP_BETWEEN_ROWS.duToDp()))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ROW_PROFILE.duToDp()),
                horizontalArrangement = Arrangement.spacedBy(GAP_BETWEEN_CELLS.duToDp())
            ) {
                SettingsValueRow(
                    modifier = Modifier.weight(1f),
                    label = "BODY WEIGHT",
                    value = formatWeight(settings.bodyWeightKg),
                    unit = "KG"
                )
                SettingsValueRow(
                    modifier = Modifier.weight(1f),
                    label = "REST PRESET",
                    value = formatRest(settings.restPresetSec),
                    unit = "MIN"
                )
            }

            // ---- AI COACH ----------------------------------------------------------
            Spacer(Modifier.height(GAP_PROFILE_TO_LABEL.duToDp()))
            SettingsSectionLabel("AI COACH")

            Spacer(Modifier.height(GAP_AFTER_LABEL.duToDp()))
            ProviderSegmented(
                selected = settings.aiProvider,
                onSelect = viewModel::setAiProvider
            )

            Spacer(Modifier.height(GAP_NOTIFICATION_ROWS.duToDp()))
            val hasKey = !settings.aiApiKeyEncrypted.isNullOrEmpty()
            SettingsRow(height = ROW_KEY) {
                MonoText(
                    text = when {
                        !hasKey -> "NO KEY SET"
                        keyRevealed -> settings.aiApiKeyEncrypted.orEmpty()
                        else -> MASKED_KEY
                    },
                    size = 13f,
                    color = if (hasKey) VitalColors.textPrimary else VitalColors.textMuted,
                    tracking = 0.02f,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = INSET_KEY_TEXT.duToDp())
                        .padding(end = if (hasKey) 62.duToDp() else 0.duToDp())
                )
                if (hasKey) {
                    MonoText(
                        text = if (keyRevealed) "HIDE" else "SHOW",
                        size = 11f,
                        color = VitalColors.orange,
                        tracking = 0.08f,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = INSET_KEY_TEXT.duToDp())
                            .clickable { keyRevealed = !keyRevealed }
                    )
                }
            }

            // ---- SECURITY ----------------------------------------------------------
            Spacer(Modifier.height(GAP_BEFORE_LABEL.duToDp()))
            SettingsSectionLabel("SECURITY")

            Spacer(Modifier.height(GAP_BEFORE_ROW.duToDp()))
            SettingsRow(height = ROW_ONE_LINE) {
                MonoText(
                    text = "PIN LOCK",
                    size = 12f,
                    color = VitalColors.textPrimary,
                    tracking = LABEL_TRACKING,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = INSET_ROW_TEXT.duToDp())
                )
                // Real state (User.pinHash), not interactive until the M6 auth gate.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = INSET_TOGGLE.duToDp())
                ) {
                    VitalToggle(
                        checked = user?.pinHash != null,
                        onCheckedChange = {},
                        enabled = false
                    )
                }
            }

            Spacer(Modifier.height(GAP_TIGHT_ROWS.duToDp()))
            SettingsRow(height = ROW_ONE_LINE) {
                MonoText(
                    text = "BIOMETRIC UNLOCK",
                    size = 12f,
                    color = VitalColors.textPrimary,
                    tracking = LABEL_TRACKING,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = INSET_ROW_TEXT.duToDp())
                )
                // Real state (User.biometricEnabled), not interactive until the M6 auth gate.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = INSET_TOGGLE.duToDp())
                ) {
                    VitalToggle(
                        checked = user?.biometricEnabled == true,
                        onCheckedChange = {},
                        enabled = false
                    )
                }
            }

            Spacer(Modifier.height(GAP_TIGHT_ROWS.duToDp()))
            SettingsRow(height = ROW_ONE_LINE) {
                MonoText(
                    text = "CHANGE PIN",
                    size = 12f,
                    color = VitalColors.textPrimary,
                    tracking = LABEL_TRACKING,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = INSET_ROW_TEXT.duToDp())
                )
                ChevronIcon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = INSET_CHEVRON.duToDp())
                )
            }

            // ---- NOTIFICATIONS -----------------------------------------------------
            Spacer(Modifier.height(GAP_BEFORE_LABEL.duToDp()))
            SettingsSectionLabel("NOTIFICATIONS")

            Spacer(Modifier.height(GAP_BEFORE_ROW.duToDp()))
            SettingsTwoLineRow(
                label = "ALLOW NOTIFICATIONS",
                sub = if (notificationsGranted) "PERMISSION GRANTED" else "PERMISSION NOT GRANTED",
                onClick = openNotificationSettings,
                trailing = {
                    VitalToggle(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                }
            )

            Spacer(Modifier.height(GAP_NOTIFICATION_ROWS.duToDp()))
            SettingsTwoLineRow(
                label = "MEAL REMINDERS",
                sub = parseTimeList(settings.mealReminderTimesJson).joinToString(" · "),
                trailing = {
                    VitalToggle(
                        checked = settings.mealRemindersEnabled,
                        onCheckedChange = viewModel::setMealRemindersEnabled
                    )
                }
            )

            Spacer(Modifier.height(GAP_NOTIFICATION_ROWS.duToDp()))
            SettingsTwoLineRow(
                label = "WORKOUT",
                sub = workoutSubtitle(settings),
                trailing = {
                    VitalToggle(
                        checked = settings.workoutRemindersEnabled,
                        onCheckedChange = viewModel::setWorkoutRemindersEnabled
                    )
                }
            )

            Spacer(Modifier.height(GAP_NOTIFICATION_ROWS.duToDp()))
            SettingsTwoLineRow(
                label = "WATER",
                sub = waterSubtitle(settings.waterReminderIntervalMin),
                trailing = {
                    VitalToggle(
                        checked = settings.waterRemindersEnabled,
                        onCheckedChange = viewModel::setWaterRemindersEnabled
                    )
                }
            )

            // ---- DATA --------------------------------------------------------------
            Spacer(Modifier.height(GAP_BEFORE_ROW.duToDp()))
            SettingsSectionLabel("DATA")

            Spacer(Modifier.height(GAP_LABEL_TO_BUTTONS.duToDp()))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ROW_BUTTON.duToDp()),
                horizontalArrangement = Arrangement.spacedBy(GAP_BETWEEN_CELLS.duToDp())
            ) {
                // EXPORT JSON / IMPORT JSON are drawn to the mock; the file pickers and the
                // export/import code arrive in the DATA step (NOTES.md), so neither is wired.
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    color = VitalColors.orange,
                    shape = RoundedCornerShape(10.duToDp())
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        MonoText("EXPORT JSON", 12f, VitalColors.black, LABEL_TRACKING)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.duToDp()),
                    border = BorderStroke(2.duToDp(), VitalColors.orange)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        MonoText("IMPORT JSON", 12f, VitalColors.orange, LABEL_TRACKING)
                    }
                }
            }

            Spacer(Modifier.height(GAP_BUTTONS_TO_NOTE.duToDp()))
            MonoText(
                text = "AI KEYS AND PIN ARE NEVER EXPORTED OR OVERWRITTEN BY AN\nIMPORT.",
                size = 10.5f,
                color = VitalColors.textMuted,
                tracking = 0f,
                lineHeight = 15f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = INSET_SECTION_LABEL.duToDp())
            )

            // ---- DEBUG -------------------------------------------------------------
            // WIDGETS in the mock is not built (brief 4.5). The debug entry point lives in this
            // slot instead, so the screen keeps its only non-mock entry point.
            Spacer(Modifier.height(GAP_BEFORE_LABEL.duToDp()))
            SettingsSectionLabel("DEBUG")

            Spacer(Modifier.height(GAP_BEFORE_ROW.duToDp()))
            SettingsRow(height = ROW_ONE_LINE, onClick = onOpenDebugShowcase) {
                MonoText(
                    text = "DEBUG SHOWCASE",
                    size = 12f,
                    color = VitalColors.textPrimary,
                    tracking = LABEL_TRACKING,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = INSET_ROW_TEXT.duToDp())
                )
                ChevronIcon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = INSET_CHEVRON.duToDp())
                )
            }

            Spacer(Modifier.height(24.duToDp()))
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
}

// ---- hero ---------------------------------------------------------------------------------

@Composable
private fun DailyTargetsHero(
    settings: Settings,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT.duToDp())
            .clip(RoundedCornerShape(18.duToDp()))
            .background(VitalColors.orange)
            .clickable(onClick = onEdit)
    ) {
        MonoText(
            text = "DAILY TARGETS",
            size = 11f,
            color = VitalColors.black,
            tracking = 0.15f,
            modifier = Modifier.offset(x = 14.3f.duToDp(), y = 21.6f.duToDp())
        )

        InterText(
            text = formatKcal(settings.kcalTarget),
            size = 76f,
            color = VitalColors.black,
            weight = FontWeight.Bold,
            tracking = -0.055f,
            modifier = Modifier.offset(x = 10.2f.duToDp(), y = 32.1f.duToDp())
        )

        MonoText(
            text = "KCAL",
            size = 18f,
            color = VitalColors.black,
            tracking = 0.1f,
            modifier = Modifier.offset(x = 218f.duToDp(), y = 87f.duToDp())
        )

        val columns = listOf(15f, 129f, 242f)
        val macros = listOf(
            "PROTEIN" to settings.proteinTarget,
            "CARBS" to settings.carbsTarget,
            "FAT" to settings.fatTarget
        )
        macros.forEachIndexed { index, (label, grams) ->
            MonoText(
                text = label,
                size = 10f,
                color = VitalColors.black.copy(alpha = 0.7f),
                tracking = 0.12f,
                modifier = Modifier.offset(
                    x = columns[index].duToDp(),
                    y = 123.7f.duToDp()
                )
            )
            InterText(
                text = "$grams g",
                size = 20f,
                color = VitalColors.black,
                weight = FontWeight.Bold,
                modifier = Modifier.offset(
                    x = columns[index].duToDp(),
                    y = 141.1f.duToDp()
                )
            )
        }

        MacroShareBar(
            proteinG = settings.proteinTarget,
            carbsG = settings.carbsTarget,
            fatG = settings.fatTarget,
            modifier = Modifier.offset(x = 12.duToDp(), y = 180.duToDp())
        )
    }
}

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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = VitalColors.wellFill,
            shape = RoundedCornerShape(18.duToDp()),
            border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
        ) {
            Column(modifier = Modifier.padding(18.duToDp())) {
                MonoText("DAILY TARGETS", 13f, VitalColors.textPrimary, 0.15f)
                Spacer(Modifier.height(6.duToDp()))
                MonoText(
                    "KCAL AND MACRO TARGETS. THE HERO AND THE DAY'S REMAINING NUMBERS FOLLOW THESE.",
                    size = 10f,
                    color = VitalColors.textMuted,
                    tracking = 0.02f,
                    lineHeight = 14f
                )
                Spacer(Modifier.height(16.duToDp()))
                TargetField(label = "KCAL", value = kcal, onValueChange = { kcal = it })
                Spacer(Modifier.height(10.duToDp()))
                Row(horizontalArrangement = Arrangement.spacedBy(10.duToDp())) {
                    TargetField("PROTEIN G", protein, { protein = it }, Modifier.weight(1f))
                    TargetField("CARBS G", carbs, { carbs = it }, Modifier.weight(1f))
                    TargetField("FAT G", fat, { fat = it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(18.duToDp()))
                Row(horizontalArrangement = Arrangement.spacedBy(10.duToDp())) {
                    DialogButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    DialogButton(
                        text = "SAVE",
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
}

@Composable
private fun TargetField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        MonoText(label, 9.5f, VitalColors.textMuted, 0.08f)
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
                    if (next.length <= 5 && next.all { it.isDigit() }) onValueChange(next)
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
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.duToDp())
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
            MonoText(
                text = text,
                size = 12f,
                color = when {
                    !enabled -> VitalColors.textMuted
                    filled -> VitalColors.black
                    else -> VitalColors.orange
                },
                tracking = 0.08f
            )
        }
    }
}

/** Grey `P 29%  C 40%  F 31%` bar, 345 x 36 at hero-relative (12, 180). */
@Composable
private fun MacroShareBar(
    proteinG: Int,
    carbsG: Int,
    fatG: Int,
    modifier: Modifier = Modifier
) {
    val proteinKcal = proteinG * KCAL_PER_G_PROTEIN
    val carbsKcal = carbsG * KCAL_PER_G_CARBS
    val fatKcal = fatG * KCAL_PER_G_FAT
    val total = (proteinKcal + carbsKcal + fatKcal).coerceAtLeast(1)

    GreyPanel(
        modifier = modifier
            .width(345.duToDp())
            .height(36.duToDp())
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.duToDp()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonoText("P ${share(proteinKcal, total)}%", 11f, VitalColors.textPrimary, 0.02f)
            MonoText("C ${share(carbsKcal, total)}%", 11f, VitalColors.textPrimary, 0.02f)
            MonoText("F ${share(fatKcal, total)}%", 11f, VitalColors.textPrimary, 0.02f)
        }
    }
}

// ---- rows ---------------------------------------------------------------------------------

@Composable
private fun SettingsRow(
    height: Float,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    WellRow(
        modifier = Modifier
            .height(height.duToDp())
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
    ) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

/** 54 du row: label top padding 11, sub-label bottom padding 11 (settings.md FIX 0A.6). */
@Composable
private fun SettingsTwoLineRow(
    label: String,
    sub: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    SettingsRow(height = ROW_TWO_LINE, onClick = onClick) {
        // TopStart, not CenterStart: centring a padded column makes the top padding only
        // half-effective, which pushed the label ~7 du down (measured).
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = INSET_ROW_TEXT.duToDp(), top = 11.duToDp())
        ) {
            // Boxes pinned to their nominal sizes: a 12 du Text lays out ~15.8 du because the
            // font's ascent + descent exceeds lineHeight, which pushed the sub-label down and
            // broke the md's "11 du top, 11 du bottom" geometry (FIX 0A.6).
            MonoText(
                label, 12f, VitalColors.textPrimary, LABEL_TRACKING,
                modifier = Modifier.height(12.duToDp())
            )
            Spacer(Modifier.height(9.5f.duToDp()))
            MonoText(
                sub, 10.5f, VitalColors.textMuted, SUB_TRACKING,
                modifier = Modifier.height(10.5f.duToDp())
            )
        }
        trailing?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = INSET_TOGGLE.duToDp())
            ) {
                it()
            }
        }
    }
}

/** Half-width well (181 du): label at (13.5, 9.7), value + unit at (13.5, 27.4). */
@Composable
private fun SettingsValueRow(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    WellRow(modifier = modifier.height(ROW_PROFILE.duToDp())) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 13.5f.duToDp(), top = 9.7f.duToDp())
        ) {
            MonoText(
                label, 10f, VitalColors.textMuted, SUB_TRACKING,
                modifier = Modifier.height(10.duToDp())
            )
            Spacer(Modifier.height(7.7f.duToDp()))
            Row(verticalAlignment = Alignment.Bottom) {
                InterText(value, 20f, VitalColors.textPrimary, FontWeight.Bold)
                Spacer(Modifier.width(3.duToDp()))
                MonoText(
                    text = unit,
                    size = 10f,
                    color = VitalColors.textMuted,
                    tracking = SUB_TRACKING,
                    modifier = Modifier.padding(bottom = 3.5f.duToDp())
                )
            }
        }
    }
}

/**
 * NAME row. Tapping the pencil edits in place; the name is written on Done or when the field
 * loses focus. The mock draws no edit state, so the editor is the value itself with an orange
 * caret and nothing else added.
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

    val displayName = currentName.ifBlank { "—" }

    SettingsRow(height = ROW_PROFILE) {
        // TopStart for the same reason as SettingsTwoLineRow: the 9.7 du top padding must be
        // measured from the row's top edge, not halved by centre alignment.
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = INSET_ROW_TEXT.duToDp(), top = 9.7f.duToDp())
        ) {
            MonoText("NAME", 10f, VitalColors.textMuted, SUB_TRACKING)
            Spacer(Modifier.height(6.8f.duToDp()))
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = VitalColors.textPrimary,
                        fontSize = 19.duToSp(),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = VitalFonts.InterTight,
                        lineHeight = 19.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    cursorBrush = SolidColor(VitalColors.orange),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    modifier = Modifier
                        .width(240.duToDp())
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (hadFocus && !state.isFocused) commit()
                            hadFocus = state.isFocused
                        }
                )
            } else {
                InterText(
                    text = displayName,
                    size = 19f,
                    color = if (currentName.isBlank()) VitalColors.textMuted
                    else VitalColors.textPrimary,
                    weight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 19.duToDp())
                .clickable {
                    if (editing) {
                        commit()
                    } else {
                        draft = currentName
                        editing = true
                    }
                }
        ) {
            PencilIcon()
        }
    }
}

// ---- segmented control --------------------------------------------------------------------

@Composable
private fun ProviderSegmented(
    selected: AiProvider,
    onSelect: (AiProvider) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.duToDp()),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(10.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.duToDp())
        ) {
            SegmentCell(
                label = "GEMINI",
                selected = selected == AiProvider.GEMINI,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(AiProvider.GEMINI) }
            )
            SegmentCell(
                label = "OPENROUTER",
                selected = selected == AiProvider.OPENROUTER,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(AiProvider.OPENROUTER) }
            )
        }
    }
}

@Composable
private fun SegmentCell(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.duToDp()))
            .background(if (selected) VitalColors.orange else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        MonoText(
            text = label,
            size = 11f,
            color = if (selected) VitalColors.black else VitalColors.textMuted,
            tracking = 0.06f
        )
    }
}

// ---- text ---------------------------------------------------------------------------------

private const val LABEL_TRACKING = 0.06f
private const val SUB_TRACKING = 0.06f
private const val MASKED_KEY = "••••••••••••••••••••••"
private const val KCAL_PER_G_PROTEIN = 4
private const val KCAL_PER_G_CARBS = 4
private const val KCAL_PER_G_FAT = 9

/**
 * Section label pinned to its nominal 13 du box. A `Text` with `lineHeight = 13` still lays
 * out ~16.7 du, because JetBrains Mono's ascent + descent is ~1.32 em and the font's own line
 * box wins over a smaller lineHeight. That extra 3.7 du pushed every row below a label down,
 * and is what made PROFILE / AI COACH read 3-6 du low against settings.md. The 2.5 du lift is
 * the measured ink offset of the small mono glyphs inside their box.
 */
@Composable
private fun SettingsSectionLabel(title: String) {
    MonoText(
        text = title,
        size = SECTION_LABEL_HEIGHT,
        color = VitalColors.textPrimary,
        tracking = 0.24f,
        modifier = Modifier
            .fillMaxWidth()
            .height(SECTION_LABEL_HEIGHT.duToDp())
            .offset(y = (-2.5f).duToDp())
            .padding(start = INSET_SECTION_LABEL.duToDp())
    )
}

@Composable
private fun MonoText(
    text: String,
    size: Float,
    color: Color,
    tracking: Float,
    modifier: Modifier = Modifier,
    lineHeight: Float = size,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.duToSp(),
        fontWeight = FontWeight.Bold,
        fontFamily = VitalFonts.JetBrainsMonoBold,
        letterSpacing = tracking.em,
        textAlign = textAlign,
        lineHeight = lineHeight.duToSp(),
        maxLines = maxLines,
        overflow = overflow,
        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
    )
}

@Composable
private fun InterText(
    text: String,
    size: Float,
    color: Color,
    weight: FontWeight,
    tracking: Float = 0f,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.duToSp(),
        fontWeight = weight,
        fontFamily = VitalFonts.InterTight,
        letterSpacing = tracking.em,
        lineHeight = size.duToSp(),
        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
    )
}

// ---- icons (stroke only, 1.8 du round caps) ------------------------------------------------

/** Outlined pencil, 14 x 14, tip to the lower left. */
@Composable
private fun PencilIcon(modifier: Modifier = Modifier) {
    val size14 = 14.duToDp()
    Canvas(modifier = modifier.size(size14)) {
        // 1 du in pixels: the canvas is exactly 14 du on a side.
        val u = size.width / 14f
        val stroke = Stroke(
            width = 1.8f * u,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        rotate(degrees = -45f, pivot = center) {
            // Body: a 8 x 4.8 bar in the upper half, tip: a triangle at its left end.
            val bodyLeft = 5.2f * u
            val bodyRight = 13.2f * u
            val bodyTop = 4.6f * u
            val bodyBottom = 9.4f * u
            drawRoundRect(
                color = VitalColors.textMuted,
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyRight - bodyLeft, bodyBottom - bodyTop),
                cornerRadius = CornerRadius(0.8f * u),
                style = stroke
            )
            val tip = Path().apply {
                moveTo(bodyLeft, bodyTop)
                lineTo(bodyLeft, bodyBottom)
                lineTo(0.9f * u, center.y)
                close()
            }
            drawPath(tip, color = VitalColors.textMuted, style = stroke)
        }
    }
}

/** 6 x 11 chevron pointing right. */
@Composable
private fun ChevronIcon(modifier: Modifier = Modifier) {
    val chevronSize = Modifier.size(width = 6.duToDp(), height = 11.duToDp())
    Canvas(modifier = modifier.then(chevronSize)) {
        val stroke = 1.8f * (size.height / 11f)
        val inset = stroke / 2f
        val path = Path().apply {
            moveTo(inset, inset)
            lineTo(size.width - inset, size.height / 2f)
            lineTo(inset, size.height - inset)
        }
        drawPath(
            path = path,
            color = VitalColors.textMuted,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// ---- formatting --------------------------------------------------------------------------

private fun formatKcal(kcal: Int): String = String.format(Locale.US, "%,d", kcal)

private fun formatWeight(kg: Double): String =
    if (kg == kg.toLong().toDouble()) kg.toLong().toString()
    else String.format(Locale.US, "%.1f", kg)

private fun formatRest(seconds: Int): String =
    String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60)

private fun share(part: Int, total: Int): Int = Math.round(part * 100f / total)

private fun workoutSubtitle(settings: Settings): String {
    val days = parseIntList(settings.workoutReminderDaysJson).distinct().sorted()
    val daysText = when {
        days.isEmpty() -> null
        days.size == 7 -> "EVERY DAY"
        else -> days.mapNotNull { DAY_NAMES.getOrNull(it - 1) }.joinToString(" ")
    }
    return listOfNotNull(daysText, settings.workoutReminderTime).joinToString(" · ")
}

private fun waterSubtitle(intervalMin: Int): String = when {
    intervalMin <= 0 -> "OFF"
    intervalMin % 60 == 0 -> "EVERY ${intervalMin / 60} H"
    else -> "EVERY $intervalMin MIN"
}

// The reminder fields are seed-written JSON strings (Settings entity). These read them without
// pulling the serialization runtime into the screen.
private fun parseTimeList(json: String): List<String> =
    Regex("\"([^\"]+)\"").findAll(json).map { it.groupValues[1] }.toList()

private fun parseIntList(json: String): List<Int> =
    Regex("\\d+").findAll(json).mapNotNull { it.value.toIntOrNull() }.toList()
