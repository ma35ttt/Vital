package com.vital.ui

import com.vital.ai.TreadmillReading
import java.util.Locale

/**
 * Maps a photo reading onto the four Run/Walk inputs. Only values the model actually saw are
 * returned, so a partly visible console still fills what it can and leaves the rest typed.
 */
internal fun treadmillDraftValues(reading: TreadmillReading): Map<CardioField, String> = buildMap {
    reading.durationSec?.let { put(CardioField.DURATION, formatRestTime(it)) }
    reading.distanceKm?.let { put(CardioField.DISTANCE_KM, trimDecimal(it, 2)) }
    reading.speedKmh?.let { put(CardioField.SPEED, trimDecimal(it, 1)) }
    reading.inclinePct?.let { put(CardioField.INCLINE, trimDecimal(it, 1)) }
}

/** `6.0` -> `6`, `3.20` -> `3.2`, `3.22` -> `3.22`. */
internal fun trimDecimal(value: Double, decimals: Int): String =
    String.format(Locale.US, "%.${decimals}f", value).trimEnd('0').trimEnd('.')