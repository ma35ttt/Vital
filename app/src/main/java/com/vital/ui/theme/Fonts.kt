package com.vital.ui.theme

import com.vital.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object VitalFonts {

    // JetBrains Mono - for clocks, numbers, tags, labels
    val JetBrainsMonoRegular = FontFamily(Font(R.font.jetbrainsmono_regular, FontWeight.Normal))
    val JetBrainsMonoLight = FontFamily(Font(R.font.jetbrainsmono_light, FontWeight.Light))
    val JetBrainsMonoMedium = FontFamily(Font(R.font.jetbrainsmono_medium, FontWeight.Medium))
    val JetBrainsMonoSemiBold = FontFamily(Font(R.font.jetbrainsmono_semibold, FontWeight.SemiBold))
    val JetBrainsMonoBold = FontFamily(Font(R.font.jetbrainsmono_bold, FontWeight.Bold))

    // Inter Tight - headlines and big values (design system)
    val InterTight = FontFamily(
        Font(R.font.inter_tight_400, FontWeight.Normal),
        Font(R.font.inter_tight_500, FontWeight.Medium),
        Font(R.font.inter_tight_600, FontWeight.SemiBold),
        Font(R.font.inter_tight_700, FontWeight.Bold)
    )

    // Space Grotesk - for headlines and big values (variable font with wght axis)
    val SpaceGroteskRegular = FontFamily(Font(R.font.spacegrotesk_regular, FontWeight.Normal))
    val SpaceGroteskMedium = FontFamily(Font(R.font.spacegrotesk_medium, FontWeight.Medium))
    val SpaceGroteskSemiBold = FontFamily(Font(R.font.spacegrotesk_semibold, FontWeight.SemiBold))
    val SpaceGroteskBold = FontFamily(Font(R.font.spacegrotesk_bold, FontWeight.Bold))
    val SpaceGroteskLight = FontFamily(Font(R.font.spacegrotesk_light, FontWeight.Light))

    // Default fallbacks
    val DefaultMono = FontFamily.Default
    val DefaultSans = FontFamily.Default
}