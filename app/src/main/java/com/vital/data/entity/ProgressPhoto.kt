package com.vital.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "progress_photos",
    indices = [Index("date"), Index("userId")]
)
@Serializable
data class ProgressPhoto(
    @PrimaryKey
    val id: String, // UUID
    val userId: String,
    val date: String, // yyyy-MM-dd
    val imagePath: String, // File path or content URI
    val type: PhotoType,
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            userId: String,
            date: String,
            imagePath: String,
            type: PhotoType,
            notes: String? = null
        ): ProgressPhoto {
            return ProgressPhoto(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                imagePath = imagePath,
                type = type,
                notes = notes
            )
        }
    }
}

enum class PhotoType {
    FRONT, SIDE, BACK, OTHER
}