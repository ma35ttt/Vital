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
                kcal = kcal,
                kcalSource = kcalSource
            )
        }
    }
}

enum class CardioType {
    RUNNING, WALKING, CYCLING, ROWING, SWIMMING, ELLIPTICAL, STAIR_CLIMBER, OTHER
}

enum class KcalSource {
    READ, ACSM
}