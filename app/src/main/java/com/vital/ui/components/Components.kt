package com.vital.ui.components

import com.vital.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.duToSp
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.LocalDesignUnitsScale

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = com.vital.ui.theme.VitalColors.TagButtonFill,
    textColor: Color = com.vital.ui.theme.VitalColors.PrimaryText,
    fontSize: TextUnit = 11.sp,
    elevation: androidx.compose.ui.unit.Dp = 2.dp
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .insetShadow(elevation = elevation, cornerRadius = 4.dp, shadowColor = com.vital.ui.theme.VitalColors.BackgroundTrueBlack.copy(alpha = 0.4f)),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun capsuleRow(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    sparklineData: List<Float>? = null,
    accentColor: Color,
    dateLabel: String = "",
    onClick: (() -> Unit)? = null
) {
    // Capsule: 90.7 du height, half-height radius, #0D0D0D fill
    // Darker outer ring ~6.5 du, light rim line at bottom
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(90.7.dp), // ~90.7 du at 1x scale
        color = com.vital.ui.theme.VitalColors.CapsuleFill, // #0D0D0D
        shape = RoundedCornerShape(45.35.dp), // half height
        border = BorderStroke(1.dp, com.vital.ui.theme.VitalColors.CapsuleDarkRing) // #030303 darker ring ~6.5 du
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = { onClick?.invoke() }),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon badge (circle)
            androidx.compose.material3.Surface(
                modifier = Modifier.size(44.dp),
                color = accentColor.copy(alpha = 0.18f),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            // Label and value (center)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label.uppercase(),
                    color = com.vital.ui.theme.VitalColors.MutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = value,
                    color = com.vital.ui.theme.VitalColors.PrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            // Grey thumbnail panel with sparkline (108.7×73 du, flat #5F6971) — inset shadow for depth
            sparklineData?.let { data ->
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .width(108.7.dp)  // 108.7 du
                        .height(73.dp)    // 73 du
                        .padding(4.dp)
                        .insetShadow(elevation = 2.dp, cornerRadius = 6.dp, shadowColor = com.vital.ui.theme.VitalColors.BackgroundTrueBlack.copy(alpha = 0.5f)),
                    color = com.vital.ui.theme.VitalColors.InsetGreyPanel, // #5F6971 flat
                    shape = RoundedCornerShape(6.dp) // small radius for thumbnail
                ) {
                    Sparkline(
                        modifier = Modifier.fillMaxSize(),
                        dataPoints = data,
                        accentColor = accentColor,
                        dateLabel = dateLabel,
                        rulerLabels = listOf("960", "980", "990"), // Numbers, NOT MIN AVG MAX
                        activeRulerIndex = 1 // Middle (980) active = dark
                    )
                }
            }
        }

        // Light rim line at bottom edge (CapsuleRimLine #1A1A1A)
        // Draw as a bottom border overlay - wrap in Box to get BoxScope for align
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(1.dp)
                    .padding(horizontal = 8.dp)
                    .background(com.vital.ui.theme.VitalColors.CapsuleRimLine) // #1A1A1A
            )
        }
    }
}

// JetBrains Mono from res/font
val jetbrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_400, FontWeight.Normal),
    Font(R.font.jetbrains_mono_700, FontWeight.Bold)
)

@Composable
fun ShowMoreButton(
    modifier: Modifier = Modifier,
    accentColor: Color,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .insetShadow(elevation = 2.dp, cornerRadius = 8.dp, shadowColor = accentColor.copy(alpha = 0.3f)),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, accentColor)
    ) {
        Text(
            text = "SHOW MORE",
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = jetbrainsMono,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    showMoreAction: (() -> Unit)? = null,
    accentColor: Color = com.vital.ui.theme.VitalColors.AccentTDY
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = com.vital.ui.theme.VitalColors.PrimaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = jetbrainsMono,
            letterSpacing = 1.sp
        )

        showMoreAction?.let {
            ShowMoreButton(
                modifier = Modifier.width(100.dp),
                accentColor = accentColor,
                onClick = it
            )
        }
    }
}

@Composable
fun MacroRow(
    protein: Float,
    carbs: Float,
    fat: Float,
    proteinTarget: Int,
    carbsTarget: Int,
    fatTarget: Int,
    modifier: Modifier = Modifier
) {
    val duScale = LocalDesignUnitsScale.current
    val dimInk = com.vital.ui.theme.VitalColors.RefInk.copy(alpha = 0.5f)
    // 3 columns starting at x 15.5 with pitch ~126
    // Parent TodayScreen provides start padding, so we use relative positions
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(126f.duToDp())
    ) {
        MacroStat(
            label = "P",
            current = protein.toInt(),
            target = proteinTarget,
            labelTop = 0f, // relative to MacroRow top
            valueTop = 19.1f, // 160.6 - 141.5 = 19.1 du below label top
            duScale = duScale,
            dimInk = dimInk
        )
        MacroStat(
            label = "C",
            current = carbs.toInt(),
            target = carbsTarget,
            labelTop = 0f,
            valueTop = 19.1f,
            duScale = duScale,
            dimInk = dimInk
        )
        MacroStat(
            label = "F",
            current = fat.toInt(),
            target = fatTarget,
            labelTop = 0f,
            valueTop = 19.1f,
            duScale = duScale,
            dimInk = dimInk
        )
    }
}

@Composable
fun MacroStat(
    label: String,
    current: Int,
    target: Int,
    labelTop: Float,
    valueTop: Float,
    duScale: Float,
    dimInk: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        // Label: cap height 10 at top
        Text(
            text = label,
            color = dimInk,
            fontSize = (10f * duScale).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = jetbrainsMono
        )
        // Spacer to position value at valueTop
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(((valueTop - 10f) * duScale).dp))
        // Value: 17.3 tall, unit at 60% size on same baseline
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = current.toString(),
                color = com.vital.ui.theme.VitalColors.PrimaryText,
                fontSize = (17.3f * duScale).sp,
                fontWeight = FontWeight.Medium,
                fontFamily = jetbrainsMono
            )
            Text(
                text = "/$target",
                color = com.vital.ui.theme.VitalColors.PrimaryText.copy(alpha = 0.7f),
                fontSize = (17.3f * duScale * 0.6f).sp,
                fontWeight = FontWeight.Medium,
                fontFamily = jetbrainsMono
            )
        }
    }
}

@Composable
fun BigKcalDisplay(
    remaining: Float,
    unit: String = "KCAL",
    accentColor: Color = com.vital.ui.theme.VitalColors.AccentTDY,
    modifier: Modifier = Modifier
) {
    val duScale = LocalDesignUnitsScale.current
    // Left-aligned, digit height 62.7, KCAL superscript to right of digits
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = remaining.toInt().toString(),
            color = com.vital.ui.theme.VitalColors.RefInk,
            fontSize = (62.7f * duScale).sp,
            fontWeight = FontWeight.Light,
            fontFamily = jetbrainsMono,
            letterSpacing = 2.sp
        )
        // KCAL as small ink superscript to right of digits, clear of them
        Text(
            text = unit,
            color = com.vital.ui.theme.VitalColors.RefInk,
            fontSize = (14f * duScale).sp,
            fontWeight = FontWeight.Light,
            fontFamily = jetbrainsMono,
            letterSpacing = 1.sp,
            modifier = Modifier
                .padding(start = (4f * duScale).dp)
                .padding(top = (4f * duScale).dp) // superscript offset
        )
    }
}

@Composable
fun QuickStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = color.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}