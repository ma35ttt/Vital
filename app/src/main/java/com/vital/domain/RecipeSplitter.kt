package com.vital.domain

/** Totals for a whole recipe, before splitting into servings. */
data class RecipeTotals(
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

/**
 * Splits a recipe into [servings] equal portions. Each field is rounded to 0.1 and the
 * rounding remainder is spread over the first portions, so the portions always add back
 * to the recipe totals.
 */
fun splitRecipe(recipe: RecipeTotals, servings: Int): List<RecipeTotals> {
    require(servings > 0) { "servings must be at least 1" }

    fun split(value: Double): List<Double> {
        val tenths = kotlin.math.round(value * 10.0).toInt()
        val base = tenths / servings
        val remainder = tenths % servings
        return (0 until servings).map { index ->
            (base + if (index < remainder) 1 else 0) / 10.0
        }
    }

    val kcal = split(recipe.kcal)
    val protein = split(recipe.proteinG)
    val carbs = split(recipe.carbsG)
    val fat = split(recipe.fatG)

    return (0 until servings).map { index ->
        RecipeTotals(
            kcal = kcal[index],
            proteinG = protein[index],
            carbsG = carbs[index],
            fatG = fat[index]
        )
    }
}

fun perServing(recipe: RecipeTotals, servings: Int): RecipeTotals =
    splitRecipe(recipe, servings).first()
