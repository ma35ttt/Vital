package com.vital.domain

/**
 * ACSM metabolic equations, speed/grade form:
 *
 *   Walking (<= 6 km/h): VO2 (ml/kg/min) = 3.5 + 0.1 * speed(m/min) + 1.8 * speed(m/min) * grade
 *   Running (>  6 km/h): VO2 (ml/kg/min) = 3.5 + 0.2 * speed(m/min) + 0.9 * speed(m/min) * grade
 *
 *   kcal/min = VO2 * weight(kg) / 200
 *
 * 6 km/h, 2%, 68 kg for 32:10 gives 187 kcal on the walking branch.
 *
 * The walking equation is only validated up to 6 km/h, so anything faster uses the running
 * equation instead of being refused or clamped.
 */
object Acsm {
    /** Boundary between the walking and running equations. */
    const val WALK_MAX_SPEED_KMH = 6.0

    fun kcal(
        speedKmh: Double,
        inclinePercent: Double,
        weightKg: Double,
        durationSeconds: Int
    ): Double? {
        if (speedKmh <= 0.0 || weightKg <= 0.0 || durationSeconds <= 0) return null

        val speedMetresPerMinute = speedKmh * 1000.0 / 60.0
        val grade = inclinePercent / 100.0
        val vo2 = if (speedKmh <= WALK_MAX_SPEED_KMH) {
            3.5 + 0.1 * speedMetresPerMinute + 1.8 * speedMetresPerMinute * grade
        } else {
            3.5 + 0.2 * speedMetresPerMinute + 0.9 * speedMetresPerMinute * grade
        }
        val kcalPerMinute = vo2 * weightKg / 200.0
        return kcalPerMinute * durationSeconds / 60.0
    }
}
