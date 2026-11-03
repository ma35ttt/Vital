package com.vital.domain

import kotlin.math.abs

/**
 * Barbell plate maths. Weights are kilograms, plates are per side.
 * The default inventory is the usual gym set: 25, 20, 15, 10, 5, 2.5, 1.25.
 */
data class PlateLoad(
    val totalKg: Double,
    val perSideKg: List<Double>
)

object PlateCalculator {
    const val DEFAULT_BAR_KG = 20.0
    val DEFAULT_PLATES = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    private const val EPS = 1e-6
    private const val STEP_KG = 0.25

    /** Exact load for [targetKg], or null when the target is not loadable with [plates]. */
    fun forTarget(
        targetKg: Double,
        barKg: Double = DEFAULT_BAR_KG,
        plates: List<Double> = DEFAULT_PLATES
    ): PlateLoad? {
        if (targetKg + EPS < barKg) return null
        val perSide = (targetKg - barKg) / 2.0
        val load = greedy(perSide, plates) ?: return null
        return PlateLoad(totalKg = barKg + load.sum() * 2.0, perSideKg = load)
    }

    /**
     * Closest loadable weight to [targetKg]. Searches 0.25 kg steps within 10 kg of the target,
     * so 83 kg resolves to 82.5 kg rather than 85 kg.
     */
    fun nearestLoadable(
        targetKg: Double,
        barKg: Double = DEFAULT_BAR_KG,
        plates: List<Double> = DEFAULT_PLATES
    ): PlateLoad? {
        var best: PlateLoad? = null
        var bestDelta = Double.MAX_VALUE
        val start = maxOf(barKg, targetKg - 10.0)
        val end = targetKg + 10.0
        var candidate = start
        while (candidate <= end + EPS) {
            val load = forTarget(candidate, barKg, plates)
            if (load != null) {
                val delta = abs(load.totalKg - targetKg)
                if (delta < bestDelta - EPS) {
                    best = load
                    bestDelta = delta
                }
            }
            candidate += STEP_KG
        }
        return best
    }

    private fun greedy(perSideKg: Double, plates: List<Double>): List<Double>? {
        if (perSideKg < -EPS) return null
        var remaining = perSideKg
        val used = mutableListOf<Double>()
        for (plate in plates.sortedDescending()) {
            while (remaining + EPS >= plate) {
                used += plate
                remaining -= plate
            }
        }
        return if (abs(remaining) <= EPS) used else null
    }
}
