package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vital.data.entity.SyncCursor

@Dao
interface SyncCursorDao {
    @Query("SELECT * FROM sync_cursors WHERE tableName = :tableName")
    suspend fun getCursor(tableName: String): SyncCursor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCursor(cursor: SyncCursor)
}
