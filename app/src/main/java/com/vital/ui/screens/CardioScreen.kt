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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.CardioSession
import com.vital.data.entity.CardioType
import com.vital.domain.Acsm
import com.vital.ui.CardioViewModel
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.components.EditorialHeader
import com.vital.ui.components.EditorialSectionHeader
import com.vital.ui.components.Hairline
import com.vital.ui.components.formatEditorialNumber
import com.vital.ui.formatCardioDate
import com.vital.ui.formatCardioKcal
import com.vital.ui.formatCardioSpeed
import com.vital.ui.formatRestTime
import com.vital.ui.parseCardioDuration
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Cardio, restyled to the editorial-dark hybrid mock: a number with a plain hairline
 * instead of a gauge, underlined type tabs, a 2 x 2 entry grid, then recent sessions
 * and the ACSM estimate note.
 */
@Composable
fun CardioScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToWeeks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CardioViewModel = hiltViewModel()
) {
    val todayKcal by viewModel.todayKcal.collectAsStateWithLifecycle(0.0)
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle(0)
    val recent by viewModel.recentSessions.collectAsStateWithLifecycle(emptyList())
    val type by viewModel.type.collectAsStateWithLifecycle(CardioType.RUNNING)
    val durationText by viewModel.durationText.collectAsStateWithLifecycle("")
    val distanceText by viewModel.distanceText.collectAsStateWithLifecycle("")
    val speedText by viewModel.speedText.collectAsStateWithLifecycle("")
    val inclineText by viewModel.inclineText.collectAsStateWithLifecycle("")

    var detailSession by remember { mutableStateOf<CardioSession?>(null) }
    var showDetail by rememberSaveable { mutableStateOf(false) }

    val durationSec = parseCardioDuration(durationText)
    val speed = speedText.toDoubleOrNull()
    val outOfRange = (speed ?: 0.0) > Acsm.MAX_SPEED_KMH
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
                    title = "Cardio",
                    dateLabel = dateLabel
                )
            }
            item {
                CardioHero(
                    kcal = todayKcal,
                    sessions = todayCount,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                CardioTypeTabs(
                    selected = type,
                    onSelect = viewModel::setType,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
            item {
                CardioFieldGrid(
                    durationText = durationText,
                    distanceText = distanceText,
                    speedText = speedText,
                    inclineText = inclineText,
                    onDurationChange = viewModel::setDuration,
                    onDistanceChange = viewModel::setDistance,
                    onSpeedChange = viewModel::setSpeed,
                    onInclineChange = viewModel::setIncline,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
            item {
                EditorialButton(
                    text = "Log session",
                    onClick = viewModel::save,
                    enabled = durationSec != null,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
            item {
                RecentSection(
                    sessions = recent,
                    onSelect = { session ->
                        detailSession = session
                        showDetail = true
                    },
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
            item {
                EstimateNote(
                    outOfRange = outOfRange,
                    modifier = Modifier.padding(top = 26.dp)
                )
            }
        }
    }

    if (showDetail && detailSession != null) {
        CardioDetailDialog(
            session = detailSession!!,
            onDelete = {
                viewModel.delete(detailSession!!)
                showDetail = false
            },
            onDismiss = { showDetail = false }
        )
    }
}

// ---- hero ----------------------------------------------------------------------------------

@Composable
private fun CardioHero(
    kcal: Double,
    sessions: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialEyebrow(text = "Cardio kcal")
        Text(
            text = formatEditorialNumber(kcal),
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 72.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.055).em,
            modifier = Modifier.padding(top = 14.dp)
        )
        Hairline(modifier = Modifier.padding(top = 22.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "today",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.Medium)) {
                        append(sessions.toString())
                    }
                    append(if (sessions == 1) " session" else " sessions")
                },
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
    }
}

// ---- type tabs -----------------------------------------------------------------------------

@Composable
private fun CardioTypeTabs(
    selected: CardioType,
    onSelect: (CardioType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            items(CardioType.entries, key = { it.name }) { option ->
                val isSelected = option == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clickable { onSelect(option) }
                ) {
                    Text(
                        text = cardioTypeLabel(option),
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

// ---- fields --------------------------------------------------------------------------------

@Composable
private fun CardioFieldGrid(
    durationText: String,
    distanceText: String,
    speedText: String,
    inclineText: String,
    onDurationChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onSpeedChange: (String) -> Unit,
    onInclineChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            CardioField(
                label = "Duration",
                value = durationText,
                onValueChange = onDurationChange,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                startPadding = 0.dp,
                modifier = Modifier.weight(1f)
            )
            CellDivider()
            CardioField(
                label = "Distance · km",
                value = distanceText,
                onValueChange = onDistanceChange,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
                startPadding = 20.dp,
                modifier = Modifier.weight(1f)
            )
        }
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            CardioField(
                label = "Speed · km/h",
                value = speedText,
                onValueChange = onSpeedChange,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
                startPadding = 0.dp,
                modifier = Modifier.weight(1f)
            )
            CellDivider()
            CardioField(
                label = "Incline · %",
                value = inclineText,
                onValueChange = onInclineChange,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
                startPadding = 20.dp,
                modifier = Modifier.weight(1f)
            )
        }
        Hairline()
    }
}

@Composable
private fun CellDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(VitalColors.rule)
    )
}

@Composable
private fun CardioField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
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
        Box(modifier = Modifier.padding(top = 7.dp)) {
            if (value.isEmpty()) {
                Text(
                    text = "—",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.02).em
                ),
                cursorBrush = SolidColor(VitalColors.orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---- recent --------------------------------------------------------------------------------

@Composable
private fun RecentSection(
    sessions: List<CardioSession>,
    onSelect: (CardioSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Recent",
            trailing = if (sessions.isEmpty()) "none" else "${sessions.size} shown"
        )
        if (sessions.isEmpty()) {
            Text(
                text = "No sessions yet",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 13.dp)
            )
            Hairline()
        } else {
            sessions.forEach { session ->
                RecentRow(session = session, onClick = { onSelect(session) })
            }
        }
    }
}

@Composable
private fun RecentRow(session: CardioSession, onClick: () -> Unit) {
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
                text = cardioTypeLabel(session.type),
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                modifier = Modifier.width(92.dp)
            )
            Text(
                text = "${formatCardioKcal(session.kcal)} kcal",
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatCardioDate(session.date),
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
        Hairline()
    }
}

// ---- estimate ------------------------------------------------------------------------------

@Composable
private fun EstimateNote(
    outOfRange: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            title = "Estimate",
            trailing = "ACSM"
        )
        Hairline()
        Text(
            text = if (outOfRange) {
                "Above 6 km/h · the estimate is clamped."
            } else {
                "Valid up to 6 km/h · above that the estimate is clamped."
            },
            color = if (outOfRange) VitalColors.orange else VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Hairline()
    }
}

// ---- detail dialog -------------------------------------------------------------------------

@Composable
private fun CardioDetailDialog(
    session: CardioSession,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VitalColors.sheet, RoundedCornerShape(12.dp))
                .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "${cardioTypeLabel(session.type).uppercase()} · " +
                    formatCardioDate(session.date),
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.16.em
            )
            Spacer(Modifier.height(14.dp))
            DetailLine("Duration", formatRestTime(session.durationSec))
            session.distanceKm?.let { DetailLine("Distance", formatCardioDistance(it)) }
            session.speedKmh?.let { DetailLine("Speed", "${formatCardioSpeed(it)} km/h") }
            session.inclinePct?.let { DetailLine("Incline", "${formatCardioSpeed(it)} %") }
            DetailLine("Kcal", "${formatCardioKcal(session.kcal)} · ACSM est")
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogOutlineButton(
                    text = "Delete",
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
                DialogButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp
            )
            Text(
                text = value,
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
        Hairline()
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
            color = VitalColors.black,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DialogOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .border(1.dp, VitalColors.orange, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = VitalColors.orange,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---- helpers -------------------------------------------------------------------------------

private fun cardioTypeLabel(type: CardioType): String = when (type) {
    CardioType.RUNNING -> "Run"
    CardioType.WALKING -> "Walk"
    CardioType.CYCLING -> "Bike"
    CardioType.ROWING -> "Row"
    CardioType.SWIMMING -> "Swim"
    CardioType.ELLIPTICAL -> "Elliptical"
    CardioType.STAIR_CLIMBER -> "Stairs"
    CardioType.OTHER -> "Other"
}

private fun formatCardioDistance(km: Double): String {
    val rounded = Math.round(km * 100.0) / 100.0
    val text = if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
    return "$text km"
}
