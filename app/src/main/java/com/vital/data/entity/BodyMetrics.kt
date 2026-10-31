package com.vital.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "body_metrics",
    indices = [Index("date"), Index("userId")]
)
@Serializable
data class BodyMetrics(
    @PrimaryKey
    val id: String, // UUID
    val userId: String,
    val date: String, // yyyy-MM-dd
    val weightKg: Double,
    val bodyFatPct: Double? = null,
    val measurementsJson: String? = null, // JSON: waist, chest, arms, thighs, calves, hips, neck
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            userId: String,
            date: String,
            weightKg: Double,
            bodyFatPct: Double? = null,
            measurementsJson: String? = null
        ): BodyMetrics {
            return BodyMetrics(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                weightKg = weightKg,
                bodyFatPct = bodyFatPct,
                measurementsJson = measurementsJson
            )
        }
    }
}

@Serializable
data class BodyMeasurements(
    val waistCm: Double? = null,
    val chestCm: Double? = null,
    val leftArmCm: Double? = null,
    val rightArmCm: Double? = null,
    val leftThighCm: Double? = null,
    val rightThighCm: Double? = null,
    val leftCalfCm: Double? = null,
    val rightCalfCm: Double? = null,
    val hipsCm: Double? = null,
    val neckCm: Double? = null
)