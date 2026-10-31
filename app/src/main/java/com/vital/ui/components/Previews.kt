package com.vital.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF141414)
fun AllComponentsPreview() {
    var knobProgress by remember { mutableStateOf(0.5f) }
    var period by remember { mutableStateOf(Period.M) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = com.vital.ui.theme.VitalColors.Background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Knurled Knob
            KnurledKnob(
                modifier = Modifier.size(120.dp),
                progress = knobProgress,
                onProgressChange = { knobProgress = it }
            )

            // Hero Line Graph
            HeroLineGraph(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                dataPoints = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f),
                labels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
                accentColor = com.vital.ui.theme.VitalColors.AccentTDY,
                showTooltip = true,
                tooltipIndex = 4,
                tooltipValue = 0.52f
            )

            // Sparkline row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Sparkline(
                    modifier = Modifier.weight(1f).height(60.dp),
                    dataPoints = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f),
                    accentColor = com.vital.ui.theme.VitalColors.AccentTDY
                )
                Sparkline(
                    modifier = Modifier.weight(1f).height(60.dp),
                    dataPoints = listOf(0.4f, 0.5f, 0.45f, 0.6f, 0.55f),
                    accentColor = com.vital.ui.theme.VitalColors.AccentMLS
                )
                Sparkline(
                    modifier = Modifier.weight(1f).height(60.dp),
                    dataPoints = listOf(0.3f, 0.4f, 0.5f, 0.45f, 0.5f),
                    accentColor = com.vital.ui.theme.VitalColors.AccentLFT
                )
            }

            // Macro Row
            MacroRow(
                protein = 120f,
                carbs = 180f,
                fat = 65f,
                proteinTarget = 180,
                carbsTarget = 250,
                fatTarget = 85
            )

            // Capsule Rows
            capsuleRow(
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.LocalFireDepartment, "", tint = com.vital.ui.theme.VitalColors.AccentTDY) },
                label = "Calories Burned",
                value = "420 kcal",
                sparklineData = listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f),
                accentColor = com.vital.ui.theme.VitalColors.AccentTDY
            )

            capsuleRow(
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.DirectionsRun, "", tint = com.vital.ui.theme.VitalColors.AccentLFT) },
                label = "Volume",
                value = "12,450 kg",
                sparklineData = listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.65f),
                accentColor = com.vital.ui.theme.VitalColors.AccentLFT
            )
        }
    }
}
