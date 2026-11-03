package com.vital.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.vital.R
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.designUnitsScale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Geometry measured from chrome.md (OrangeDial section) and REMAINING_TABS_BRIEF.md section 3.
private const val DIAL_CX = 300f
private const val DIAL_CY = 316f
private const val DIAL_R = 152f
private const val TICK_R = 135f
private const val THIN_R = 100f
private const val INNER_R = 64f
private const val INNER_RING_R = 56f

private const val KNOB_CX = 66f
private const val KNOB_CY = 268f
private const val KNOB_R = 37f

private const val FAB_CX = 50f
private const val FAB_CY = 386f
private const val FAB_R = 28f

// Three decorative letter circles on the dial face (brief section 3).
private val MAIN_LETTER_POSITIONS = listOf(
    Offset(183.5f, 254f),
    Offset(168.1f, 311.4f),
    Offset(179.4f, 369.7f)
)

// L / M / H art under the knob. M is always the active one on these tabs.
// chrome.md: the M circle was measured at (88.5, 353.5); use that over the brief's 357.9.
private val UNDER_KNOB_POSITIONS = listOf(
    Offset(67.1f, 343f),
    Offset(88.5f, 353.5f),
    Offset(99.2f, 381.3f)
)

private val MonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_400, FontWeight.Normal),
    Font(R.font.jetbrains_mono_500, FontWeight.Medium),
    Font(R.font.jetbrains_mono_700, FontWeight.Bold)
)

private val InterTightFamily = FontFamily(
    Font(R.font.inter_tight_400, FontWeight.Normal),
    Font(R.font.inter_tight_500, FontWeight.Medium),
    Font(R.font.inter_tight_600, FontWeight.SemiBold),
    Font(R.font.inter_tight_700, FontWeight.Bold)
)

/**
 * Decorative dial used on Meals, Lift and Cardio (chrome.md / brief section 3).
 *
 * The static art (discs, rings, ticks, fan, knob, FAB, pill and letter-circle backgrounds)
 * is rendered once into an [ImageBitmap] and reused every frame. Only text is drawn per frame.
 * The letters, knob and FAB are decoration: no gestures, no focus, no 44 du target.
 */
@Composable
fun OrangeDial(
    modifier: Modifier = Modifier,
    letters: List<String> = listOf("L", "M", "H"),
    highlightedIndex: Int = 1,
    innerLabel: String = "KCAL LEFT",
    innerValue: String = "1,240",
    innerUnit: String = "KCAL",
    pillText: String = "TODAY",
    topPill: String = "MOVE",
    fabGlyph: FabGlyph = FabGlyph.Plus,
    showFab: Boolean = true
) {
    DesignUnitsProvider {
        val scale = designUnitsScale()
        val localDensity = LocalDensity.current
        val density = localDensity.density
        val layoutDirection = LocalLayoutDirection.current
        val textMeasurer = rememberTextMeasurer()

        val highlight = highlightedIndex.coerceIn(0, 2)
        val visibleLetters = letters.take(3)

        fun duToSp(du: Float) = with(localDensity) { (du * scale).dp.toSp() }

        val mainLetterLayouts = visibleLetters.mapIndexed { index, letter ->
            textMeasurer.measure(
                text = letter,
                style = TextStyle(
                    color = if (index == highlight) VitalColors.black else VitalColors.orange,
                    fontSize = duToSp(11f),
                    fontWeight = FontWeight.Bold,
                    fontFamily = MonoFamily
                )
            )
        }

        val underKnobLayouts = listOf("L", "M", "H").mapIndexed { index, letter ->
            textMeasurer.measure(
                text = letter,
                style = TextStyle(
                    color = if (index == 1) VitalColors.orange else VitalColors.black,
                    fontSize = duToSp(10f),
                    fontWeight = FontWeight.Bold,
                    fontFamily = MonoFamily
                )
            )
        }

        val innerLabelLayout = textMeasurer.measure(
            text = innerLabel,
            style = TextStyle(
                color = VitalColors.textMuted,
                fontSize = duToSp(9f),
                fontWeight = FontWeight.Medium,
                fontFamily = MonoFamily,
                letterSpacing = duToSp(1.6f)
            )
        )

        val innerValueLayout = textMeasurer.measure(
            text = innerValue,
            style = TextStyle(
                color = VitalColors.textPrimary,
                fontSize = duToSp(34f),
                fontWeight = FontWeight.Bold,
                fontFamily = InterTightFamily,
                letterSpacing = (-0.03).em
            )
        )

        val unitText = innerUnit.trim()
        val innerUnitLayout = if (unitText.isEmpty()) {
            null
        } else {
            textMeasurer.measure(
                text = unitText,
                style = TextStyle(
                    color = VitalColors.textMuted,
                    fontSize = duToSp(15f),
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterTightFamily
                )
            )
        }

        val pillLayout = textMeasurer.measure(
            text = pillText,
            style = TextStyle(
                color = VitalColors.black,
                fontSize = duToSp(9.5f),
                fontWeight = FontWeight.Bold,
                fontFamily = MonoFamily,
                letterSpacing = 0.06.em
            )
        )

        val topPillLayout = textMeasurer.measure(
            text = topPill,
            style = TextStyle(
                color = VitalColors.textPrimary,
                fontSize = duToSp(11f),
                fontWeight = FontWeight.Medium,
                fontFamily = MonoFamily
            )
        )

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .drawWithCache {
                    val art = buildDialArt(
                        widthPx = size.width.toInt().coerceAtLeast(1),
                        heightPx = size.height.toInt().coerceAtLeast(1),
                        density = density,
                        layoutDirection = layoutDirection,
                        scale = scale,
                        highlight = highlight,
                        letterCount = visibleLetters.size,
                        fabGlyph = fabGlyph,
                        showFab = showFab
                    )
                    onDrawBehind {
                        drawImage(art)
                        drawDialText(
                            scale = scale,
                            density = density,
                            mainLetterLayouts = mainLetterLayouts,
                            underKnobLayouts = underKnobLayouts,
                            innerLabelLayout = innerLabelLayout,
                            innerValueLayout = innerValueLayout,
                            innerUnitLayout = innerUnitLayout,
                            pillLayout = pillLayout,
                            topPillLayout = topPillLayout
                        )
                    }
                }
        ) {}
    }
}

/**
 * The FAB alone, drawn above the sheet so its rim stays visible where it crosses y 404.
 * The caller positions a 60 x 60 du box; the FAB centre sits at its centre (50, 386 du on screen).
 */
@Composable
fun OrangeDialFab(
    modifier: Modifier = Modifier,
    fabGlyph: FabGlyph = FabGlyph.Plus
) {
    DesignUnitsProvider {
        val scale = designUnitsScale()
        val localDensity = LocalDensity.current
        val density = localDensity.density
        val layoutDirection = LocalLayoutDirection.current

        Canvas(
            modifier = modifier.drawWithCache {
                val art = buildFabArt(
                    widthPx = size.width.toInt().coerceAtLeast(1),
                    heightPx = size.height.toInt().coerceAtLeast(1),
                    density = density,
                    layoutDirection = layoutDirection,
                    scale = scale,
                    fabGlyph = fabGlyph
                )
                onDrawBehind { drawImage(art) }
            }
        ) {}
    }
}

enum class FabGlyph { Plus, Camera }

private fun buildDialArt(
    widthPx: Int,
    heightPx: Int,
    density: Float,
    layoutDirection: LayoutDirection,
    scale: Float,
    highlight: Int,
    letterCount: Int,
    fabGlyph: FabGlyph,
    showFab: Boolean
): ImageBitmap {
    val bitmap = ImageBitmap(widthPx, heightPx)
    CanvasDrawScope().draw(
        density = Density(density),
        layoutDirection = layoutDirection,
        canvas = androidx.compose.ui.graphics.Canvas(bitmap),
        size = Size(widthPx.toFloat(), heightPx.toFloat())
    ) {
        drawStaticDial(scale, density, highlight, letterCount, fabGlyph, showFab)
    }
    return bitmap
}

private fun buildFabArt(
    widthPx: Int,
    heightPx: Int,
    density: Float,
    layoutDirection: LayoutDirection,
    scale: Float,
    fabGlyph: FabGlyph
): ImageBitmap {
    val bitmap = ImageBitmap(widthPx, heightPx)
    CanvasDrawScope().draw(
        density = Density(density),
        layoutDirection = layoutDirection,
        canvas = androidx.compose.ui.graphics.Canvas(bitmap),
        size = Size(widthPx.toFloat(), heightPx.toFloat())
    ) {
        drawFabAt(
            center = Offset(widthPx / 2f, heightPx / 2f),
            scale = scale,
            density = density,
            fabGlyph = fabGlyph
        )
    }
    return bitmap
}

private fun DrawScope.drawStaticDial(
    scale: Float,
    density: Float,
    highlight: Int,
    letterCount: Int,
    fabGlyph: FabGlyph,
    showFab: Boolean
) {
    fun px(value: Float): Float = value * scale * density
    val center = Offset(px(DIAL_CX), px(DIAL_CY))

    // Outer disc: black fill, deliberately clipped by the right screen edge.
    drawCircle(color = VitalColors.black, radius = px(DIAL_R), center = center)
    drawCircle(
        color = VitalColors.dialOuterStroke,
        radius = px(DIAL_R),
        center = center,
        style = Stroke(px(1.5f))
    )

    // Tick ring: radius 135, 14 du stroke, dashed 1.3 / 5.71.
    drawCircle(
        color = VitalColors.tickGrey,
        radius = px(TICK_R),
        center = center,
        style = Stroke(
            width = px(14f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(px(1.3f), px(5.71f)))
        )
    )

    // Thin ring: radius 100, 1.5 du.
    drawCircle(
        color = VitalColors.dialThinRing,
        radius = px(THIN_R),
        center = center,
        style = Stroke(px(1.5f))
    )

    // Accent fan: 13 lines, angles 140..212 step 6, start radius 78, end radius 83..128.
    for (index in 0..12) {
        val angle = (140f + index * 6f) * PI.toFloat() / 180f
        val startRadius = px(78f)
        val endRadius = px(83f + index * (45f / 12f))
        val strokeWidth = px(1.2f + index * (2.4f / 12f))
        val alpha = 0.35f + index * (0.65f / 12f)
        drawLine(
            color = VitalColors.orange.copy(alpha = alpha),
            start = Offset(
                center.x + startRadius * cos(angle),
                center.y + startRadius * sin(angle)
            ),
            end = Offset(
                center.x + endRadius * cos(angle),
                center.y + endRadius * sin(angle)
            ),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Letter-circle backgrounds (radius 10). Highlighted = orange, the rest black.
    MAIN_LETTER_POSITIONS.take(letterCount).forEachIndexed { index, position ->
        drawCircle(
            color = if (index == highlight) VitalColors.orange else VitalColors.black,
            radius = px(10f),
            center = Offset(px(position.x), px(position.y))
        )
    }

    // Inner disc: radius 64 fill + 3 du stroke, then the radius 56 dashed ring.
    drawCircle(color = VitalColors.dialInnerFill, radius = px(INNER_R), center = center)
    drawCircle(
        color = VitalColors.dialInnerStroke,
        radius = px(INNER_R),
        center = center,
        style = Stroke(px(3f))
    )
    drawCircle(
        color = VitalColors.dialInnerDashedRing,
        radius = px(INNER_RING_R),
        center = center,
        style = Stroke(
            width = px(6f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(px(0.9f), px(4.6f)))
        )
    )

    // Top pill: 56 x 24 at (272, 178), radius 8.
    drawRoundRect(
        color = VitalColors.panelGrey,
        topLeft = Offset(px(272f), px(178f)),
        size = Size(px(56f), px(24f)),
        cornerRadius = CornerRadius(px(8f))
    )

    // Inner pill: 78 x 22 at (261, 338), radius 11.
    drawRoundRect(
        color = VitalColors.orange,
        topLeft = Offset(px(DIAL_CX - 39f), px(338f)),
        size = Size(px(78f), px(22f)),
        cornerRadius = CornerRadius(px(11f))
    )

    // Active M under the knob: black circle, orange text is drawn later.
    val active = UNDER_KNOB_POSITIONS[1]
    drawCircle(
        color = VitalColors.black,
        radius = px(10f),
        center = Offset(px(active.x), px(active.y))
    )

    drawKnob(scale, density)

    if (showFab) {
        drawFabAt(Offset(px(FAB_CX), px(FAB_CY)), scale, density, fabGlyph)
    }
}

private fun DrawScope.drawKnob(scale: Float, density: Float) {
    fun px(value: Float): Float = value * scale * density
    val center = Offset(px(KNOB_CX), px(KNOB_CY))

    // Orange body (same colour as the hero, so only the scalloped edge reads).
    drawCircle(color = VitalColors.orange, radius = px(KNOB_R), center = center)
    drawCircle(
        color = VitalColors.black,
        radius = px(KNOB_R),
        center = center,
        style = Stroke(
            width = px(3.2f),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(px(2.6f), px(2f)))
        )
    )
    drawCircle(
        color = VitalColors.black.copy(alpha = 0.28f),
        radius = px(31f),
        center = center,
        style = Stroke(px(1.2f))
    )
    drawCircle(
        color = VitalColors.black,
        radius = px(7f),
        center = Offset(px(60f), px(264f))
    )
    drawLine(
        color = VitalColors.black,
        start = Offset(px(80f), px(279f)),
        end = Offset(px(89f), px(287f)),
        strokeWidth = px(5f),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawFabAt(
    center: Offset,
    scale: Float,
    density: Float,
    fabGlyph: FabGlyph
) {
    fun px(value: Float): Float = value * scale * density

    drawCircle(color = VitalColors.black, radius = px(FAB_R), center = center)
    // Visible rim: the FAB overlaps the black sheet, so the stroke must be lighter than black.
    // chrome.md FIX: 2 du #2A2A2A rim, because the brief's 1.5 du #1F1F1F is invisible on the sheet.
    drawCircle(
        color = VitalColors.fabRimVisible,
        radius = px(FAB_R),
        center = center,
        style = Stroke(px(2f))
    )

    when (fabGlyph) {
        FabGlyph.Plus -> {
            drawLine(
                color = VitalColors.orange,
                start = Offset(center.x - px(12f), center.y),
                end = Offset(center.x + px(12f), center.y),
                strokeWidth = px(3.2f),
                cap = StrokeCap.Round
            )
            drawLine(
                color = VitalColors.orange,
                start = Offset(center.x, center.y - px(12f)),
                end = Offset(center.x, center.y + px(12f)),
                strokeWidth = px(3.2f),
                cap = StrokeCap.Round
            )
        }

        FabGlyph.Camera -> drawCameraGlyph(center, scale, density)
    }
}

private fun DrawScope.drawCameraGlyph(center: Offset, scale: Float, density: Float) {
    fun px(value: Float): Float = value * scale * density

    drawRoundRect(
        color = VitalColors.orange,
        topLeft = Offset(center.x - px(13.5f), center.y - px(5f)),
        size = Size(px(27f), px(19f)),
        cornerRadius = CornerRadius(px(3f)),
        style = Stroke(px(2.4f), join = StrokeJoin.Round)
    )

    val bump = Path().apply {
        moveTo(center.x - px(6f), center.y - px(5f))
        lineTo(center.x - px(4f), center.y - px(9f))
        lineTo(center.x + px(4f), center.y - px(9f))
        lineTo(center.x + px(6f), center.y - px(5f))
    }
    drawPath(
        path = bump,
        color = VitalColors.orange,
        style = Stroke(px(2.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    drawCircle(
        color = VitalColors.orange,
        radius = px(5f),
        center = Offset(center.x, center.y + px(4.5f)),
        style = Stroke(px(2.4f))
    )
}

private fun DrawScope.drawDialText(
    scale: Float,
    density: Float,
    mainLetterLayouts: List<TextLayoutResult>,
    underKnobLayouts: List<TextLayoutResult>,
    innerLabelLayout: TextLayoutResult,
    innerValueLayout: TextLayoutResult,
    innerUnitLayout: TextLayoutResult?,
    pillLayout: TextLayoutResult,
    topPillLayout: TextLayoutResult
) {
    fun px(value: Float): Float = value * scale * density

    mainLetterLayouts.forEachIndexed { index, layout ->
        if (index >= MAIN_LETTER_POSITIONS.size) return@forEachIndexed
        val position = MAIN_LETTER_POSITIONS[index]
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                px(position.x) - layout.size.width / 2f,
                px(position.y) - layout.size.height / 2f
            )
        )
    }

    UNDER_KNOB_POSITIONS.forEachIndexed { index, position ->
        val layout = underKnobLayouts.getOrNull(index) ?: return@forEachIndexed
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                px(position.x) - layout.size.width / 2f,
                px(position.y) - layout.size.height / 2f
            )
        )
    }

    // Inner label, baseline 293.
    drawText(
        textLayoutResult = innerLabelLayout,
        topLeft = Offset(
            px(DIAL_CX) - innerLabelLayout.size.width / 2f,
            px(293f) - innerLabelLayout.firstBaseline
        )
    )

    // Inner value + optional unit, sharing baseline 329 and centred as one block.
    val valueWidth = innerValueLayout.size.width
    val unitWidth = innerUnitLayout?.size?.width?.toFloat() ?: 0f
    val gap = if (innerUnitLayout != null) px(2f) else 0f
    val blockWidth = valueWidth + gap + unitWidth
    val blockLeft = px(DIAL_CX) - blockWidth / 2f

    drawText(
        textLayoutResult = innerValueLayout,
        topLeft = Offset(blockLeft, px(329f) - innerValueLayout.firstBaseline)
    )
    if (innerUnitLayout != null) {
        drawText(
            textLayoutResult = innerUnitLayout,
            topLeft = Offset(
                blockLeft + valueWidth + gap,
                px(329f) - innerUnitLayout.firstBaseline
            )
        )
    }

    // Inner pill text, centred in the 78 x 22 pill at (261, 338).
    val pillLeft = px(DIAL_CX - 39f)
    val pillTop = px(338f)
    drawText(
        textLayoutResult = pillLayout,
        topLeft = Offset(
            pillLeft + (px(78f) - pillLayout.size.width) / 2f,
            pillTop + (px(22f) - pillLayout.size.height) / 2f
        )
    )

    // Top pill text, centred in the 56 x 24 pill at (272, 178).
    val topPillLeft = px(272f)
    val topPillTop = px(178f)
    drawText(
        textLayoutResult = topPillLayout,
        topLeft = Offset(
            topPillLeft + (px(56f) - topPillLayout.size.width) / 2f,
            topPillTop + (px(24f) - topPillLayout.size.height) / 2f
        )
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 430)
@Composable
fun OrangeDialPreview_Meals() {
    com.vital.ui.theme.VitalTheme(true) {
        Box(modifier = Modifier.fillMaxSize().background(VitalColors.orange)) {
            OrangeDial(
                modifier = Modifier.fillMaxSize(),
                letters = listOf("P", "C", "F"),
                highlightedIndex = 0,
                innerLabel = "EATEN",
                innerValue = "1,260",
                innerUnit = "",
                pillText = "OF 2,500",
                topPill = "LUNCH",
                fabGlyph = FabGlyph.Plus
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 430)
@Composable
fun OrangeDialPreview_Lift() {
    com.vital.ui.theme.VitalTheme(true) {
        Box(modifier = Modifier.fillMaxSize().background(VitalColors.orange)) {
            OrangeDial(
                modifier = Modifier.fillMaxSize(),
                letters = listOf("2", "3", "4"),
                highlightedIndex = 1,
                innerLabel = "BENCH PRESS",
                innerValue = "80",
                innerUnit = "kg",
                pillText = "SET 3 · ×8",
                topPill = "OF 3:00",
                fabGlyph = FabGlyph.Plus
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 430)
@Composable
fun OrangeDialPreview_Cardio() {
    com.vital.ui.theme.VitalTheme(true) {
        Box(modifier = Modifier.fillMaxSize().background(VitalColors.orange)) {
            OrangeDial(
                modifier = Modifier.fillMaxSize(),
                letters = listOf("R", "B", "W"),
                highlightedIndex = 0,
                innerLabel = "SPEED",
                innerValue = "6.0",
                innerUnit = "km/h",
                pillText = "INCL 2.0%",
                topPill = "32:10",
                fabGlyph = FabGlyph.Camera
            )
        }
    }
}
