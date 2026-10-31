package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.BodyMetrics
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricsDao {
    @Query("SELECT * FROM body_metrics WHERE userId = :userId AND deleted = 0 ORDER BY date DESC")
    fun getAllBodyMetrics(userId: String): Flow<List<BodyMetrics>>

    @Query("SELECT * FROM body_metrics WHERE userId = :userId AND date = :date AND deleted = 0")
    fun getBodyMetricsForDate(userId: String, date: String): Flow<BodyMetrics?>

    @Query("SELECT * FROM body_metrics WHERE id = :id AND deleted = 0")
    fun getBodyMetrics(id: String): Flow<BodyMetrics?>

    @Query("SELECT * FROM body_metrics WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND deleted = 0 ORDER BY date ASC")
    fun getBodyMetricsForRange(userId: String, startDate: String, endDate: String): Flow<List<BodyMetrics>>

    @Query("SELECT * FROM body_metrics WHERE userId = :userId AND deleted = 0 ORDER BY date DESC LIMIT 1")
    fun getLatestBodyMetrics(userId: String): Flow<BodyMetrics?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMetrics(metrics: BodyMetrics)

    @Update
    suspend fun updateBodyMetrics(metrics: BodyMetrics)

    @Query("UPDATE body_metrics SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteBodyMetrics(id: String, updatedAt: Long)

    @Query("SELECT * FROM body_metrics WHERE updatedAt > :since AND deleted = 0")
    suspend fun getBodyMetricsForSync(since: Long): List<BodyMetrics>
}