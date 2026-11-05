package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.WaterEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE userId = :userId AND date = :date AND deleted = 0 ORDER BY time ASC")
    fun getWaterEntriesForDate(userId: String, date: String): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND deleted = 0 ORDER BY date DESC, time ASC")
    fun getWaterEntriesForRange(userId: String, startDate: String, endDate: String): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries WHERE id = :id AND deleted = 0")
    fun getWaterEntry(id: String): Flow<WaterEntry?>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE userId = :userId AND date = :date AND deleted = 0")
    suspend fun getTotalWaterForDate(userId: String, date: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntries(entries: List<WaterEntry>)

    @Update
    suspend fun updateWaterEntry(entry: WaterEntry)

    @Query("UPDATE water_entries SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteWaterEntry(id: String, updatedAt: Long)

    // Sync reads include tombstones (deleted rows) so removals propagate to other devices.
    @Query("SELECT * FROM water_entries WHERE updatedAt > :since")
    suspend fun getWaterEntriesForSync(since: Long): List<WaterEntry>
}