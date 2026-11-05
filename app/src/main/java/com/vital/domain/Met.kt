package com.vital.domain

import com.vital.data.entity.CardioType
import com.vital.data.entity.Intensity
import com.vital.data.entity.StrokeType

/**
 * MET (metabolic equivalent) calorie estimate for the activities ACSM's walking/running
 * equations do not cover.
 *
 *   kcal/min = MET * 3.5 (ml O2/kg/min) * weight(kg) / 200
 *
 * Values are the mid-range entries from the Compendium of Physical Activities; the swim
 * stroke and the Other effort picker select between them.
 */
object Met {

    /** MET for [type], or null for walking/running (which use ACSM instead). */
    fun value(
        type: CardioType,
        intensity: Intensity? = null,
        stroke: StrokeType? = null
    ): Double? = when (type) {
        CardioType.CYCLING -> 7.0
        CardioType.ROWING -> 7.0
        CardioType.ELLIPTICAL -> 5.0
        CardioType.STAIR_CLIMBER -> 9.0
        CardioType.SWIMMING -> when (stroke ?: StrokeType.FREESTYLE) {
            StrokeType.FREESTYLE -> 6.0
            StrokeType.BREASTSTROKE -> 5.3
            StrokeType.BACKSTROKE -> 4.8
            StrokeType.BUTTERFLY -> 10.0
            StrokeType.MIXED -> 6.0
        }
        CardioType.OTHER -> when (intensity ?: Intensity.MODERATE) {
            Intensity.LIGHT -> 3.0
            Intensity.MODERATE -> 5.0
            Intensity.VIGOROUS -> 8.0
        }
        CardioType.RUNNING, CardioType.WALKING -> null
    }

    fun kcal(
        type: CardioType,
        weightKg: Double,
        durationSeconds: Int,
        intensity: Intensity? = null,
        stroke: StrokeType? = null
    ): Double? {
        if (weightKg <= 0.0 || durationSeconds <= 0) return null
        val met = value(type, intensity, stroke) ?: return null
        return met * 3.5 * weightKg / 200.0 * (durationSeconds / 60.0)
    }
}
