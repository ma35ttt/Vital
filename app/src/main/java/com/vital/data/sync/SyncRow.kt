package com.vital.data.sync

/**
 * One row as it travels to and from the cloud. [id] is the entity's own id (the Firestore
 * document id), [json] is the serialized entity, and [updatedAt]/[deleted] mirror the entity's
 * fields so the cloud can filter and resolve conflicts without deserializing.
 */
data class SyncRow(
    val id: String,
    val json: String,
    val updatedAt: Long,
    val deleted: Boolean
)

/** Summary of a sync pass, used for the Settings status line. */
data class SyncSummary(
    val pushed: Int,
    val pulled: Int
)
