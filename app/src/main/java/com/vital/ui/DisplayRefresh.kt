package com.vital.ui

import android.app.Activity
import android.os.Build
import android.view.Display

/**
 * Applies the "high refresh rate" preference to an activity's window.
 *
 * Android has no API that forces 120Hz: the panel and the system's refresh-rate policy
 * decide. What an app can do is ask for the fastest display mode the panel offers and tell
 * the surface what frame rate its content is drawn at. Together those are enough for the
 * system to hold the high rate while Vital is on screen. Both requests are best-effort and
 * are ignored when the user has capped the display at 60Hz in system settings.
 */
object DisplayRefresh {

    /** Asks for the panel's fastest mode when [enabled], or the system default when not. */
    fun apply(activity: Activity, enabled: Boolean) {
        val window = activity.window
        val display = displayOf(activity) ?: return

        val attrs = window.attributes
        if (enabled) {
            val best = bestMode(display)
            if (best != null) {
                attrs.preferredDisplayModeId = best.modeId
                attrs.preferredRefreshRate = best.refreshRate
            }
        } else {
            // 0 = no preference; hand the choice back to the system.
            attrs.preferredDisplayModeId = 0
            attrs.preferredRefreshRate = 0f
        }
        window.attributes = attrs

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val target = if (enabled) bestMode(display)?.refreshRate ?: 0f else 0f
            window.decorView.setRequestedFrameRate(target)
        }
    }

    private fun displayOf(activity: Activity): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay
        }
    }

    /**
     * The fastest mode that keeps the current resolution, so asking for 120Hz never drops
     * the panel to a smaller picture; falls back to the fastest mode of any size.
     */
    private fun bestMode(display: Display): Display.Mode? {
        val current = display.mode
        val modes = display.supportedModes
        val sameResolution = modes.filter {
            it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight
        }
        return (sameResolution.ifEmpty { modes.toList() }).maxByOrNull { it.refreshRate }
    }
}
