package com.vital.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp
import com.vital.ui.theme.VitalColors


@Composable
private fun duPx(du: Float): Float {
    val dp = du.duToDp()
    return with(LocalDensity.current) { dp.toPx() }
}

/**
 * Glass nav (FIXES_AND_GESTURES_BRIEF section 2 / chrome.md):
 * container 12..381 (369 wide), height 60, bottom edge 14 above the screen bottom, radius 30.
 * Slot centres TDY 43, MLS 93, LFT 144, + 196.5, CRD 249, WKS 299, SET 349.
 */
@Composable
fun GlassBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onPlusClick: () -> Unit
) {
    // The nav sits in the Scaffold bottomBar, outside any screen-level DesignUnitsProvider,
    // so it provides its own or every du value would silently render as dp.
    DesignUnitsProvider {
        GlassNavBar(selectedTab = selectedTab, onTabSelected = onTabSelected, onPlusClick = onPlusClick)
    }
}

@Composable
private fun GlassNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onPlusClick: () -> Unit
) {
    val tabLabels = listOf("TDY", "MLS", "LFT", "CRD", "WKS", "SET")

    val radiusPx = duPx(30f)
    val borderPx = duPx(1f)
    val shadowBlurPx = duPx(24f)
    val shadowDyPx = duPx(10f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.duToDp())
            .padding(bottom = 14.duToDp())
            .height(60.duToDp())
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Drop shadow: black 65 %, blur 24, y 10.
            val shadowPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.BLACK
                alpha = (0.65f * 255).toInt()
                maskFilter = BlurMaskFilter(shadowBlurPx, BlurMaskFilter.Blur.NORMAL)
            }
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    shadowDyPx,
                    size.width,
                    size.height + shadowDyPx,
                    radiusPx,
                    radiusPx,
                    shadowPaint
                )
            }

            // Glass fill: vertical gradient white 16 % to white 5 %.
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.05f)),
                    startY = 0f,
                    endY = size.height
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )

            // Inner top edge white 38 % (1 du).
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = Offset(radiusPx * 0.5f, borderPx),
                end = Offset(size.width - radiusPx * 0.5f, borderPx),
                strokeWidth = borderPx,
                cap = StrokeCap.Round
            )
            // Inner bottom edge white 7 %.
            drawLine(
                color = Color.White.copy(alpha = 0.07f),
                start = Offset(radiusPx * 0.5f, size.height - borderPx),
                end = Offset(size.width - radiusPx * 0.5f, size.height - borderPx),
                strokeWidth = borderPx,
                cap = StrokeCap.Round
            )

            // Border 1 du white 16 %.
            drawRoundRect(
                color = Color.White.copy(alpha = 0.16f),
                topLeft = Offset(borderPx / 2f, borderPx / 2f),
                size = androidx.compose.ui.geometry.Size(size.width - borderPx, size.height - borderPx),
                cornerRadius = CornerRadius(radiusPx - borderPx / 2f, radiusPx - borderPx / 2f),
                style = Stroke(width = borderPx)
            )
        }

        // Three equal-weight tab slots, a fixed 54 du centre slot for +, three more.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..2) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    GlassTabItem(
                        label = tabLabels[i],
                        isSelected = selectedTab == i,
                        onClick = { onTabSelected(i) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(54.duToDp())
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                GlassPlusButton(onClick = onPlusClick)
            }

            for (i in 3..5) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    GlassTabItem(
                        label = tabLabels[i],
                        isSelected = selectedTab == i,
                        onClick = { onTabSelected(i) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val pillRadiusPx = duPx(23f)
    val glowBlurPx = duPx(14f)
    val highlightPx = duPx(1f)
    val highlightInsetPx = duPx(14f)
    val highlightTopPx = duPx(3f)
    val pillShape = RoundedCornerShape(23.duToDp())

    Box(
        modifier = Modifier
            .width(48.duToDp())
            .height(46.duToDp())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Orange glow (alpha .45, blur 14).
                val glowPaint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = VitalColors.orange.toArgb()
                    alpha = (0.45f * 255).toInt()
                    maskFilter = BlurMaskFilter(glowBlurPx, BlurMaskFilter.Blur.NORMAL)
                }
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        highlightPx,
                        highlightPx,
                        size.width - highlightPx,
                        size.height - highlightPx,
                        pillRadiusPx,
                        pillRadiusPx,
                        glowPaint
                    )
                }
                // Orange pill.
                drawRoundRect(
                    color = VitalColors.orange,
                    cornerRadius = CornerRadius(pillRadiusPx, pillRadiusPx)
                )
                // Inner top highlight white 40 %.
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(highlightInsetPx, highlightTopPx),
                    end = Offset(size.width - highlightInsetPx, highlightTopPx),
                    strokeWidth = highlightPx,
                    cap = StrokeCap.Round
                )
            }
        }
        Text(
            text = label,
            color = if (isSelected) VitalColors.black else VitalColors.tabInactive,
            fontSize = 10.duToSp(),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.12.em
        )
    }
}

@Composable
private fun GlassPlusButton(onClick: () -> Unit) {
    val strokePx = duPx(1.5f)
    val glowBlurPx = duPx(12f)
    val iconStrokePx = duPx(2.6f)
    val iconArmPx = duPx(10f)

    Box(
        modifier = Modifier
            .size(56.duToDp()) // tap target 56 du, drawn circle 48 du
            .semantics { contentDescription = "Quick add" }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(48.duToDp())) {
            val centre = Offset(size.width / 2f, size.height / 2f)
            val circleRadiusPx = size.minDimension / 2f

            // Faint orange glow.
            val glowPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = VitalColors.orange.toArgb()
                alpha = (0.25f * 255).toInt()
                maskFilter = BlurMaskFilter(glowBlurPx, BlurMaskFilter.Blur.NORMAL)
            }
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawCircle(centre.x, centre.y, circleRadiusPx, glowPaint)
            }

            // Circle: black 55 % fill, 1.5 du orange 75 % stroke.
            drawCircle(color = VitalColors.black.copy(alpha = 0.55f), radius = circleRadiusPx, center = centre)
            drawCircle(
                color = VitalColors.orange.copy(alpha = 0.75f),
                radius = circleRadiusPx - strokePx / 2f,
                center = centre,
                style = Stroke(width = strokePx)
            )

            // Plus icon: 20 du, stroke 2.6, round caps.
            drawLine(
                color = VitalColors.orange,
                start = centre - Offset(iconArmPx, 0f),
                end = centre + Offset(iconArmPx, 0f),
                strokeWidth = iconStrokePx,
                cap = StrokeCap.Round
            )
            drawLine(
                color = VitalColors.orange,
                start = centre - Offset(0f, iconArmPx),
                end = centre + Offset(0f, iconArmPx),
                strokeWidth = iconStrokePx,
                cap = StrokeCap.Round
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Glass nav", showBackground = true, backgroundColor = 0xFF000000, widthDp = 393, heightDp = 120)
@Composable
private fun GlassBottomNavPreview() {
    com.vital.ui.theme.VitalTheme(trueBlack = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VitalColors.black),
            contentAlignment = Alignment.BottomCenter
        ) {
            GlassBottomNav(selectedTab = 1, onTabSelected = {}, onPlusClick = {})
        }
    }
}
