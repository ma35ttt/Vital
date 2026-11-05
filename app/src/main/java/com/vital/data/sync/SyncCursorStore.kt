package com.vital.data.sync

import com.vital.data.dao.SyncCursorDao
import com.vital.data.entity.SyncCursor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-table high-water marks, so the engine can pull and push only what changed. Abstracted
 * from Room so the engine can be unit-tested without a database.
 */
interface SyncCursorStore {
    suspend fun cursor(tableName: String): SyncCursor
    suspend fun save(cursor: SyncCursor)
}

@Singleton
class RoomSyncCursorStore @Inject constructor(
    private val dao: SyncCursorDao
) : SyncCursorStore {
    override suspend fun cursor(tableName: String): SyncCursor =
        dao.getCursor(tableName) ?: SyncCursor(tableName)

    override suspend fun save(cursor: SyncCursor) = dao.upsertCursor(cursor)
}
