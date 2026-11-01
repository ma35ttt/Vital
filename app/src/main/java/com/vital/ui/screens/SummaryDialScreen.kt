package com.vital.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.KnurledKnob
import com.vital.ui.components.SummaryDial
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import com.vital.ui.components.Period
import androidx.compose.material.icons.Icons

@Composable
fun SummaryDialScreen(
    onClose: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val kcalRemaining by viewModel.remainingKcal.collectAsStateWithLifecycle(2500f)
    val kcalTarget by viewModel.kcalTarget.collectAsStateWithLifecycle(2500)
    val proteinPercent by viewModel.proteinPercent.collectAsStateWithLifecycle(0.3f)
    val carbsPercent by viewModel.carbsPercent.collectAsStateWithLifecycle(0.4f)
    val fatPercent by viewModel.fatPercent.collectAsStateWithLifecycle(0.3f)
    val period by viewModel.summaryPeriod.collectAsStateWithLifecycle(Period.M)

    var knobProgress by remember { mutableStateOf(0.5f) }

    val accentColor = VitalColors.AccentTDY
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

    // Full accent background per SPEC.md - SummaryDial fills entire screen
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = accentColor
    ) {
        SummaryDial(
            modifier = Modifier.fillMaxSize(),
            kcalRemaining = kcalRemaining,
            kcalTarget = kcalTarget.toFloat(),
            proteinPercent = proteinPercent,
            carbsPercent = carbsPercent,
            fatPercent = fatPercent,
            period = period,
            onPeriodChange = { },
            accentColor = VitalColors.Background, // dark accent on orange bg
            showKnob = true,
            onKnobProgressChange = { knobProgress = it }
        )
    }
}

@Composable
fun InfoPill(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(56.dp),
        color = VitalColors.NearBlackRow,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, VitalColors.NearBlackRowStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, color = VitalColors.MutedText, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

object SummaryDialScreenPreviewData {}