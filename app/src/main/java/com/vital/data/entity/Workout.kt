package com.vital.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "workout_sessions",
    indices = [Index("date"), Index("userId")]
)
@Serializable
data class WorkoutSession(
    @PrimaryKey
    val id: String, // UUID
    val userId: String,
    val date: String, // yyyy-MM-dd
    val startedAt: Long,
    val endedAt: Long? = null,
    val notes: String? = null,
    val routineId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(userId: String, date: String, routineId: String? = null): WorkoutSession {
            return WorkoutSession(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                startedAt = System.currentTimeMillis(),
                routineId = routineId
            )
        }
    }
}

@Entity(
    tableName = "workout_sets",
    indices = [Index("sessionId"), Index("exerciseId")]
)
@Serializable
data class WorkoutSet(
    @PrimaryKey
    val id: String, // UUID
    val sessionId: String,
    val exerciseId: String,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double? = null,
    val isWarmup: Boolean = false,
    val restSecTaken: Int? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            sessionId: String,
            exerciseId: String,
            setIndex: Int,
            weightKg: Double,
            reps: Int,
            rpe: Double? = null,
            isWarmup: Boolean = false
        ): WorkoutSet {
            return WorkoutSet(
                id = java.util.UUID.randomUUID().toString(),
                sessionId = sessionId,
                exerciseId = exerciseId,
                setIndex = setIndex,
                weightKg = weightKg,
                reps = reps,
                rpe = rpe,
                isWarmup = isWarmup
            )
        }
    }
}