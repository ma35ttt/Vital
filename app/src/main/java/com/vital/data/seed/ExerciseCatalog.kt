package com.vital.data.seed

import com.vital.data.entity.Equipment
import com.vital.data.entity.Exercise
import com.vital.data.entity.MuscleGroup
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One row of the bundled exercise catalog (see `assets/exercises.json`). */
@Serializable
data class ExerciseSeed(
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment = Equipment.NONE,
    val isBarbell: Boolean = false,
    val instructions: String? = null,
    val defaultRestSec: Int = 90,
    val isCompound: Boolean = false
)

@Serializable
private data class ExerciseCatalogFile(
    val version: Int = 1,
    val exercises: List<ExerciseSeed> = emptyList()
)

/**
 * Parses the shipped exercise catalog into [Exercise] rows.
 *
 * Kept free of Android types so the mapping is unit-tested on the JVM; the asset is read and
 * handed in as text. Ids are derived from the name (`seed:<slug>`) so re-seeding is stable
 * instead of minting a new UUID every launch; a name collision gets a numeric suffix.
 */
object ExerciseCatalog {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(catalogJson: String): List<Exercise> {
        val file = json.decodeFromString<ExerciseCatalogFile>(catalogJson)
        val usedIds = HashSet<String>()
        return file.exercises.map { seed ->
            val base = "seed:" + seed.name.toSeedSlug()
            var id = base
            var suffix = 2
            while (!usedIds.add(id)) {
                id = "$base-$suffix"
                suffix++
            }
            Exercise(
                id = id,
                name = seed.name,
                muscleGroup = seed.muscleGroup,
                isBarbell = seed.isBarbell,
                equipment = seed.equipment,
                instructions = seed.instructions,
                defaultRestSec = seed.defaultRestSec,
                isCompound = seed.isCompound
            )
        }
    }
}
