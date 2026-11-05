package com.vital.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "cardio_sessions",
    indices = [Index("date"), Index("userId")]
)
@Serializable
data class CardioSession(
    @PrimaryKey
    val id: String, // UUID
    val userId: String,
    val date: String, // yyyy-MM-dd
    val type: CardioType,
    val durationSec: Int,
    val distanceKm: Double? = null,
    val speedKmh: Double? = null,
    val inclinePct: Double? = null,
    // Activity-specific fields; only the ones the selected type collects are set.
    val level: Int? = null, // Bike / Elliptical resistance, Stairs level
    val cadenceSpm: Double? = null, // Row stroke rate, Stairs steps per minute
    val floors: Int? = null, // Stairs
    val laps: Int? = null, // Swim
    val stroke: StrokeType? = null, // Swim
    val intensity: Intensity? = null, // Other
    val kcal: Double,
    val kcalSource: KcalSource = KcalSource.READ,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            userId: String,
            date: String,
            type: CardioType,
            durationSec: Int,
            kcal: Double,
            distanceKm: Double? = null,
            speedKmh: Double? = null,
            inclinePct: Double? = null,
            level: Int? = null,
            cadenceSpm: Double? = null,
            floors: Int? = null,
            laps: Int? = null,
            stroke: StrokeType? = null,
            intensity: Intensity? = null,
            kcalSource: KcalSource = KcalSource.READ
        ): CardioSession {
            return CardioSession(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                type = type,
                durationSec = durationSec,
                distanceKm = distanceKm,
                speedKmh = speedKmh,
                inclinePct = inclinePct,
                level = level,
                cadenceSpm = cadenceSpm,
                floors = floors,
                laps = laps,
                stroke = stroke,
                intensity = intensity,
                kcal = kcal,
                kcalSource = kcalSource
            )
        }
    }
}

enum class CardioType {
    RUNNING, WALKING, CYCLING, ROWING, SWIMMING, ELLIPTICAL, STAIR_CLIMBER, OTHER
}

/** Swim stroke; drives the MET used for the calorie estimate. */
enum class StrokeType {
    FREESTYLE, BREASTSTROKE, BACKSTROKE, BUTTERFLY, MIXED
}

/** Effort level for the catch-all Other type; drives the MET. */
enum class Intensity {
    LIGHT, MODERATE, VIGOROUS
}

enum class KcalSource {
    READ, ACSM, MET
}