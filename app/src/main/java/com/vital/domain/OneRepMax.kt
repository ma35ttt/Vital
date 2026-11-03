package com.vital.domain

/** Estimated one-rep max. Epley is the formula used for PR checks and lift history. */
object OneRepMax {
    fun epley(weightKg: Double, reps: Int): Double {
        require(reps >= 1) { "reps must be at least 1" }
        return weightKg * (1.0 + reps / 30.0)
    }
}
