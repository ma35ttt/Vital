package com.vital.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.CardioSession
import com.vital.data.entity.CardioType
import com.vital.domain.Acsm
import com.vital.ui.CardioViewModel
import com.vital.ui.components.FabGlyph
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.Sparkline
import com.vital.ui.components.insetShadow
import com.vital.ui.formatCardioDate
import com.vital.ui.formatCardioKcal
import com.vital.ui.formatCardioSpeed
import com.vital.ui.formatRestTime
import com.vital.ui.parseCardioDuration
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Cardio, built to docs/measurements/cardio.md. The sheet starts at 405 du; the LazyColumn
 * top padding is 11 du, then 8 du between every block: tiles 416, scan row 478, fields 532
 * and 584, footer 636, RECENT 680, capsule 700.
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
    val incline = inclineText.toDoubleOrNull()
    val outOfRange = (speed ?: 0.0) > Acsm.MAX_SPEED_KMH

    val heroValue = todayKcal.roundToInt().toString()
    val dialValue = speed?.let { formatCardioSpeed(it) } ?: "—"
    val dialUnit = if (speed != null) "km/h" else ""
    val dialPill = "INCL ${incline?.let { formatCardioSpeed(it) } ?: "—"}%"
    val dialTopPill = durationSec?.let { formatRestTime(it) } ?: "—:—"
    val dialHighlight = when (type) {
        CardioType.RUNNING -> 0
        CardioType.CYCLING -> 1
        else -> 2
    }

    val sparkline = remember(recent) {
        val points = recent.reversed().map { it.kcal.toFloat() }
        if (points.size == 1) listOf(points[0], points[0]) else points
    }

    DesignUnitsProvider {
        ScreenScaffold(
            variant = ScreenVariant.OrangeHero,
            title = "CARDIO",
            heroLabel = "CARDIO KCAL",
            heroValue = heroValue,
            onShowMore = onShowSummary,
            dialLetters = listOf("R", "B", "W"),
            dialHighlightedIndex = dialHighlight,
            dialInnerLabel = "SPEED",
            dialInnerValue = dialValue,
            dialInnerUnit = dialUnit,
            dialPillText = dialPill,
            dialTopPill = dialTopPill,
            dialFabGlyph = FabGlyph.Camera
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.duToDp(),
                    end = 16.duToDp(),
                    top = 11.duToDp(),
                    // Clear the glass nav: it overlays the sheet, so the last RECENT row
                    // needs extra scroll room to finish above it.
                    bottom = 96.duToDp()
                ),
                verticalArrangement = Arrangement.spacedBy(8.duToDp())
            ) {
                item {
                    TypeTiles(
                        selected = type,
                        onSelect = viewModel::setType
                    )
                }
                item {
                    ScanRow()
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.duToDp()),
                        horizontalArrangement = Arrangement.spacedBy(8.duToDp())
                    ) {
                        CardioField(
                            label = "DURATION",
                            value = durationText,
                            unit = "",
                            placeholder = "—",
                            keyboardType = KeyboardType.Text,
                            onValueChange = viewModel::setDuration,
                            modifier = Modifier.weight(1f)
                        )
                        CardioField(
                            label = "DISTANCE",
                            value = distanceText,
                            unit = "KM",
                            placeholder = "—",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = viewModel::setDistance,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.duToDp()),
                        horizontalArrangement = Arrangement.spacedBy(8.duToDp())
                    ) {
                        CardioField(
                            label = "SPEED",
                            value = speedText,
                            unit = "KM/H",
                            placeholder = "—",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = viewModel::setSpeed,
                            modifier = Modifier.weight(1f)
                        )
                        CardioField(
                            label = "INCLINE",
                            value = inclineText,
                            unit = "%",
                            placeholder = "—",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = viewModel::setIncline,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    FooterRow(
                        outOfRange = outOfRange,
                        canSave = durationSec != null,
                        onSave = viewModel::save
                    )
                }
                if (recent.isNotEmpty()) {
                    item {
                        Text(
                            text = "RECENT",
                            style = TextStyle(
                                color = VitalColors.textMuted,
                                fontSize = 10.duToSp(),
                                fontWeight = FontWeight.Bold,
                                fontFamily = VitalFonts.JetBrainsMonoBold,
                                letterSpacing = 0.12.em,
                                lineHeight = 10.duToSp(),
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            modifier = Modifier.padding(top = 4.duToDp())
                        )
                    }
                    items(recent, key = { it.id }) { session ->
                        RecentCapsule(
                            session = session,
                            sparkline = sparkline,
                            onClick = {
                                detailSession = session
                                showDetail = true
                            }
                        )
                    }
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
}

// ---- type tiles ----------------------------------------------------------------------------

@Composable
private fun TypeTiles(
    selected: CardioType,
    onSelect: (CardioType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.duToDp()),
        horizontalArrangement = Arrangement.spacedBy(8.duToDp())
    ) {
        TypeTile(
            label = "RUN",
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            selected = selected == CardioType.RUNNING,
            onClick = { onSelect(CardioType.RUNNING) },
            modifier = Modifier.weight(1f)
        )
        TypeTile(
            label = "BIKE",
            icon = Icons.AutoMirrored.Filled.DirectionsBike,
            selected = selected == CardioType.CYCLING,
            onClick = { onSelect(CardioType.CYCLING) },
            modifier = Modifier.weight(1f)
        )
        TypeTile(
            label = "WALK",
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            selected = selected == CardioType.WALKING,
            onClick = { onSelect(CardioType.WALKING) },
            modifier = Modifier.weight(1f)
        )
        TypeTile(
            label = "OTHER",
            icon = Icons.Default.Timer,
            selected = selected == CardioType.OTHER,
            onClick = { onSelect(CardioType.OTHER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TypeTile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        color = if (selected) VitalColors.orange else VitalColors.wellFill,
        shape = RoundedCornerShape(12.duToDp()),
        border = BorderStroke(
            1.5f.duToDp(),
            if (selected) Color.Transparent else VitalColors.wellStroke
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) VitalColors.black else VitalColors.orange,
                modifier = Modifier.size(18.duToDp())
            )
            Spacer(Modifier.height(3.duToDp()))
            Text(
                text = label,
                style = TextStyle(
                    color = if (selected) VitalColors.black else VitalColors.textPrimary,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.1.em,
                    lineHeight = 10.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1
            )
        }
    }
}

// ---- scan row ------------------------------------------------------------------------------

@Composable
private fun ScanRow() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.duToDp()),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(10.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 9.duToDp(), end = 16.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(30.duToDp()),
                color = VitalColors.iconWellFill,
                shape = CircleShape,
                border = BorderStroke(1.duToDp(), VitalColors.iconWellStroke)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Scan treadmill",
                        tint = VitalColors.orange,
                        modifier = Modifier.size(17.duToDp(), 14.duToDp())
                    )
                }
            }
            Spacer(Modifier.width(15.duToDp()))
            Text(
                text = "SCAN TREADMILL",
                style = TextStyle(
                    color = VitalColors.textPrimary,
                    fontSize = 14.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.InterTight,
                    lineHeight = 14.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "ON-DEVICE OCR",
                style = TextStyle(
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Medium,
                    fontFamily = VitalFonts.JetBrainsMonoMedium,
                    lineHeight = 10.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1
            )
        }
    }
}

// ---- fields --------------------------------------------------------------------------------

@Composable
private fun CardioField(
    label: String,
    value: String,
    unit: String,
    placeholder: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val valueStyle = TextStyle(
        color = VitalColors.textPrimary,
        fontSize = 15.duToSp(),
        fontWeight = FontWeight.SemiBold,
        fontFamily = VitalFonts.InterTight,
        lineHeight = 15.duToSp(),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .insetShadow(
                elevation = 2.dp,
                cornerRadius = 10.duToDp(),
                shadowColor = VitalColors.black.copy(alpha = 0.4f)
            ),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(10.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = valueStyle,
            cursorBrush = SolidColor(VitalColors.orange),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.duToDp(), top = 6.duToDp())
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = VitalFonts.JetBrainsMonoBold,
                            letterSpacing = 0.12.em,
                            lineHeight = 10.duToSp(),
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(3.duToDp()))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Box(modifier = Modifier.width(IntrinsicSize.Min)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = valueStyle.copy(color = VitalColors.textMuted)
                                )
                            }
                            innerTextField()
                        }
                        if (unit.isNotBlank()) {
                            Text(
                                text = " $unit",
                                style = TextStyle(
                                    color = VitalColors.textMuted,
                                    fontSize = 10.duToSp(),
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = VitalFonts.JetBrainsMonoMedium,
                                    lineHeight = 10.duToSp(),
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                modifier = Modifier.padding(bottom = 1.duToDp())
                            )
                        }
                    }
                }
            }
        )
    }
}

// ---- footer --------------------------------------------------------------------------------

@Composable
private fun FooterRow(
    outOfRange: Boolean,
    canSave: Boolean,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.duToDp()),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (outOfRange) "OUT OF RANGE · ACSM ≤ 6 KM/H" else "ACSM EST · VALID ≤ 6 KM/H",
            style = TextStyle(
                color = VitalColors.orange,
                fontSize = 11.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                letterSpacing = 0.06.em,
                lineHeight = 11.duToSp(),
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.duToDp()))
        Surface(
            modifier = Modifier
                .width(80.duToDp())
                .fillMaxHeight()
                .clickable(enabled = canSave, onClick = onSave),
            color = if (canSave) VitalColors.orange else VitalColors.orange.copy(alpha = 0.35f),
            shape = RoundedCornerShape(8.duToDp())
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "SAVE",
                    style = TextStyle(
                        color = VitalColors.black.copy(alpha = if (canSave) 1f else 0.5f),
                        fontSize = 12.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        letterSpacing = 0.08.em,
                        lineHeight = 12.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

// ---- recent --------------------------------------------------------------------------------

@Composable
private fun RecentCapsule(
    session: CardioSession,
    sparkline: List<Float>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.duToDp())
            .clickable(onClick = onClick),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(28.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 13.duToDp(), end = 9.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.duToDp()),
                color = VitalColors.iconWellFill,
                shape = CircleShape,
                border = BorderStroke(1.duToDp(), VitalColors.iconWellStroke)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = cardioIcon(session.type),
                        contentDescription = null,
                        tint = VitalColors.orange,
                        modifier = Modifier.size(16.duToDp())
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 15.duToDp())
            ) {
                Text(
                    text = "${session.type.name} · ${formatCardioDate(session.date)}",
                    style = TextStyle(
                        color = VitalColors.textMuted,
                        fontSize = 10.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        letterSpacing = 0.08.em,
                        lineHeight = 10.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.duToDp()))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = session.distanceKm?.let { formatCardioDistance(it) }
                            ?: formatRestTime(session.durationSec),
                        style = TextStyle(
                            color = VitalColors.textPrimary,
                            fontSize = 19.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = VitalFonts.InterTight,
                            lineHeight = 19.duToSp(),
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.duToDp()))
                    Text(
                        text = "${formatCardioKcal(session.kcal)} KCAL",
                        style = TextStyle(
                            color = VitalColors.textMuted,
                            fontSize = 10.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = VitalFonts.JetBrainsMonoBold,
                            lineHeight = 10.duToSp(),
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier.padding(bottom = 2.duToDp())
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .size(96.duToDp(), 44.duToDp())
                    .insetShadow(
                        elevation = 2.dp,
                        cornerRadius = 6.duToDp(),
                        shadowColor = VitalColors.black.copy(alpha = 0.5f)
                    ),
                color = VitalColors.panelGrey,
                shape = RoundedCornerShape(6.duToDp())
            ) {
                Sparkline(
                    modifier = Modifier.fillMaxSize(),
                    dataPoints = sparkline,
                    accentColor = VitalColors.orange
                )
            }
        }
    }
}

private fun cardioIcon(type: CardioType): ImageVector = when (type) {
    CardioType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
    CardioType.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
    CardioType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    else -> Icons.Default.Timer
}

private fun formatCardioDistance(km: Double): String {
    val rounded = Math.round(km * 100.0) / 100.0
    val number = if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
    return "$number km"
}

// ---- detail dialog -------------------------------------------------------------------------

@Composable
private fun CardioDetailDialog(
    session: CardioSession,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = VitalColors.wellFill,
            shape = RoundedCornerShape(18.duToDp()),
            border = BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
        ) {
            Column(modifier = Modifier.padding(18.duToDp())) {
                Text(
                    text = "${session.type.name} · ${formatCardioDate(session.date)}",
                    style = TextStyle(
                        color = VitalColors.textPrimary,
                        fontSize = 13.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = VitalFonts.JetBrainsMonoBold,
                        letterSpacing = 0.12.em,
                        lineHeight = 13.duToSp(),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(Modifier.height(10.duToDp()))
                DetailLine("DURATION", formatRestTime(session.durationSec))
                session.distanceKm?.let { DetailLine("DISTANCE", formatCardioDistance(it)) }
                session.speedKmh?.let { DetailLine("SPEED", "${formatCardioSpeed(it)} KM/H") }
                session.inclinePct?.let { DetailLine("INCLINE", "${formatCardioSpeed(it)} %") }
                DetailLine("KCAL", "${formatCardioKcal(session.kcal)} · ACSM EST")
                Spacer(Modifier.height(14.duToDp()))
                Row(horizontalArrangement = Arrangement.spacedBy(8.duToDp())) {
                    DialogButton(
                        text = "DELETE",
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    )
                    DialogButton(
                        text = "CLOSE",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.duToDp()),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = VitalColors.textMuted,
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                letterSpacing = 0.1.em,
                lineHeight = 10.duToSp(),
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
        Text(
            text = value,
            style = TextStyle(
                color = VitalColors.textPrimary,
                fontSize = 12.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = VitalFonts.JetBrainsMonoBold,
                lineHeight = 12.duToSp(),
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(46.duToDp())
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.duToDp()),
        border = BorderStroke(1.5f.duToDp(), VitalColors.orange)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = TextStyle(
                    color = VitalColors.orange,
                    fontSize = 12.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = VitalFonts.JetBrainsMonoBold,
                    letterSpacing = 0.08.em,
                    lineHeight = 12.duToSp(),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}
