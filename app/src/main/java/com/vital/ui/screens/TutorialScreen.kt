package com.vital.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.R
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.Hairline
import com.vital.ui.components.QuickAddAction
import com.vital.ui.components.QuickAddGlyph
import com.vital.ui.components.VITAL_TABS
import com.vital.ui.components.VitalNavGlyph
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import kotlinx.coroutines.launch

private const val TUTORIAL_PAGES = 5

private data class TutorialCopy(val eyebrow: String, val title: String, val body: String)

private val TUTORIAL_COPY = listOf(
    TutorialCopy(
        eyebrow = "Step 02",
        title = "Log anything in a few taps",
        body = "Open More and pick a quick add. Each row jumps straight to the right screen."
    ),
    TutorialCopy(
        eyebrow = "Step 03",
        title = "Today keeps the tally",
        body = "Calories left, the three macros, and whether you've trained — all on one page."
    ),
    TutorialCopy(
        eyebrow = "Step 04",
        title = "Every set counts",
        body = "Log a set and the rest timer starts. It keeps counting while you browse the rest of the app."
    ),
    TutorialCopy(
        eyebrow = "Step 05",
        title = "Watch the trend, not a spreadsheet",
        body = "Weeks turns your logs into adherence, macro hit rates and personal records."
    )
)

/**
 * The first-run walk-through: five swipeable pages in the editorial style, shown once per
 * install by [com.vital.ui.TutorialViewModel]. Swipe or tap Next; Skip and Get started finish it.
 */
@Composable
fun TutorialScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { TUTORIAL_PAGES })
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitalColors.paper)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page > 0) {
                Text(
                    text = "0${page + 1} / 0$TUTORIAL_PAGES",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp,
                    letterSpacing = 0.06.em
                )
            }
            Spacer(Modifier.weight(1f))
            if (page < TUTORIAL_PAGES - 1) {
                Text(
                    text = "Skip",
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp,
                    letterSpacing = 0.06.em,
                    modifier = Modifier
                        .clickable(onClick = onFinish)
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            TutorialPage(index = index)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            Dots(current = page)
            Spacer(Modifier.height(18.dp))
            EditorialButton(
                text = if (page == TUTORIAL_PAGES - 1) "Get started" else "Next",
                onClick = {
                    if (page == TUTORIAL_PAGES - 1) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(page + 1) }
                    }
                }
            )
        }
    }
}

@Composable
private fun TutorialPage(index: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (index) {
                0 -> WelcomeVisual()
                1 -> QuickAddVisual()
                2 -> TodayVisual()
                3 -> TrainVisual()
                else -> ProgressVisual()
            }
        }

        if (index > 0) {
            val copy = TUTORIAL_COPY[index - 1]
            Text(
                text = copy.eyebrow.uppercase(),
                color = VitalColors.orange,
                fontFamily = VitalFonts.InterTight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.18.em
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = copy.title,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.03).em,
                lineHeight = 33.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = copy.body,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ---- page visuals --------------------------------------------------------------------------

@Composable
private fun WelcomeVisual() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_round),
            contentDescription = null,
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = buildAnnotatedString {
                append("Vital")
                withStyle(SpanStyle(color = VitalColors.orange)) { append(".") }
            },
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 56.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.04).em
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Food, training and the numbers that matter — kept in one place.",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        Spacer(Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VITAL_TABS.forEachIndexed { index, tab ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    VitalNavGlyph(
                        tabIndex = index,
                        color = VitalColors.inkMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(9.dp))
                    Text(
                        text = tab.label.uppercase(),
                        color = VitalColors.navInactive,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 9.sp,
                        letterSpacing = 0.08.em
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAddVisual() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VitalColors.sheet, RoundedCornerShape(16.dp))
            .border(1.dp, VitalColors.rule, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp)
    ) {
        QuickAddAction.entries.forEachIndexed { index, action ->
            if (index > 0) Hairline()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickAddGlyph(
                    action = action,
                    color = VitalColors.orange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = action.label.lowercase().replaceFirstChar { it.uppercase() },
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TodayVisual() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "KCAL LEFT",
            color = VitalColors.orange,
            fontFamily = VitalFonts.InterTight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.18.em
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "1,240",
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 64.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.05).em
            )
            Text(
                text = " / 2,100",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(VitalColors.rule, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.59f)
                    .background(VitalColors.orange, RoundedCornerShape(3.dp))
            )
        }
        Spacer(Modifier.height(22.dp))
        Hairline()
        MetricRow("PROTEIN", "110 / 160 g")
        Hairline()
        MetricRow("CARBS", "180 / 240 g")
        Hairline()
        MetricRow("LIFTED TODAY", "Yes", accent = true)
        Hairline()
    }
}

@Composable
private fun TrainVisual() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "REST",
            color = VitalColors.orange,
            fontFamily = VitalFonts.InterTight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.18.em
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "1:32",
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 46.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.04).em
        )
        Spacer(Modifier.height(26.dp))
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = VitalColors.rule,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = VitalColors.orange,
                startAngle = -90f,
                sweepAngle = 200f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RestPreset("2:00", selected = false)
            RestPreset("3:00", selected = true)
            RestPreset("4:00", selected = false)
        }
        Spacer(Modifier.height(28.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VitalColors.sheet, RoundedCornerShape(16.dp))
                .border(1.dp, VitalColors.rule, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Barbell Squat",
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                MiniTag(text = "COMPOUND")
            }
        }
    }
}

@Composable
private fun ProgressVisual() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ADHERENCE",
            color = VitalColors.orange,
            fontFamily = VitalFonts.InterTight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.18.em
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "82",
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 64.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.05).em
            )
            Text(
                text = "%",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "12 weeks · 69 of 84 days",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(18.dp))
        val bars = listOf(0.52f, 0.66f, 0.44f, 0.78f, 0.60f, 0.92f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bars.forEachIndexed { index, fraction ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction)
                        .background(
                            color = if (index == bars.lastIndex) {
                                VitalColors.orange
                            } else {
                                VitalColors.ruleStrong
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Hairline()
        MetricRow("BODY WEIGHT", "79.4 kg")
        Hairline()
        MetricRow("BENCH PR", "102.5 kg")
        Hairline()
    }
}

// ---- small pieces --------------------------------------------------------------------------

@Composable
private fun MetricRow(
    label: String,
    value: String,
    accent: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp,
            letterSpacing = 0.04.em,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = if (accent) VitalColors.orange else VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RestPreset(text: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            color = if (selected) VitalColors.ink else VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(9.dp))
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(2.dp)
                .background(if (selected) VitalColors.orange else Color.Transparent)
        )
    }
}

@Composable
private fun MiniTag(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = VitalColors.orange,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.em
        )
    }
}

@Composable
private fun Dots(current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(TUTORIAL_PAGES) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(if (active) 22.dp else 7.dp)
                    .background(
                        color = if (active) VitalColors.orange else VitalColors.ruleStrong,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
