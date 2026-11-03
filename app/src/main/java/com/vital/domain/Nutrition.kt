package com.vital.domain

/** Macronutrients in grams. 4 kcal per gram of protein/carbs, 9 kcal per gram of fat. */
data class Macros(
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0
) {
    val kcal: Double get() = proteinG * 4.0 + carbsG * 4.0 + fatG * 9.0

    operator fun plus(other: Macros): Macros = Macros(
        proteinG = proteinG + other.proteinG,
        carbsG = carbsG + other.carbsG,
        fatG = fatG + other.fatG
    )
}

/** Percent of total macro calories, each 0..100. */
data class MacroShare(
    val proteinPct: Double,
    val carbsPct: Double,
    val fatPct: Double
)

fun totalKcal(entries: Iterable<Macros>): Double = entries.sumOf { it.kcal }

fun totalMacros(entries: Iterable<Macros>): Macros =
    entries.fold(Macros()) { acc, item -> acc + item }

fun macroShare(macros: Macros): MacroShare {
    val total = macros.kcal
    if (total <= 0.0) return MacroShare(0.0, 0.0, 0.0)
    return MacroShare(
        proteinPct = macros.proteinG * 4.0 / total * 100.0,
        carbsPct = macros.carbsG * 4.0 / total * 100.0,
        fatPct = macros.fatG * 9.0 / total * 100.0
    )
}
