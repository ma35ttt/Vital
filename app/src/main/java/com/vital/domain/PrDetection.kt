package com.vital.domain

data class PrCheck(
    val isPr: Boolean,
    val estimated1RmKg: Double,
    val previousBestKg: Double?
)

/**
 * A set is a PR when its Epley estimated 1RM beats the best estimated 1RM in history.
 * History is (weightKg, reps) pairs for the same exercise.
 */
fun detectPr(
    newWeightKg: Double,
    newReps: Int,
    history: List<Pair<Double, Int>>
): PrCheck {
    val newEstimate = OneRepMax.epley(newWeightKg, newReps)
    val previousBest = history.maxOfOrNull { (weight, reps) ->
        OneRepMax.epley(weight, reps)
    }
    return PrCheck(
        isPr = previousBest == null || newEstimate > previousBest + 1e-9,
        estimated1RmKg = newEstimate,
        previousBestKg = previousBest
    )
}
