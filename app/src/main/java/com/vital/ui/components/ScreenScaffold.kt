package com.vital.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.vital.ui.theme.VitalFonts
import com.vital.ui.theme.isTrueBlackEnabled
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.vital.ui.components.FabGlyph

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
    // OrangeDial parameters (only used for OrangeHero variant)
    dialLetters: List<String> = listOf("L", "M", "H"),
    dialHighlightedIndex: Int = 1,
    dialInnerLabel: String = "KCAL LEFT",
    dialInnerValue: String = "1,240",
    dialInnerUnit: String = "KCAL",
    dialPillText: String = "TODAY",
    dialTopPill: String = "MOVE",
    dialFabGlyph: FabGlyph = FabGlyph.Plus,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3).uppercase()
    val dateTag = today.month.name.take(3).uppercase() + ".${today.dayOfMonth}"

    DesignUnitsProvider {
        val jetbrainsMono = FontFamily.Monospace

        // Background
        val trueBlack = isTrueBlackEnabled()
        val backgroundColor = when (variant) {
            ScreenVariant.OrangeHero -> VitalColors.orange
            ScreenVariant.BlackPage -> if (trueBlack) VitalColors.trueBlackBackground else VitalColors.falseBlackBackground
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
                    dialLetters = dialLetters,
                    dialHighlightedIndex = dialHighlightedIndex,
                    dialInnerLabel = dialInnerLabel,
                    dialInnerValue = dialInnerValue,
                    dialInnerUnit = dialInnerUnit,
                    dialPillText = dialPillText,
                    dialTopPill = dialTopPill,
                    dialFabGlyph = dialFabGlyph,
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
    dialLetters: List<String>,
    dialHighlightedIndex: Int,
    dialInnerLabel: String,
    dialInnerValue: String,
    dialInnerUnit: String,
    dialPillText: String,
    dialTopPill: String,
    dialFabGlyph: FabGlyph,
    content: @Composable () -> Unit
) {
    // Orange hero area with OrangeDial: starts at screen top y=0, height 404 (up to sheet highlight)
    val sheetHighlightTop = 404.duToDp() // 1 du highlight at y 404

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content column: hero area -> highlight -> sheet
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Orange hero area with OrangeDial (starts at y=0, height 404)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHighlightTop)
            ) {
                // OrangeDial fills the orange area. The FAB is drawn separately, above the
                // sheet, so its rim stays visible where it crosses y 404.
                OrangeDial(
                    modifier = Modifier.fillMaxSize(),
                    letters = dialLetters,
                    highlightedIndex = dialHighlightedIndex,
                    innerLabel = dialInnerLabel,
                    innerValue = dialInnerValue,
                    innerUnit = dialInnerUnit,
                    pillText = dialPillText,
                    topPill = dialTopPill,
                    fabGlyph = dialFabGlyph,
                    showFab = false
                )

                // Hero label + value overlay (top-left: label cap top y=100, value digit top y=133, x=16/17/18)
                heroLabel?.let { label ->
                    heroValue?.let { value ->
                        HeroTag(
                            label = label,
                            value = value,
                            modifier = Modifier
                                .offset(x = 16.duToDp(), y = 96.duToDp())
                        )
                    }
                }

                // SHOW MORE button overlaid on top right of dial area (y=118 from screen top)
                onShowMore?.let { action ->
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .size(123.duToDp(), 42.duToDp())
                            .offset(x = 254.duToDp(), y = 118.duToDp())
                            .clickable(onClick = action),
                        color = Color.Transparent,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.duToDp()),
                        border = androidx.compose.foundation.BorderStroke(3.duToDp(), VitalColors.black)
                    ) {
                        Text(
                            text = "SHOW MORE",
                            color = VitalColors.black,
                            fontSize = 12.duToSp(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.22.em,
                            lineHeight = 12.duToSp(),
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // 1 du highlight line at y 404 (top of sheet)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.duToDp())
                    .background(VitalColors.sheetTopHighlight)
            )

            // Black sheet with rounded top corners (radius 26) starting at y 405
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = VitalColors.black,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 26.duToDp(), topEnd = 26.duToDp())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.duToDp()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sheet content - left/right 16
                    content()
                }
            }
        }

        // FAB above the sheet: 60 x 60 du box centred on (50, 386), rim visible over black.
        OrangeDialFab(
            modifier = Modifier
                .offset(x = 20.duToDp(), y = 356.duToDp())
                .size(60.duToDp()),
            fabGlyph = dialFabGlyph
        )

        // Fixed header overlay at top (chips y=50, title cap top ~61)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.duToDp())
        ) {
            // Left chip - day tag
            VitalChip(
                text = dayTag,
                modifier = Modifier
                    .size(54.duToDp(), 32.duToDp())
                    .offset(x = 16.duToDp(), y = 50.duToDp()),
                minWidth = 54.duToDp()
            )

            // Right chip - date tag
            VitalChip(
                text = dateTag,
                modifier = Modifier
                    .width(64.duToDp())
                    .height(32.duToDp())
                    .offset(x = 313.duToDp(), y = 50.duToDp()),
                minWidth = 54.duToDp()
            )

            // Centered title
            Text(
                text = title.uppercase(),
                color = VitalColors.black,
                fontSize = 15.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.26.em,
                lineHeight = 15.duToSp(),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = 55.duToDp())
            )
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
    val trueBlack = isTrueBlackEnabled()
    val pageBg = if (trueBlack) VitalColors.trueBlackBackground else VitalColors.falseBlackBackground

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Body column: content starts at y=98 from screen top (not below header)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.duToDp()) // bottom padding for nav
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.duToDp())
                    .padding(top = 98.duToDp()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }

        // Fixed header overlay at top (chips y=50, title cap top ~61)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.duToDp())
                .background(pageBg)
        ) {
            // Left chip - day tag (chipDark on black)
            VitalChip(
                text = dayTag,
                modifier = Modifier
                    .size(54.duToDp(), 32.duToDp())
                    .offset(x = 16.duToDp(), y = 50.duToDp()),
                isOnOrange = false,
                minWidth = 54.duToDp()
            )

            // Right chip - date tag (chipDark on black)
            VitalChip(
                text = dateTag,
                modifier = Modifier
                    .width(64.duToDp())
                    .height(32.duToDp())
                    .offset(x = 313.duToDp(), y = 50.duToDp()),
                minWidth = 54.duToDp(),
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
                lineHeight = 15.duToSp(),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = 55.duToDp())
            )
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
            .widthIn(min = minWidth)
            .heightIn(min = 32.duToDp())
            .insetShadow(elevation = 2.duToDp(), cornerRadius = 6.duToDp(), shadowColor = VitalColors.black.copy(alpha = 0.4f)),
        color = backgroundColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.duToDp()),
        border = androidx.compose.foundation.BorderStroke(1.duToDp(), strokeColor)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.duToSp(),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.1.em,
            textAlign = TextAlign.Center,
            lineHeight = 12.duToSp(),
            modifier = Modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(horizontal = 8.duToDp())
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
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            color = VitalColors.black,
            fontSize = 21.5f.duToSp(),
            fontWeight = FontWeight.SemiBold,
            fontFamily = VitalFonts.InterTight,
            lineHeight = 21.5f.duToSp()
        )
        Text(
            text = value,
            color = VitalColors.black,
            fontSize = 84.duToSp(),
            fontWeight = FontWeight.Bold,
            fontFamily = VitalFonts.InterTight,
            letterSpacing = -0.045.em,
            lineHeight = 84.duToSp(),
            modifier = Modifier.offset(y = (-8).duToDp())
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
        lineHeight = 13.duToSp(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.duToDp())
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
                    letterSpacing = 1.sp,
                    lineHeight = 10.duToSp()
                )
                Text(
                    text = value,
                    color = VitalColors.textPrimary,
                    fontSize = 19.duToSp(), // digit height 14 -> font ~19
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 19.duToSp()
                )
                secondaryText?.let {
                    Text(
                        text = it,
                        color = VitalColors.textMuted,
                        fontSize = 10.duToSp(),
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 10.duToSp()
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
    val trackWidth = 46.duToDp()
    val trackHeight = 26.duToDp()
    val knobSize = 20.duToDp()

    androidx.compose.material3.Surface(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clickable(enabled = enabled, onClick = { onCheckedChange(!checked) })
            .insetShadow(elevation = 1.duToDp(), cornerRadius = trackHeight / 2, shadowColor = VitalColors.black.copy(alpha = 0.3f)),
        color = if (checked) VitalColors.toggleOnFill else VitalColors.toggleOffFill,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(trackHeight / 2),
        border = if (checked) androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
        else androidx.compose.foundation.BorderStroke(1.5f.duToDp(), VitalColors.toggleOffStroke)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .size(knobSize)
                    .padding(horizontal = 3.duToDp())
                    .padding(vertical = 3.duToDp()),
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
    com.vital.ui.theme.VitalTheme(themeId = "dark") {
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
    com.vital.ui.theme.VitalTheme(themeId = "dark") {
        ScreenScaffold(
            variant = ScreenVariant.BlackPage,
            title = "WEEKS"
        ) {
            Text(text = "Content area", color = VitalColors.textPrimary)
        }
    }
}