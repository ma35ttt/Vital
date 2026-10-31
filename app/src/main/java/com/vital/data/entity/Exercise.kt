package com.vital.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "exercises")
@Serializable
data class Exercise(
    @PrimaryKey
    val id: String, // UUID
    val name: String,
    val muscleGroup: MuscleGroup,
    val isBarbell: Boolean = false,
    val equipment: Equipment = Equipment.NONE,
    val instructions: String? = null,
    val defaultRestSec: Int = 90, // Default rest time in seconds
    val isCompound: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            name: String,
            muscleGroup: MuscleGroup,
            isBarbell: Boolean = false,
            equipment: Equipment = Equipment.NONE,
            instructions: String? = null,
            defaultRestSec: Int = 90,
            isCompound: Boolean = false
        ): Exercise {
            return Exercise(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                muscleGroup = muscleGroup,
                isBarbell = isBarbell,
                equipment = equipment,
                instructions = instructions,
                defaultRestSec = defaultRestSec,
                isCompound = isCompound
            )
        }
    }
}

enum class MuscleGroup {
    CHEST, BACK, SHOULDERS, ARMS, LEGS, CORE, CARDIO, FULL_BODY
}

enum class Equipment {
    BARBELL, DUMBBELL, MACHINE, CABLE, BODYWEIGHT, KETTLEBELL, BANDS, NONE, OTHER
}