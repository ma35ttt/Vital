package com.vital.data.seed

import com.vital.data.entity.Food
import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One row of the bundled Indian food catalog (see `assets/foods.json`). */
@Serializable
data class FoodSeed(
    val name: String,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val hindi: String? = null,
    val category: String? = null,
    val code: String? = null,
    val verified: Boolean = false,
    val source: String? = null
)

@Serializable
private data class FoodCatalogFile(
    val version: Int = 1,
    val foods: List<FoodSeed> = emptyList()
)

/**
 * Parses the shipped Indian food catalog into [Food] rows.
 *
 * The data is IFCT 2017 (NIN Hyderabad) plus Gopalan 2004, taken from the CC0-1.0
 * `prabhubng/indian-nutrition-data` set. Macros are per 100 g and the calorie value is the
 * one the source measured, kept as-is instead of being re-derived from 4/4/9 so search rows
 * and logged entries show the same number.
 *
 * Android-free so the mapping is unit-tested on the JVM. Ids are derived from the name
 * (`seed:<slug>`) so re-seeding is stable; a slug collision gets a numeric suffix. The Hindi
 * name and the source code are folded into [Food.referenceName] so they can be searched.
 */
object FoodCatalog {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(catalogJson: String): List<Food> {
        val file = json.decodeFromString<FoodCatalogFile>(catalogJson)
        val usedIds = HashSet<String>()
        return file.foods.map { seed ->
            val base = "seed:" + seed.name.toSeedSlug()
            var id = base
            var suffix = 2
            while (!usedIds.add(id)) {
                id = "$base-$suffix"
                suffix++
            }
            Food(
                id = id,
                name = seed.name,
                variant = FoodVariant.NONE,
                proteinPer100 = seed.protein,
                carbsPer100 = seed.carbs,
                fatPer100 = seed.fat,
                kcalPer100 = seed.kcal,
                source = FoodSource.SEED,
                verified = seed.verified,
                referenceName = referenceName(seed)
            )
        }
    }

    private fun referenceName(seed: FoodSeed): String? {
        val code = seed.code?.let { "${seed.source ?: "IFCT"} $it" }
        return listOfNotNull(seed.hindi, code).joinToString(" · ").ifBlank { null }
    }
}
