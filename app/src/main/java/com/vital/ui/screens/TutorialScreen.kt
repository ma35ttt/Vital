package com.vital.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.R
import com.vital.data.entity.ActivityLevel
import com.vital.data.entity.GoalType
import com.vital.data.entity.Settings
import com.vital.data.entity.Sex
import com.vital.domain.bodyGoalPlan
import com.vital.ui.TutorialViewModel
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.Hairline
import com.vital.ui.components.QuickAddAction
import com.vital.ui.components.QuickAddGlyph
import com.vital.ui.components.VITAL_TABS
import com.vital.ui.components.VitalNavGlyph
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val TUTORIAL_PAGES = 8

private const val LB_PER_KG = 2.2046226218

private data class TutorialCopy(val eyebrow: String, val title: String, val body: String)

private val TUTORIAL_COPY = listOf(
    TutorialCopy(
        eyebrow = "Step 05",
        title = "Log anything in a few taps",
        body = "Open More and pick a quick add. Each row jumps straight to the right screen."
    ),
    TutorialCopy(
        eyebrow = "Step 06",
        title = "Today keeps the tally",
        body = "Calories left, the three macros, and whether you've trained — all on one page."
    ),
    TutorialCopy(
        eyebrow = "Step 07",
        title = "Every set counts",
        body = "Log a set and the rest timer starts. It keeps counting while you browse the rest of the app."
    ),
    TutorialCopy(
        eyebrow = "Step 08",
        title = "Watch the trend, not a spreadsheet",
        body = "Weeks turns your logs into adherence, macro hit rates and personal records."
    )
)

/**
 * The first-run walk-through: eight swipeable pages in the editorial style. The first four set up
 * the body goals (welcome, you, goal, numbers), the rest tour the app. Finishing saves the profile
 * and derived target; skipping leaves the stored settings untouched.
 */
@Composable
fun TutorialScreen(viewModel: TutorialViewModel) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { TUTORIAL_PAGES })
    val scope = rememberCoroutineScope()
    var imperial by rememberSaveable { mutableStateOf(false) }
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
                        .clickable(onClick = viewModel::skip)
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            when (index) {
                0 -> WelcomePage()
                1 -> YouPage(
                    draft = draft,
                    imperial = imperial,
                    onImperial = { imperial = it },
                    onSettings = viewModel::setDraft
                )
                2 -> GoalPage(
                    draft = draft,
                    imperial = imperial,
                    onSettings = viewModel::setDraft
                )
                3 -> NumbersPage(draft = draft, imperial = imperial)
                else -> FeaturePage(index = index - 4)
            }
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
                        viewModel.finish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(page + 1) }
                    }
                }
            )
        }
    }
}

// ---- pages ---------------------------------------------------------------------------------

@Composable
private fun WelcomePage() {
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
            WelcomeVisual()
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FeaturePage(index: Int) {
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
                0 -> QuickAddVisual()
                1 -> TodayVisual()
                2 -> TrainVisual()
                else -> ProgressVisual()
            }
        }

        val copy = TUTORIAL_COPY[index]
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
        Spacer(Modifier.height(24.dp))
    }
}

// ---- onboarding: you -----------------------------------------------------------------------

@Composable
private fun YouPage(
    draft: Settings,
    imperial: Boolean,
    onImperial: (Boolean) -> Unit,
    onSettings: (Settings) -> Unit
) {
    val plan = remember(draft) { bodyGoalPlan(draft) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp)
    ) {
        Eyebrow("Step 02")
        Spacer(Modifier.height(10.dp))
        Title("A few numbers to size it up")
        Spacer(Modifier.height(10.dp))
        Body("Height, age, sex and how much you move set your energy estimate.")
        Spacer(Modifier.height(16.dp))

        TutSheet {
            TutSegRow(
                label = "UNITS",
                options = listOf("Metric", "Imperial"),
                selected = if (imperial) 1 else 0,
                onSelect = { onImperial(it == 1) }
            )
            Hairline()
            TutSegRow(
                label = "SEX",
                options = listOf("Male", "Female"),
                selected = if (draft.sex == Sex.MALE) 0 else 1,
                onSelect = {
                    onSettings(draft.copy(sex = if (it == 0) Sex.MALE else Sex.FEMALE))
                }
            )
            Hairline()
            TutStepper(
                label = "AGE",
                value = "${draft.age} yr",
                onMinus = { onSettings(draft.copy(age = (draft.age - 1).coerceAtLeast(13))) },
                onPlus = { onSettings(draft.copy(age = (draft.age + 1).coerceAtMost(100))) }
            )
            Hairline()
            TutStepper(
                label = "HEIGHT",
                value = tutHeight(draft.heightCm, imperial),
                onMinus = {
                    onSettings(draft.copy(heightCm = (draft.heightCm - 1.0).coerceAtLeast(120.0)))
                },
                onPlus = {
                    onSettings(draft.copy(heightCm = (draft.heightCm + 1.0).coerceAtMost(230.0)))
                }
            )
        }

        Spacer(Modifier.height(20.dp))
        FieldLabel("HOW MUCH CARDIO DO YOU DO?")
        Spacer(Modifier.height(10.dp))
        CardioGrid(selected = draft.activityLevel) { level ->
            onSettings(draft.copy(activityLevel = level))
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = buildAnnotatedString {
                append("${activityLabel(draft.activityLevel)} cardio → maintenance of ")
                withStyle(SpanStyle(color = VitalColors.ink, fontWeight = FontWeight.SemiBold)) {
                    append("${plan.maintenanceKcal} kcal")
                }
                append(".")
            },
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(6.dp))
        Fine("Your energy needs are an estimate, accurate to about ±10%. We refine it as you log.")
        Spacer(Modifier.height(24.dp))
    }
}

// ---- onboarding: goal ----------------------------------------------------------------------

@Composable
private fun GoalPage(
    draft: Settings,
    imperial: Boolean,
    onSettings: (Settings) -> Unit
) {
    val plan = remember(draft) { bodyGoalPlan(draft) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp)
    ) {
        Eyebrow("Step 03")
        Spacer(Modifier.height(10.dp))
        Title("Where are you headed?")
        Spacer(Modifier.height(16.dp))

        TutSheet {
            TutStepper(
                label = "CURRENT WEIGHT",
                value = tutWeight(draft.bodyWeightKg, imperial),
                onMinus = {
                    onSettings(
                        draft.copy(bodyWeightKg = (draft.bodyWeightKg - 0.5).coerceAtLeast(30.0))
                    )
                },
                onPlus = {
                    onSettings(
                        draft.copy(bodyWeightKg = (draft.bodyWeightKg + 0.5).coerceAtMost(300.0))
                    )
                }
            )
            Hairline()
            TutStepper(
                label = "GOAL WEIGHT",
                value = draft.goalWeightKg?.let { tutWeight(it, imperial) } ?: "Not set",
                onMinus = {
                    draft.goalWeightKg?.let {
                        onSettings(draft.copy(goalWeightKg = (it - 0.5).coerceAtLeast(30.0)))
                    }
                },
                onPlus = {
                    val next = draft.goalWeightKg?.let { (it + 0.5).coerceAtMost(300.0) }
                        ?: (draft.bodyWeightKg - 5.0).coerceAtLeast(30.0)
                    onSettings(draft.copy(goalWeightKg = next))
                }
            )
        }

        Spacer(Modifier.height(14.dp))
        ChoiceChips(
            options = listOf("Lose fat", "Hold", "Build"),
            selected = when (draft.goalType) {
                GoalType.LOSE -> 0
                GoalType.MAINTAIN -> 1
                GoalType.GAIN -> 2
            },
            onSelect = {
                onSettings(
                    draft.copy(
                        goalType = when (it) {
                            0 -> GoalType.LOSE
                            1 -> GoalType.MAINTAIN
                            else -> GoalType.GAIN
                        }
                    )
                )
            }
        )

        Spacer(Modifier.height(18.dp))
        HealthyRangeBar(
            heightCm = draft.heightCm,
            nowKg = draft.bodyWeightKg,
            goalKg = draft.goalWeightKg,
            plan = plan,
            imperial = imperial,
            alwaysShowLabels = true
        )

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FieldLabel("EST. MAINTENANCE")
            Text(
                text = "${plan.maintenanceKcal} kcal",
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 13.sp
            )
        }
        Text(
            text = "at ${activityLabel(draft.activityLevel).lowercase()} cardio · estimate ±10%",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 10.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
        Spacer(Modifier.height(24.dp))
    }
}

// ---- onboarding: numbers -------------------------------------------------------------------

@Composable
private fun NumbersPage(draft: Settings, imperial: Boolean) {
    val plan = remember(draft) { bodyGoalPlan(draft) }
    val goal = draft.goalWeightKg
    val inRange = goal != null &&
        goal in plan.healthyRange.minKg..plan.healthyRange.maxKg

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp)
    ) {
        Eyebrow("Step 04")
        Spacer(Modifier.height(10.dp))
        Title("Here's your daily plan")

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = String.format(Locale.US, "%,d", plan.targetKcal),
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 46.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.04).em
            )
            Text(
                text = " kcal / day",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = planSentence(draft, plan, imperial),
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(Modifier.height(18.dp))
        TutSheet {
            MacroSheetRow(label = "PROTEIN", value = "${plan.macros.proteinG.roundToInt()} g")
            Hairline()
            MacroSheetRow(label = "CARBS", value = "${plan.macros.carbsG.roundToInt()} g")
            Hairline()
            MacroSheetRow(label = "FAT", value = "${plan.macros.fatG.roundToInt()} g")
            Hairline()
            MacroSheetRow(label = "MAINTENANCE", value = "${plan.maintenanceKcal} kcal")
        }

        if (goal != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = if (inRange) {
                    "Your goal of ${tutWeight(goal, imperial)} sits inside the healthy range for " +
                        "your height."
                } else {
                    "Your goal of ${tutWeight(goal, imperial)} is outside the healthy range for " +
                        "your height — worth a second look."
                },
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.InterTight,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(8.dp))
        Fine("Maintenance is an estimate (±10%); we tune it as you log.")
        Spacer(Modifier.height(24.dp))
    }
}

// ---- onboarding pieces ---------------------------------------------------------------------

@Composable
private fun Eyebrow(text: String) {
    Text(
        text = text.uppercase(),
        color = VitalColors.orange,
        fontFamily = VitalFonts.InterTight,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.18.em
    )
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        color = VitalColors.ink,
        fontFamily = VitalFonts.InterTight,
        fontSize = 30.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.03).em,
        lineHeight = 33.sp
    )
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.InterTight,
        fontSize = 13.5.sp,
        lineHeight = 20.sp
    )
}

@Composable
private fun Fine(text: String) {
    Text(
        text = text,
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.JetBrainsMonoRegular,
        fontSize = 10.sp,
        lineHeight = 15.sp
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.JetBrainsMonoRegular,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.12.em
    )
}

@Composable
private fun TutSheet(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VitalColors.sheet, RoundedCornerShape(16.dp))
            .border(1.dp, VitalColors.rule, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 2.dp),
        content = content
    )
}

@Composable
private fun TutSegRow(
    label: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FieldLabel(label)
        Spacer(Modifier.weight(1f))
        Segmented(
            options = options,
            selected = selected,
            onSelect = onSelect,
            modifier = Modifier.width(150.dp)
        )
    }
}

@Composable
private fun TutStepper(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FieldLabel(label)
        Spacer(Modifier.weight(1f))
        TutStepButton(symbol = "−", onClick = onMinus)
        Text(
            text = value,
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(84.dp)
        )
        TutStepButton(symbol = "+", onClick = onPlus)
    }
}

@Composable
private fun TutStepButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .border(1.dp, VitalColors.ruleStrong, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun CardioGrid(selected: ActivityLevel, onSelect: (ActivityLevel) -> Unit) {
    val options = listOf(
        Triple(ActivityLevel.NONE, "None", "little / none"),
        Triple(ActivityLevel.LIGHT, "Light", "1–2× / wk"),
        Triple(ActivityLevel.MODERATE, "Moderate", "3–4× / wk"),
        Triple(ActivityLevel.HIGH, "High", "5+× / wk")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { (level, title, sub) ->
                    val on = level == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (on) VitalColors.orange.copy(alpha = 0.10f) else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (on) VitalColors.orange else VitalColors.ruleStrong,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(level) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = title,
                            color = VitalColors.ink,
                            fontFamily = VitalFonts.InterTight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = sub,
                            color = VitalColors.inkMuted,
                            fontFamily = VitalFonts.JetBrainsMonoRegular,
                            fontSize = 10.sp,
                            letterSpacing = 0.02.em
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroSheetRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FieldLabel(label)
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---- copy helpers --------------------------------------------------------------------------

private fun activityLabel(level: ActivityLevel): String = when (level) {
    ActivityLevel.NONE -> "None"
    ActivityLevel.LIGHT -> "Light"
    ActivityLevel.MODERATE -> "Moderate"
    ActivityLevel.HIGH -> "High"
}

private fun tutWeight(kg: Double, imperial: Boolean): String {
    val value = if (imperial) kg * LB_PER_KG else kg
    val text = String.format(Locale.US, "%.1f", value)
        .let { if (it.endsWith(".0")) it.removeSuffix(".0") else it }
    return "$text ${if (imperial) "lb" else "kg"}"
}

private fun tutHeight(cm: Double, imperial: Boolean): String {
    if (!imperial) return "${cm.roundToInt()} cm"
    val totalInches = (cm / 2.54).roundToInt()
    return "${totalInches / 12}'${totalInches % 12}\""
}

private fun planSentence(
    draft: Settings,
    plan: com.vital.domain.BodyGoalPlan,
    imperial: Boolean
): String = when (draft.goalType) {
    GoalType.MAINTAIN ->
        "Holding at maintenance, around ${tutWeight(draft.bodyWeightKg, imperial)}."
    GoalType.LOSE, GoalType.GAIN -> {
        val goal = draft.goalWeightKg?.let { tutWeight(it, imperial) } ?: "your goal"
        val weeks = plan.weeks
        val direction = if (draft.goalType == GoalType.LOSE) "deficit" else "surplus"
        if (weeks != null) {
            "A ${plan.deficitKcal} kcal $direction to reach $goal in about $weeks weeks."
        } else {
            "A ${plan.deficitKcal} kcal $direction to reach $goal."
        }
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