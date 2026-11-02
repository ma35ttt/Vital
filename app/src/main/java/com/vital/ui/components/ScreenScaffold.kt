package com.vital.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.foundation.layout.wrapContentHeight
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp
import com.vital.ui.theme.VitalColors
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ScreenVariant {
    OrangeHero,
    BlackPage
}

@Composable
fun ScreenScaffold(
    variant: ScreenVariant = ScreenVariant.BlackPage,
    title: String,
    heroLabel: String? = null,
    heroValue: String? = null,
    onShowMore: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3).uppercase()
    val dateTag = today.month.name.take(3).uppercase() + ".${today.dayOfMonth}"

    DesignUnitsProvider {
        val jetbrainsMono = FontFamily.Monospace

        // Background
        val backgroundColor = when (variant) {
            ScreenVariant.OrangeHero -> VitalColors.orange
            ScreenVariant.BlackPage -> VitalColors.falseBlackBackground // #141414 when trueBlack off
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor
        ) {
            if (variant == ScreenVariant.OrangeHero) {
                OrangeHeroScaffold(
                    title = title,
                    dayTag = dayTag,
                    dateTag = dateTag,
                    heroLabel = heroLabel,
                    heroValue = heroValue,
                    onShowMore = onShowMore,
                    content = content
                )
            } else {
                BlackPageScaffold(
                    title = title,
                    dayTag = dayTag,
                    dateTag = dateTag,
                    content = content
                )
            }
        }
    }
}

@Composable
private fun OrangeHeroScaffold(
    title: String,
    dayTag: String,
    dateTag: String,
    heroLabel: String?,
    heroValue: String?,
    onShowMore: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    // Header row - fixed at top
    val headerHeight = 82.dp // chips y 50 + height 32 = 82
    val heroLabelTop = 98.dp // cap top y 98
    val heroValueTop = 131.dp // digit top y 131 (61 tall = 192)
    val showMoreTop = 118.dp // SHOW MORE y 118
    val sheetTop = 405.dp // black sheet starts at y 405
    val sheetHighlightTop = 404.dp // 1 du highlight at y 404

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Fixed header with chips and title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            // Left chip - day tag
            VitalChip(
                text = dayTag,
                modifier = Modifier
                    .size(54.dp, 32.dp)
                    .offset(x = 16.dp, y = 50.dp)
            )

            // Right chip - date tag
            VitalChip(
                text = dateTag,
                modifier = Modifier
                    .width(64.dp)
                    .height(32.dp)
                    .offset(x = 313.dp, y = 50.dp),
                minWidth = 54.dp
            )

            // Centered title
            Text(
                text = title.uppercase(),
                color = VitalColors.black,
                fontSize = 15.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.26.em,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = 55.dp) // title cap top ~61, so ~55 top padding
            )
        }

        // Hero area - orange background with label, value, SHOW MORE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHighlightTop) // up to the sheet highlight
        ) {
            // Hero label
            heroLabel?.let { label ->
                Text(
                    text = label,
                    color = VitalColors.black,
                    fontSize = 23.duToSp(), // cap height 17 / 0.73 ≈ 23
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .padding(top = 98.dp) // cap top y 98
                )
            }

            // Hero value
            heroValue?.let { value ->
                Text(
                    text = value,
                    color = VitalColors.black,
                    fontSize = 84.duToSp(), // digit height 61 / 0.73 ≈ 84
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = -0.045.em,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .padding(top = 131.dp) // digit top y 131
                )
            }

            // SHOW MORE button
            onShowMore?.let { action ->
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .size(123.dp, 42.dp)
                        .offset(x = 254.dp, y = 118.dp)
                        .clickable(onClick = action),
                    color = Color.Transparent,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, VitalColors.black)
                ) {
                    Text(
                        text = "SHOW MORE",
                        color = VitalColors.black,
                        fontSize = 12.duToSp(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.22.em,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
            }
        }

        // Black sheet with content - starts at y 405 with 1 du highlight at y 404
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHighlightTop - sheetHighlightTop) // 1 du highlight
                .background(VitalColors.sheetTopHighlight) // #1C1C1C highlight
        )

        // Sheet content area - scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp) // small gap after sheet top
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
private fun BlackPageScaffold(
    title: String,
    dayTag: String,
    dateTag: String,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    val headerHeight = 82.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp) // bottom padding for nav
    ) {
        // Fixed header with chips and title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(VitalColors.black) // black backing for header
        ) {
            // Left chip - day tag (chipDark on black)
            VitalChip(
                text = dayTag,
                modifier = Modifier
                    .size(54.dp, 32.dp)
                    .offset(x = 16.dp, y = 50.dp),
                isOnOrange = false
            )

            // Right chip - date tag (chipDark on black)
            VitalChip(
                text = dateTag,
                modifier = Modifier
                    .width(64.dp)
                    .height(32.dp)
                    .offset(x = 313.dp, y = 50.dp),
                minWidth = 54.dp,
                isOnOrange = false
            )

            // Centered title
            Text(
                text = title.uppercase(),
                color = VitalColors.textPrimary,
                fontSize = 15.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.26.em,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = 55.dp)
            )
        }

        // Body content - left/right 12, from top 98
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(top = 98.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun VitalChip(
    text: String,
    modifier: Modifier = Modifier,
    minWidth: androidx.compose.ui.unit.Dp = 54.dp,
    isOnOrange: Boolean = true
) {
    val backgroundColor = if (isOnOrange) VitalColors.chipOnOrange else VitalColors.chipDark
    val strokeColor = if (isOnOrange) VitalColors.chipOnOrangeStroke else VitalColors.chipDarkStroke
    val textColor = if (isOnOrange) VitalColors.black else VitalColors.textPrimary

    androidx.compose.material3.Surface(
        modifier = modifier
            .width(minWidth)
            .height(32.dp)
            .insetShadow(elevation = 2.dp, cornerRadius = 6.dp, shadowColor = VitalColors.black.copy(alpha = 0.4f)),
        color = backgroundColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.duToSp(), // cap height 9 -> font ~12
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.1.em,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(horizontal = 8.dp)
        )
    }
}

// Hero tag (label + value) for OrangeHero
@Composable
fun HeroTag(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 98.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            color = VitalColors.black,
            fontSize = 23.duToSp(),
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            color = VitalColors.black,
            fontSize = 84.duToSp(),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = -0.045.em
        )
    }
}

// Section label - mono 700 13, tracking .24em
@Composable
fun SectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        color = VitalColors.textPrimary,
        fontSize = 13.duToSp(),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.24.em,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

// Well row - well fill/stroke, for settings rows etc.
@Composable
fun WellRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .insetShadow(elevation = 2.dp, cornerRadius = 10.dp, shadowColor = VitalColors.black.copy(alpha = 0.4f)),
        color = VitalColors.wellFill,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, VitalColors.wellStroke)
    ) {
        content()
    }
}

// Capsule row - 68 tall, full radius, well fill, round icon well 40
@Composable
fun CapsuleRow(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    secondaryText: String? = null,
    sparklineData: List<Float>? = null,
    onClick: (() -> Unit)? = null
) {
    val capsuleHeight = 68.dp
    val iconWellSize = 40.dp

    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(capsuleHeight)
            .clickable(onClick = { onClick?.invoke() }),
        color = VitalColors.wellFill,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(capsuleHeight / 2),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon well
            androidx.compose.material3.Surface(
                modifier = Modifier.size(iconWellSize),
                color = VitalColors.iconWellFill,
                shape = androidx.compose.foundation.shape.CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, VitalColors.iconWellStroke)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            // Label and value
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label.uppercase(),
                    color = VitalColors.textMuted,
                    fontSize = 10.duToSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = value,
                    color = VitalColors.textPrimary,
                    fontSize = 19.duToSp(), // digit height 14 -> font ~19
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                secondaryText?.let {
                    Text(
                        text = it,
                        color = VitalColors.textMuted,
                        fontSize = 10.duToSp(),
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Grey sparkline panel - 96 x 44
            sparklineData?.let { data ->
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .size(96.dp, 44.dp)
                        .insetShadow(elevation = 2.dp, cornerRadius = 6.dp, shadowColor = VitalColors.black.copy(alpha = 0.5f)),
                    color = VitalColors.panelGrey,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                ) {
                    Sparkline(
                        modifier = Modifier.fillMaxSize(),
                        dataPoints = data,
                        accentColor = VitalColors.orange
                    )
                }
            }
        }
    }
}

// Vital toggle - 46 x 26, custom drawn
@Composable
fun VitalToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackWidth = 46.dp
    val trackHeight = 26.dp
    val knobSize = 20.dp

    androidx.compose.material3.Surface(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clickable(enabled = enabled, onClick = { onCheckedChange(!checked) })
            .insetShadow(elevation = 1.dp, cornerRadius = trackHeight / 2, shadowColor = VitalColors.black.copy(alpha = 0.3f)),
        color = if (checked) VitalColors.toggleOnFill else VitalColors.toggleOffFill,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(trackHeight / 2),
        border = if (checked) androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
        else androidx.compose.foundation.BorderStroke(1.5.dp, VitalColors.toggleOffStroke)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .size(knobSize)
                    .padding(horizontal = 3.dp)
                    .padding(vertical = 3.dp),
                color = if (checked) VitalColors.toggleOnKnob else VitalColors.toggleOffKnob,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {}
        }
    }
}

// Grey panel with inset shadow
@Composable
fun GreyPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .insetShadow(elevation = 7.dp, cornerRadius = 6.dp, shadowColor = VitalColors.panelGreyShadow),
        color = VitalColors.panelGrey,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
    ) {
        content()
    }
}

// Simple sparkline for capsule rows
@Composable
fun Sparkline(
    modifier: Modifier = Modifier,
    dataPoints: List<Float>,
    accentColor: Color = VitalColors.orange
) {
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        if (dataPoints.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 4.dp.toPx()
        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding

        val minValue = dataPoints.minOrNull() ?: 0f
        val maxValue = dataPoints.maxOrNull() ?: 1f
        val range = if (maxValue > minValue) maxValue - minValue else 1f

        val path = androidx.compose.ui.graphics.Path()
        dataPoints.forEachIndexed { index, value ->
            val x = padding + (index / (dataPoints.size - 1).toFloat()) * graphWidth
            val normalizedValue = (value - minValue) / range
            val y = padding + graphHeight - normalizedValue * graphHeight
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // End dot
        val lastIndex = dataPoints.lastIndex
        val lastX = padding + (lastIndex / (dataPoints.size - 1).toFloat()) * graphWidth
        val lastNormalized = (dataPoints[lastIndex] - minValue) / range
        val lastY = padding + graphHeight - lastNormalized * graphHeight
        drawCircle(
            color = accentColor,
            center = androidx.compose.ui.geometry.Offset(lastX, lastY),
            radius = 3.dp.toPx()
        )
    }
}

@Preview
@Composable
fun ScreenScaffoldPreview() {
    com.vital.ui.theme.VitalTheme(true) {
        ScreenScaffold(
            variant = ScreenVariant.OrangeHero,
            title = "MEALS",
            heroLabel = "KCAL LEFT",
            heroValue = "1,240",
            onShowMore = { /* TODO */ }
        ) {
            Text(text = "Content area", color = VitalColors.textPrimary)
        }
    }
}

@Preview
@Composable
fun ScreenScaffoldBlackPagePreview() {
    com.vital.ui.theme.VitalTheme(true) {
        ScreenScaffold(
            variant = ScreenVariant.BlackPage,
            title = "WEEKS"
        ) {
            Text(text = "Content area", color = VitalColors.textPrimary)
        }
    }
}