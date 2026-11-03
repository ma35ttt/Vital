package com.vital.domain

/**
 * ACSM walking equation, valid at or below 6 km/h:
 *   VO2 (ml/kg/min) = 3.5 + 0.1 * speed(m/min) + 1.8 * speed(m/min) * grade
 *   kcal/min = VO2 * weight(kg) / 200
 * 6 km/h, 2%, 68 kg for 32:10 gives 187 kcal.
 */
object Acsm {
    const val MAX_SPEED_KMH = 6.0

    fun kcal(
        speedKmh: Double,
        inclinePercent: Double,
        weightKg: Double,
        durationSeconds: Int
    ): Double? {
        if (speedKmh <= 0.0 || weightKg <= 0.0 || durationSeconds <= 0) return null
        if (speedKmh > MAX_SPEED_KMH + 1e-9) return null

        val speedMetresPerMinute = speedKmh * 1000.0 / 60.0
        val grade = inclinePercent / 100.0
        val vo2 = 3.5 + 0.1 * speedMetresPerMinute + 1.8 * speedMetresPerMinute * grade
        val kcalPerMinute = vo2 * weightKg / 200.0
        return kcalPerMinute * durationSeconds / 60.0
    }
}
