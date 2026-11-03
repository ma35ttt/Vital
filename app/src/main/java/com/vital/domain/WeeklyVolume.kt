package com.vital.domain

/** A completed set, used for weekly volume. */
data class VolumeSet(
    val weightKg: Double,
    val reps: Int,
    val weekIndex: Int = 0
)

fun weeklyVolume(sets: Iterable<VolumeSet>): Double =
    sets.sumOf { it.weightKg * it.reps }

fun volumeByWeek(sets: Iterable<VolumeSet>): Map<Int, Double> =
    sets.groupBy { it.weekIndex }
        .mapValues { (_, week) -> weeklyVolume(week) }
