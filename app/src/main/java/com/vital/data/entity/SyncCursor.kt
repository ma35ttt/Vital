package com.vital.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-table high-water marks for cloud sync, so each table pulls and pushes only what changed
 * since it last synced. One row per synced table, keyed by [tableName].
 *
 * [lastPullAt] is the newest `updatedAt` seen from the cloud; [lastPushAt] is the newest
 * `updatedAt` pushed to it. Keeping them per-table avoids one busy table hiding changes in
 * another.
 */
@Entity(tableName = "sync_cursors")
data class SyncCursor(
    @PrimaryKey val tableName: String,
    val lastPullAt: Long = 0,
    val lastPushAt: Long = 0
)
