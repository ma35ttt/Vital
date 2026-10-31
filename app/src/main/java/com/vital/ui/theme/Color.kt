package com.vital.ui.theme

import androidx.compose.ui.graphics.Color

object VitalColors {

    // Main palette from SPEC.md
    val Background = Color(0xFF141414)
    val BackgroundTrueBlack = Color(0xFF000000)
    val Card = Color(0xFF1E1E1E)
    val NearBlackRow = Color(0xFF0B0B0B)
    val NearBlackRowStroke = Color(0xFF2A2A2A)
    val InsetGreyPanel = Color(0xFF6B7780)
    val InsetGreyGridLines = Color(0xFF454D53)
    val InsetGreyLabels = Color(0xFF3D454A)
    val PrimaryText = Color(0xFFF2F2F2)
    val MutedText = Color(0xFF8A8A8A)
    val TagButtonFill = Color(0xFF2A2A2A)

    // Tab accents (black text on accent fills)
    val AccentTDY = Color(0xFFFF4D00) // Safety Orange
    val AccentMLS = Color(0xFFC6FF00) // Acid Lime
    val AccentLFT = Color(0xFF3D7BFF) // Electric Blue
    val AccentCRD = Color(0xFF00E0C6) // Teal
    val AccentWKS = Color(0xFFFFC400) // Amber
    val AccentSET = Color(0xFFE6E6E6) // Light Grey

    // Reference-measured colors (from vital-ui tokens.css)
    val RefOrange = Color(0xFFFD3F00)       // --orange: screen background, knob, pills, icons
    val RefOrangeChip = Color(0xFFD93400)   // --orange-chip: SUN / APR.9 chips
    val RefInk = Color(0xFF070707)          // --ink: dial, text on orange, FAB
    val RefInkDeep = Color(0xFF000000)      // --ink-deep: rim ring around the inner dial
    val RefPillGray = Color(0xFF5E6870)     // --pill-gray: 09:24 pill centre
    val RefLabelGray = Color(0xFF8A8A8A)    // --label-gray: STEPS
    val RefRingLine = Color(0xFF232323)     // --ring-line: hairline on the inner dial
    val RefWhite = Color(0xFFF4F4F4)        // --white

    // Dial face gradient stops (from dial.js radialGradient #dialFace)
    val DialFaceStop0 = Color(0xFF070707)   // 0%
    val DialFaceStop1 = Color(0xFF080808)   // 86%
    val DialFaceStop2 = Color(0xFF0B0B0B)   // 93%
    val DialFaceStop3 = Color(0xFF050505)   // 97.5%
    val DialFaceStop4 = Color(0xFF000000)   // 100%

    // Inner dial gradient stops (from dial.js radialGradient #innerFace)
    val InnerFaceStop0 = Color(0xFF030303)  // 0%
    val InnerFaceStop1 = Color(0xFF050505)  // 72%
    val InnerFaceStop2 = Color(0xFF0D0D0D)  // 93%
    val InnerFaceStop3 = Color(0xFF0B0B0B)  // 100%

    // Inner dial black rim band
    val InnerDialRim = Color(0xFF000000)    // Black rim band

    // Knob colors (from knob.js)
    val KnobOrange = Color(0xFFFD3F00)      // TEAL in knob.js but actually orange
    val KnobInk = Color(0xFF070707)         // Black ink for needle, pin, selector

    // Dial components (updated to match reference)
    val DialDarkDisk = Color(0xFF141414)
    val DialTickRing = Color(0xFF3D3D3D)
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
    val Surface = Card
    val SurfaceVariant = NearBlackRow
    val Outline = NearBlackRowStroke
    val OnSurface = PrimaryText
    val OnSurfaceVariant = MutedText

    fun getTabAccent(tabIndex: Int): Color = when (tabIndex) {
        0 -> AccentTDY
        1 -> AccentMLS
        2 -> AccentLFT
        3 -> AccentCRD
        4 -> AccentWKS
        5 -> AccentSET
        else -> AccentTDY
    }

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