package com.vital.ui.theme

import androidx.compose.ui.graphics.Color

object VitalColors {

    // ===== DESIGN TOKENS (from chrome.md, REMAINING_TABS_BRIEF.md) =====
    // One accent, orange #FD3F00, plus black and #5F6971. No per-tab accents.

    val orange = Color(0xFFFD3F00)           // hero fill, active states, accents
    val black = Color(0xFF000000)            // screen background, sheets, text on orange
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
    val chartBarOnGrey = Color(0xFF171A1D)   // FIX: 3.1:1 on #5F6971 (mock #2B2F33 was 2.4:1)
    val sheetTopHighlight = Color(0xFF1C1C1C) // 1 du line at y 404
    val sheetHighlight = sheetTopHighlight    // alias for chrome.md "sheetHighlight"
    val fabRimStroke = Color(0xFF1F1F1F)     // FAB stroke
    val fabRimVisible = Color(0xFF2A2A2A)    // visible rim where FAB overlaps sheet
    val navFill = Color(0xFF1B1B1B)          // nav container fill over black
    val navTopHighlight = Color(0xFF7F7F7F)  // inner top edge white 38%
    val navBottomHighlight = Color(0xFF121212) // inner bottom edge white 7%
    val navGlow = Color(0xFFFD3F00).copy(alpha = 0.45f) // orange glow alpha .45
    val tabInactive = Color(0xFFBDBDBD)      // inactive tab label
    val toggleOnFill = orange                // toggle on = orange fill
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
    val AccentTDY = orange
    val AccentMLS = orange
    val AccentLFT = orange
    val AccentCRD = orange
    val AccentWKS = orange
    val AccentSET = orange

    // Reference-measured colors (from vital-ui tokens.css)
    val RefOrange = orange
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
    val KnobOrange = orange
    val KnobInk = Color(0xFF070707)

    // Icon colors
    val FabIcon = orange
    val ActivityFanIcon = orange

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