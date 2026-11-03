package com.vital.domain

/**
 * Local smart-text parser. It only uses the bundled food table, so it works offline.
 *
 * Supported shapes:
 *   "chicken 150g"                         -> chicken, 150 g, RAW/COOKED ambiguity
 *   "rice 150g cooked"                     -> cooked rice, 150 g
 *   "2 eggs"                               -> 2 eggs (100 g)
 *   "chicken breast cooked 200g rice 150g" -> two items, rice still ambiguous
 *
 * A food with both raw and cooked entries and no explicit preparation returns an
 * [Ambiguity]; the item uses the raw entry until the user answers the question.
 */
enum class Preparation { RAW, COOKED }

data class FoodMacros(
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double
)

data class Food(
    val name: String,
    val aliases: List<String>,
    val raw: FoodMacros? = null,
    val cooked: FoodMacros? = null,
    val gramsPerUnit: Double? = null,
    val unitNames: List<String> = emptyList()
)

data class ParsedItem(
    val name: String,
    val grams: Double,
    val quantity: Double?,
    val unit: String?,
    val preparation: Preparation?,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

data class Ambiguity(
    val foodName: String,
    val grams: Double,
    val question: String
)

data class ParseResult(
    val items: List<ParsedItem>,
    val ambiguities: List<Ambiguity>
) {
    val kcal: Double get() = items.sumOf { it.kcal }
    val proteinG: Double get() = items.sumOf { it.proteinG }
    val carbsG: Double get() = items.sumOf { it.carbsG }
    val fatG: Double get() = items.sumOf { it.fatG }
    val macros: Macros get() = Macros(proteinG, carbsG, fatG)
}

object FoodDatabase {
    val default: List<Food> = listOf(
        Food(
            name = "chicken breast",
            aliases = listOf("chicken breast", "chicken"),
            raw = FoodMacros(120.0, 22.5, 0.0, 2.6),
            cooked = FoodMacros(165.0, 31.0, 0.0, 3.6)
        ),
        Food(
            name = "rice",
            aliases = listOf("white rice", "rice"),
            raw = FoodMacros(360.0, 6.6, 79.0, 0.6),
            cooked = FoodMacros(130.0, 2.7, 28.0, 0.3)
        ),
        Food(
            name = "egg",
            aliases = listOf("eggs", "egg"),
            raw = FoodMacros(143.0, 12.6, 0.7, 9.5),
            gramsPerUnit = 50.0,
            unitNames = listOf("eggs", "egg")
        ),
        Food(
            name = "banana",
            aliases = listOf("bananas", "banana"),
            raw = FoodMacros(89.0, 1.1, 22.8, 0.3),
            gramsPerUnit = 118.0,
            unitNames = listOf("bananas", "banana")
        ),
        Food(
            name = "oats",
            aliases = listOf("oatmeal", "oats"),
            raw = FoodMacros(389.0, 16.9, 66.3, 6.9)
        )
    )
}

private val TOKEN_REGEX = Regex("[a-z]+|\\d+(?:\\.\\d+)?")
private val COOKED_WORDS = setOf("cooked", "grilled", "boiled", "fried", "baked")
private const val RAW_WORD = "raw"

private fun String.isNumber(): Boolean = toDoubleOrNull() != null

private data class FoodMatch(
    val start: Int,
    val end: Int,
    val food: Food
)

private data class Quantity(
    val grams: Double,
    val quantity: Double?,
    val unit: String?,
    val used: Set<Int> = emptySet()
)

private data class PreparationHit(
    val preparation: Preparation?,
    val used: Set<Int> = emptySet()
)

fun parseSmartText(
    input: String,
    foods: List<Food> = FoodDatabase.default
): ParseResult {
    val normalized = input.lowercase()
    val tokens = TOKEN_REGEX.findAll(normalized).map { it.value }.toList()
    if (tokens.isEmpty()) return ParseResult(emptyList(), emptyList())

    val matches = mutableListOf<FoodMatch>()
    for (food in foods) {
        for (alias in food.aliases) {
            val aliasTokens = TOKEN_REGEX.findAll(alias.lowercase()).map { it.value }.toList()
            if (aliasTokens.isEmpty()) continue
            var index = 0
            while (index <= tokens.size - aliasTokens.size) {
                if (tokens.subList(index, index + aliasTokens.size) == aliasTokens) {
                    matches += FoodMatch(index, index + aliasTokens.size - 1, food)
                }
                index++
            }
        }
    }

    // Prefer the longest alias at the same start ("chicken breast" over "chicken").
    val accepted = mutableListOf<FoodMatch>()
    for (match in matches.sortedWith(compareBy<FoodMatch> { it.start }.thenByDescending { it.end - it.start })) {
        if (accepted.none { it.start <= match.end && match.start <= it.end }) {
            accepted += match
        }
    }

    val consumed = BooleanArray(tokens.size)
    val items = mutableListOf<ParsedItem>()
    val ambiguities = mutableListOf<Ambiguity>()

    for (match in accepted.sortedBy { it.start }) {
        val beforeIndices = (maxOf(0, match.start - 3) until match.start)
            .filter { !consumed[it] }
        val afterIndices = (match.end + 1 until minOf(tokens.size, match.end + 5))
            .filter { !consumed[it] }
        val before = beforeIndices.map { tokens[it] }
        val after = afterIndices.map { tokens[it] }

        val prep = parsePreparation(beforeIndices, afterIndices, tokens)
        val quantity = parseQuantity(beforeIndices, afterIndices, tokens, match.food)

        val entry = when (prep.preparation) {
            Preparation.RAW -> match.food.raw
            Preparation.COOKED -> match.food.cooked
            null -> match.food.raw ?: match.food.cooked
        } ?: continue

        val factor = quantity.grams / 100.0
        items += ParsedItem(
            name = match.food.name,
            grams = quantity.grams,
            quantity = quantity.quantity,
            unit = quantity.unit,
            preparation = prep.preparation,
            kcal = entry.kcalPer100g * factor,
            proteinG = entry.proteinPer100g * factor,
            carbsG = entry.carbsPer100g * factor,
            fatG = entry.fatPer100g * factor
        )

        for (index in match.start..match.end) consumed[index] = true
        quantity.used.forEach { consumed[it] = true }
        prep.used.forEach { consumed[it] = true }

        if (prep.preparation == null && match.food.raw != null && match.food.cooked != null) {
            ambiguities += Ambiguity(
                foodName = match.food.name,
                grams = quantity.grams,
                question = "${match.food.name.uppercase()} ${quantity.grams.toInt()}G · RAW OR COOKED?"
            )
        }
    }

    return ParseResult(items, ambiguities)
}

private fun parsePreparation(
    beforeIndices: List<Int>,
    afterIndices: List<Int>,
    tokens: List<String>
): PreparationHit {
    for (index in beforeIndices) {
        if (tokens[index] == RAW_WORD) return PreparationHit(Preparation.RAW, setOf(index))
    }
    for (index in afterIndices) {
        if (tokens[index] == RAW_WORD) return PreparationHit(Preparation.RAW, setOf(index))
    }
    for (index in beforeIndices) {
        if (tokens[index] in COOKED_WORDS) return PreparationHit(Preparation.COOKED, setOf(index))
    }
    for (index in afterIndices) {
        if (tokens[index] in COOKED_WORDS) return PreparationHit(Preparation.COOKED, setOf(index))
    }
    return PreparationHit(null)
}

private fun parseQuantity(
    beforeIndices: List<Int>,
    afterIndices: List<Int>,
    tokens: List<String>,
    food: Food
): Quantity {
    // Quantity after the food: "chicken 150g", "chicken cooked 200g".
    val afterNumberPos = afterIndices.take(3).indexOfFirst { tokens[it].isNumber() }
    if (afterNumberPos >= 0) {
        val numberIndex = afterIndices[afterNumberPos]
        val unitIndex = afterIndices.getOrNull(afterNumberPos + 1)
        val unit = unitIndex?.let { tokens[it] }
        val quantity = toQuantity(tokens[numberIndex].toDouble(), unit, food)
        val used = mutableSetOf(numberIndex)
        if (unitIndex != null && unit != null && isUnitToken(unit, food)) used += unitIndex
        return quantity.copy(used = used)
    }

    // Quantity before the food: "150g chicken", "2 eggs".
    val beforeNumberPos = beforeIndices.takeLast(3).indexOfFirst { tokens[it].isNumber() }
    if (beforeNumberPos >= 0) {
        val numberIndex = beforeIndices.takeLast(3)[beforeNumberPos]
        val unitIndex = beforeIndices.takeLast(3).getOrNull(beforeNumberPos + 1)
        val unit = unitIndex?.let { tokens[it] }
        val quantity = toQuantity(tokens[numberIndex].toDouble(), unit, food)
        val used = mutableSetOf(numberIndex)
        if (unitIndex != null && unit != null && isUnitToken(unit, food)) used += unitIndex
        return quantity.copy(used = used)
    }

    // No quantity: one unit when the food has one, otherwise 100 g.
    val gramsPerUnit = food.gramsPerUnit
    return if (gramsPerUnit != null) {
        Quantity(gramsPerUnit, 1.0, food.unitNames.firstOrNull())
    } else {
        Quantity(100.0, null, null)
    }
}

private fun isUnitToken(token: String, food: Food): Boolean =
    token in setOf("g", "kg", "ml", "l") || token in food.unitNames

private fun toQuantity(value: Double, unit: String?, food: Food): Quantity = when (unit) {
    "kg" -> Quantity(value * 1000.0, value, unit)
    "l" -> Quantity(value * 1000.0, value, unit)
    "g", "ml" -> Quantity(value, value, unit)
    else -> {
        val gramsPerUnit = food.gramsPerUnit
        if (gramsPerUnit != null) {
            Quantity(value * gramsPerUnit, value, unit ?: food.unitNames.firstOrNull())
        } else {
            Quantity(value, value, unit)
        }
    }
}
