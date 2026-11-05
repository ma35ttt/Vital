package com.vital.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

/**
 * Every theme is one [Palette] of the same editorial tokens. The tokens resolve through
 * [currentPalette], a snapshot state, so every composable that reads a colour recomposes
 * when the theme changes. Legacy tokens from the pre-editorial design stay dark-only; the
 * screens that still use them (progress, coach, debug showcase) are launched from More and
 * are not themed yet.
 */
internal data class Palette(
    val paper: Color,
    val ink: Color,
    val inkMuted: Color,
    val rule: Color,
    val ruleStrong: Color,
    val chartBar: Color,
    val navInactive: Color,
    val sheet: Color,
    val orange: Color,
    val onAccent: Color
)

/** One selectable theme: an id, a label, whether it is a light theme, and its palette. */
internal data class ThemeSpec(
    val id: String,
    val name: String,
    val isLight: Boolean,
    val palette: Palette
)

/**
 * The theme catalog. The first three are the base themes shown at the top of the picker;
 * the rest are the ten designed in `tmp/concepts/theme-gallery.html`. Adding a theme here is
 * all it takes for the picker to show it.
 */
internal object VitalThemes {
    const val DEFAULT_ID = "dark"

    private fun spec(
        id: String,
        name: String,
        isLight: Boolean,
        paper: Long,
        ink: Long,
        inkMuted: Long,
        rule: Long,
        ruleStrong: Long,
        chartBar: Long,
        navInactive: Long,
        sheet: Long,
        accent: Long,
        onAccent: Long
    ) = ThemeSpec(
        id = id,
        name = name,
        isLight = isLight,
        palette = Palette(
            paper = Color(paper), ink = Color(ink), inkMuted = Color(inkMuted),
            rule = Color(rule), ruleStrong = Color(ruleStrong), chartBar = Color(chartBar),
            navInactive = Color(navInactive), sheet = Color(sheet),
            orange = Color(accent), onAccent = Color(onAccent)
        )
    )

    private val white = spec(
        "white", "Default White", true,
        0xFFF2EEE5, 0xFF141210, 0xFF6E675D, 0xFFD9D2C5, 0xFFBFB5A4,
        0xFFDCD5C7, 0xFF8C8377, 0xFFFBF8F2, 0xFFE8420C, 0xFFFFF6EF
    )
    private val dark = spec(
        "dark", "Default Dark", false,
        0xFF0B0B0B, 0xFFF3F1ED, 0xFF9A948C, 0xFF22201D, 0xFF3A3631,
        0xFF2A2724, 0xFF6B6660, 0xFF111110, 0xFFFD3F00, 0xFF0B0B0B
    )
    private val amoled = spec(
        "amoled", "Amoled", false,
        0xFF000000, 0xFFF3F1ED, 0xFF9A948C, 0xFF1A1A1A, 0xFF2E2E2E,
        0xFF222222, 0xFF6B6660, 0xFF0A0A0A, 0xFFFD3F00, 0xFF0B0B0B
    )
    private val ember = spec(
        "ember", "Ember", false,
        0xFF0C0A08, 0xFFF5EFE8, 0xFFA29488, 0xFF241E18, 0xFF38302A,
        0xFF332A20, 0xFF7A6A5C, 0xFF131009, 0xFFFF8A00, 0xFF160E05
    )
    private val cobalt = spec(
        "cobalt", "Cobalt", false,
        0xFF07090E, 0xFFEDF1F7, 0xFF8E97A6, 0xFF161C28, 0xFF28313F,
        0xFF1E2634, 0xFF67728A, 0xFF0C1119, 0xFF3D7BFF, 0xFFF4F7FF
    )
    private val acid = spec(
        "acid", "Acid", false,
        0xFF080B07, 0xFFEEF3E6, 0xFF939C88, 0xFF171E13, 0xFF2A3322,
        0xFF212A1B, 0xFF6C7563, 0xFF0E130A, 0xFFB6F03C, 0xFF0C1206
    )
    private val oxblood = spec(
        "oxblood", "Oxblood", false,
        0xFF170A0C, 0xFFF6ECE5, 0xFFA88C8A, 0xFF291518, 0xFF3D2428,
        0xFF331C20, 0xFF8A6E6C, 0xFF1E0F11, 0xFFD4A24C, 0xFF1A0E06
    )
    private val terminal = spec(
        "terminal", "Terminal", false,
        0xFF040706, 0xFFCBEAD3, 0xFF6E9077, 0xFF0E1B13, 0xFF1B2E22,
        0xFF13291C, 0xFF5C7D66, 0xFF08110C, 0xFF3DFF9E, 0xFF04140A
    )
    private val rosewood = spec(
        "rosewood", "Rosewood", false,
        0xFF150A12, 0xFFF5E9F0, 0xFFA88BA0, 0xFF291525, 0xFF3D2438,
        0xFF331C2E, 0xFF8A6C82, 0xFF1D0F1A, 0xFFFF5C97, 0xFF1A0512
    )
    private val sepia = spec(
        "sepia", "Sepia", true,
        0xFFEDE4D3, 0xFF2A2118, 0xFF7A6A54, 0xFFDACDB9, 0xFFC3B296,
        0xFFE0D4BF, 0xFF8C7C63, 0xFFF6F0E3, 0xFFB4531C, 0xFFFFF6EA
    )
    private val blush = spec(
        "blush", "Blush", true,
        0xFFF8EFF2, 0xFF2A1C24, 0xFF8A6E79, 0xFFEAD6DD, 0xFFD9BEC7,
        0xFFEFDDE3, 0xFF9A8089, 0xFFFCF6F8, 0xFFC2185B, 0xFFFFF3F7
    )
    private val glacier = spec(
        "glacier", "Glacier", true,
        0xFFEAF1F6, 0xFF16212B, 0xFF5E7280, 0xFFD3DEE5, 0xFFB9CAD4,
        0xFFDCE6EC, 0xFF7E909C, 0xFFF4F9FC, 0xFF0E7C9B, 0xFFF1FBFF
    )
    private val kraft = spec(
        "kraft", "Kraft", true,
        0xFFE7DECA, 0xFF26201A, 0xFF6E6250, 0xFFD2C7AE, 0xFFBBAE90,
        0xFFD9CDB4, 0xFF877A62, 0xFFF2EAD8, 0xFF2E6B5E, 0xFFF0FBF6
    )

    /** The three base themes, pinned at the top of the picker. */
    val base: List<ThemeSpec> = listOf(white, dark, amoled)

    /** The ten additional themes. */
    val featured: List<ThemeSpec> =
        listOf(ember, cobalt, acid, oxblood, terminal, rosewood, sepia, blush, glacier, kraft)

    val all: List<ThemeSpec> = base + featured

    private val index: Map<String, ThemeSpec> = all.associateBy { it.id }

    fun byId(id: String): ThemeSpec = index[id] ?: index.getValue(DEFAULT_ID)
}

private val currentPalette = mutableStateOf(VitalThemes.byId(VitalThemes.DEFAULT_ID).palette)

/**
 * Called from [VitalTheme]. True black lifts the paper of any dark theme to pure #000 for
 * AMOLED panels; light themes ignore it. The Amoled theme is already black.
 */
internal fun applyTheme(theme: ThemeSpec, trueBlack: Boolean) {
    val palette = if (!theme.isLight && trueBlack) {
        theme.palette.copy(paper = Color(0xFF000000))
    } else {
        theme.palette
    }
    currentPalette.value = palette
}

object VitalColors {

    // ===== EDITORIAL TOKENS (2026-11) — switch with the mode =====
    val paper get() = currentPalette.value.paper          // page background
    val ink get() = currentPalette.value.ink              // primary text
    val inkMuted get() = currentPalette.value.inkMuted    // labels, meta, trailing values
    val rule get() = currentPalette.value.rule            // hairline separators
    val ruleStrong get() = currentPalette.value.ruleStrong // chart baseline / inactive bars
    val chartBar get() = currentPalette.value.chartBar    // inactive chart bars
    val navInactive get() = currentPalette.value.navInactive // inactive nav icon and label
    val sheet get() = currentPalette.value.sheet          // sheets and dialogs
    val orange get() = currentPalette.value.orange        // the single accent
    val onAccent get() = currentPalette.value.onAccent    // text/knobs on an accent fill

    // ===== LIVE ISLAND =====
    // The island stays a black cutout in both modes, like a real camera housing.
    val islandFill = Color(0xFF050505)
    val islandInk = Color(0xFFF3F1ED)
    val islandInkMuted = Color(0xFF8F8A83)
    val islandRule = Color(0xFF242220)
    val islandAction = Color(0xFF141414)

    // ===== LEGACY DESIGN TOKENS (dark only) =====
    val black = Color(0xFF000000)            // text on orange, legacy page background
    val chipOnOrange = Color(0xFFE83A00)     // header chips on orange screen fill
    val chipOnOrangeStroke = Color(0xFFC93300) // header chips on orange screen stroke
    val chipDark = Color(0xFF0C0C0C)         // header chips on black screen fill
    val chipDarkStroke = Color(0xFF1E1E1E)   // header chips on black screen stroke
    val wellFill = Color(0xFF0A0A0A)         // rows, fields, tiles on black
    val wellStroke = Color(0xFF1F1F1F)       // well stroke
    val wellStrokeFade = Color(0xFF151515)   // well stroke fade inward
    val textPrimary = Color(0xFFF2F2F2)      // text on black and on grey panels
    val textMuted = Color(0xFF8A8A8A)        // small labels
    val tickGrey = Color(0xFF3D3D3D)         // dial tick ring
    val iconWellFill = Color(0xFF121212)     // round icon wells in capsule rows
    val iconWellStroke = Color(0xFF232323)   // icon well stroke
    val panelGrey = Color(0xFF5F6971)        // data panels (use insetShadow, top/left #424143)
    val panelGreyShadow = Color(0xFF424143)  // inner shadow top/left
    val chartBarOnGrey = Color(0xFF171A1D)   // FIX: 3.1:1 on #5F6971
    val sheetTopHighlight = Color(0xFF1C1C1C) // 1 du line at y 404
    val sheetHighlight = sheetTopHighlight    // alias for chrome.md "sheetHighlight"
    val fabRimStroke = Color(0xFF1F1F1F)     // FAB stroke
    val fabRimVisible = Color(0xFF2A2A2A)    // visible rim where FAB overlaps sheet
    val navFill = Color(0xFF1B1B1B)          // nav container fill over black
    val navTopHighlight = Color(0xFF7F7F7F)  // inner top edge white 38%
    val navBottomHighlight = Color(0xFF121212) // inner bottom edge white 7%
    val navGlow = Color(0xFFFD3F00).copy(alpha = 0.45f) // orange glow alpha .45
    val tabInactive = Color(0xFFBDBDBD)      // inactive tab label
    val toggleOnFill get() = orange          // toggle on = orange fill
    val toggleOnKnob = black                 // toggle on = black knob
    val toggleOffFill = Color(0xFF1A1A1A)    // toggle off fill
    val toggleOffStroke = Color(0xFF1F1F1F)  // toggle off stroke
    val toggleOffKnob = Color(0xFF555555)    // toggle off knob
    val trueBlackBackground = Color(0xFF000000) // TRUE BLACK on
    val falseBlackBackground = Color(0xFF141414) // TRUE BLACK off
    val backgroundTrueBlackOff = falseBlackBackground // alias for chrome.md "backgroundTrueBlackOff"

    // ===== EXISTING TOKENS (kept for compatibility) =====

    // Main palette from SPEC.md
    val Background = black
    val BackgroundTrueBlack = trueBlackBackground
    val Card = wellFill
    val NearBlackRow = wellFill
    val NearBlackRowStroke = wellStroke
    val InsetGreyGridLines = Color(0xFF454D53)
    val InsetGreyLabels = Color(0xFF3D454A)
    val PrimaryText = textPrimary
    val MutedText = textMuted
    val TagButtonFill = Color(0xFF2A2A2A)

    // Activity screen colors (from SVG export spec)
    val ActivityNow = Color(0xFFE23700)
    val ActivityStatLabel = Color(0xFFCB3100)
    val ActivityChipFill = iconWellFill
    val ActivityChipBorder = iconWellStroke
    val ActivityShowMoreFill = Color(0xFF1A1A1A)
    val ActivityShowMoreStroke = Color(0xFF080808)
    val ActivityShowMoreText = Color(0xFFBDBDBD)
    val ActivityAxisLine = Color(0xFF8A939A)
    val ActivityLabelBox = panelGrey
    val ActivityLabelText = Color(0xFFAAB2B8)
    val ActivityTooltipStroke = Color(0xFF2B3034)
    val ActivityTooltipText = Color(0xFF111111)
    val ActivityLeaderLine = Color(0xFF22272A)
    val ActivityEndDot = Color(0xFF0B0B0B)
    val ActivityRulerTick = Color(0xFF3E454A)
    val ActivityRulerNumberActive = Color(0xFF0B0B0B)
    val ActivityRulerNumberInactive = Color(0xFF7F888F)
    val ActivityDateText = Color(0xFF1C2023)
    val ActivityIconWell = iconWellFill
    val ActivityMutedIcon = Color(0xFF757577)

    // Tab accents (black text on accent fills) - DEPRECATED: use orange only
    val AccentTDY get() = orange
    val AccentMLS get() = orange
    val AccentLFT get() = orange
    val AccentCRD get() = orange
    val AccentWKS get() = orange
    val AccentSET get() = orange

    // Reference-measured colors (from vital-ui tokens.css)
    val RefOrange get() = orange
    val RefOrangeChip = chipOnOrange
    val RefInk = Color(0xFF070707)
    val RefInkDeep = black
    val RefPillGray = Color(0xFF5E6870)
    val RefLabelGray = Color(0xFF8A8A8A)
    val RefRingLine = Color(0xFF232323)
    val RefWhite = Color(0xFFF4F4F4)

    // Inset Grey Panel (Activity.png measured) — FLAT #5F6971, NOT gradient
    val InsetGreyPanel = panelGrey
    val InsetGreyPanelShadow = panelGreyShadow
    val InsetGreyGuideLine = Color(0xFF4A535A)
    val InsetGreyBottomLabels = Color(0xFF3D454A)

    // Capsule row (Activity.png measured)
    val CapsuleFill = wellFill
    val CapsuleDarkRing = Color(0xFF030303)
    val CapsuleRimLine = Color(0xFF1A1A1A)

    // Dial face gradient stops (from dial.js radialGradient #dialFace)
    val DialFaceStop0 = Color(0xFF070707)
    val DialFaceStop1 = Color(0xFF080808)
    val DialFaceStop2 = Color(0xFF0B0B0B)
    val DialFaceStop3 = Color(0xFF050505)
    val DialFaceStop4 = black

    // Inner dial gradient stops (from dial.js radialGradient #innerFace)
    val InnerFaceStop0 = Color(0xFF030303)
    val InnerFaceStop1 = Color(0xFF050505)
    val InnerFaceStop2 = Color(0xFF0D0D0D)
    val InnerFaceStop3 = Color(0xFF0B0B0B)

    // Inner dial black rim band
    val InnerDialRim = black

    // Knob colors (from knob.js)
    val KnobOrange get() = orange
    val KnobInk = Color(0xFF070707)

    // Icon colors
    val FabIcon get() = orange
    val ActivityFanIcon get() = orange

    // OrangeDial (Meals / Lift / Cardio hero art) from chrome.md + brief section 3
    val dialOuterStroke = Color(0xFF1A1A1A)   // outer disc 1.5 du stroke
    val dialThinRing = Color(0xFF141414)      // radius 100 thin ring
    val dialInnerFill = Color(0xFF0A0A0A)     // inner disc fill
    val dialInnerStroke = Color(0xFF222222)   // inner disc 3 du stroke
    val dialInnerDashedRing = Color(0xFF2B2B2B) // radius 56 dashed ring

    // Dial components (updated to match reference)
    val DialDarkDisk = Color(0xFF141414)
    val DialTickRing = tickGrey
    val DialInnerRing = Color(0xFF1A1A1A)
    val DialOuterRimHighlight = Color(0xFF2A2A2A)
    val DialKnobOutline = Color.Black
    val DialKnobCenterDot = Color.Black
    val DialKnobHand = Color.Black
    val DialKnobBevelLight = Color(0xFF3D3D3D)
    val DialKnobBevelDark = Color.Black

    // Macro colors (used on dial arcs)
    val MacroProtein = Color(0xFFFF6B35)
    val MacroCarbs = Color(0xFF4ECDC4)
    val MacroFat = Color(0xFFFFE66D)

    // Semantic colors for Material3 color scheme
    val Surface = wellFill
    val SurfaceVariant = wellFill
    val Outline = wellStroke
    val OnSurface = textPrimary
    val OnSurfaceVariant = textMuted

    fun getTabAccent(tabIndex: Int): Color = orange

    fun getTabAccentLabel(tabIndex: Int): String = when (tabIndex) {
        0 -> "TDY"
        1 -> "MLS"
        2 -> "LFT"
        3 -> "CRD"
        4 -> "WKS"
        5 -> "SET"
        else -> "TDY"
    }
}
