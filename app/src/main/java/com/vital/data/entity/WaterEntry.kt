package com.vital.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "water_entries",
    indices = [Index("date"), Index("userId")]
)
@Serializable
data class WaterEntry(
    @PrimaryKey
    val id: String, // UUID
    val userId: String,
    val date: String, // yyyy-MM-dd
    val amountMl: Int,
    val time: Long, // Time of day in milliseconds since epoch (for sorting)
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            userId: String,
            date: String,
            amountMl: Int,
            time: Long = System.currentTimeMillis()
        ): WaterEntry {
            return WaterEntry(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                amountMl = amountMl,
                time = time
            )
        }
    }
}