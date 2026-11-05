package com.vital.data.sync

/**
 * The cloud half of sync. Kept behind an interface so the engine can be tested with a fake and
 * so the backend could change without touching the merge logic.
 *
 * Data is stored per signed-in account at `users/{uid}/{table}/{rowId}`.
 */
interface CloudStore {

    /** Writes (creates or overwrites) [rows] for [table] under the account [uid]. */
    suspend fun push(uid: String, table: String, rows: List<SyncRow>)

    /** Reads the rows for [table] whose `updatedAt` is strictly after [since]. */
    suspend fun pull(uid: String, table: String, since: Long): List<SyncRow>
}
