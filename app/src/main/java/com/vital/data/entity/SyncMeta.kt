package com.vital.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "sync_meta")
@Serializable
data class SyncMeta(
    @PrimaryKey
    val id: Int = 1, // Single row table
    val lastPushAt: Long = 0,
    val lastPullAt: Long = 0,
    val lastError: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)