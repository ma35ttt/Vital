package com.vital.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Editorial dark primitives: hairlines and space instead of cards, one orange accent,
 * Inter Tight for language and JetBrains Mono for numbers.
 */

@Composable
fun editorialPageColor() = VitalColors.paper

@Composable
fun EditorialHeader(
    title: String,
    dateLabel: String,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Back",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Text(
                    text = "‹ $backLabel",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp,
                    letterSpacing = 0.02.em,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(vertical = 4.dp, horizontal = 1.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = dateLabel,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                letterSpacing = 0.04.em
            )
        }
        Text(
            text = title,
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.025).em,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
fun EditorialEyebrow(
    text: String,
    color: androidx.compose.ui.graphics.Color = VitalColors.orange,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        color = color,
        fontFamily = VitalFonts.InterTight,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.18.em,
        modifier = modifier
    )
}

@Composable
fun Hairline(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = VitalColors.rule
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

@Composable
fun EditorialSectionHeader(
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 26.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.16.em
        )
        if (trailing != null) {
            Text(
                text = trailing,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EditorialRuledRow(
    leading: String?,
    title: String,
    trailing: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                Text(
                    text = leading,
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp,
                    letterSpacing = 0.01.em,
                    modifier = Modifier.width(54.dp)
                )
            }
            Text(
                text = title,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.005).em,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End
                )
            }
        }
        Hairline()
    }
}

@Composable
fun EditorialMacroGrid(
    protein: Double,
    carbs: Double,
    fat: Double,
    proteinTarget: Int,
    carbsTarget: Int,
    fatTarget: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            MacroCell(
                label = "Protein",
                value = protein,
                target = proteinTarget,
                modifier = Modifier.weight(1f)
            )
            MacroDivider()
            MacroCell(
                label = "Carbs",
                value = carbs,
                target = carbsTarget,
                modifier = Modifier.weight(1f)
            )
            MacroDivider()
            MacroCell(
                label = "Fat",
                value = fat,
                target = fatTarget,
                modifier = Modifier.weight(1f)
            )
        }
        Hairline()
    }
}

@Composable
private fun MacroCell(
    label: String,
    value: Double,
    target: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 15.dp, horizontal = 20.dp)
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
                text = formatEditorialNumber(value),
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).em
            )
            Text(
                text = "/${target} g",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun MacroDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(VitalColors.rule)
    )
}

@Composable
fun EditorialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) VitalColors.orange else VitalColors.orange.copy(alpha = 0.35f),
            contentColor = VitalColors.onAccent.copy(alpha = if (enabled) 1f else 0.5f),
            disabledContainerColor = VitalColors.orange.copy(alpha = 0.35f),
            disabledContentColor = VitalColors.onAccent.copy(alpha = 0.5f)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.005.em
        )
    }
}

/** `1,240` and `120` — no trailing decimals for whole grams. */
fun formatEditorialNumber(value: Double): String {
    val rounded = value.roundToInt()
    return String.format(Locale.US, "%,d", rounded)
}
