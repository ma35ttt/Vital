package com.vital.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = com.vital.ui.theme.VitalColors.TagButtonFill,
    textColor: Color = com.vital.ui.theme.VitalColors.PrimaryText,
    fontSize: TextUnit = 11.sp
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp),
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
    onClick: (() -> Unit)? = null
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = com.vital.ui.theme.VitalColors.NearBlackRow,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, com.vital.ui.theme.VitalColors.NearBlackRowStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = { onClick?.invoke() }),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon badge
            androidx.compose.material3.Surface(
                modifier = Modifier.size(40.dp),
                color = accentColor.copy(alpha = 0.2f),
                shape = CircleShape
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
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label.uppercase(),
                    color = com.vital.ui.theme.VitalColors.MutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Text(
                    text = value,
                    color = com.vital.ui.theme.VitalColors.PrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Sparkline in grey panel (reference frame-01 style)
            sparklineData?.let { data ->
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .width(100.dp)
                        .height(56.dp)
                        .padding(4.dp),
                    color = com.vital.ui.theme.VitalColors.InsetGreyPanel,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Sparkline(
                            modifier = Modifier.fillMaxWidth().height(28.dp).padding(top = 2.dp),
                            dataPoints = data,
                            accentColor = accentColor
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("MIN", color = com.vital.ui.theme.VitalColors.Background, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("MAX", color = com.vital.ui.theme.VitalColors.Background, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

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
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, accentColor)
    ) {
        Text(
            text = "SHOW MORE",
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MacroStat(
            label = "P",
            current = protein.toInt(),
            target = proteinTarget,
            color = com.vital.ui.theme.VitalColors.MacroProtein,
            modifier = Modifier.weight(1f)
        )
        MacroStat(
            label = "C",
            current = carbs.toInt(),
            target = carbsTarget,
            color = com.vital.ui.theme.VitalColors.MacroCarbs,
            modifier = Modifier.weight(1f)
        )
        MacroStat(
            label = "F",
            current = fat.toInt(),
            target = fatTarget,
            color = com.vital.ui.theme.VitalColors.MacroFat,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MacroStat(
    label: String,
    current: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "$current / $target",
            color = com.vital.ui.theme.VitalColors.PrimaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        // Mini progress bar
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .padding(top = 4.dp)
        ) {
            val progress = (current.toFloat() / target).coerceIn(0f, 1f)
            val width = size.width * progress
            drawRect(
                color = com.vital.ui.theme.VitalColors.NearBlackRowStroke,
                topLeft = Offset.Zero,
                size = size
            )
            drawRect(
                color = color,
                topLeft = Offset.Zero,
                size = androidx.compose.ui.geometry.Size(width, size.height)
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = remaining.toInt().toString(),
            color = com.vital.ui.theme.VitalColors.PrimaryText,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        )
        Text(
            text = unit,
            color = accentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.sp
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