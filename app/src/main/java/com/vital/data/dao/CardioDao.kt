package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.CardioSession
import kotlinx.coroutines.flow.Flow

@Dao
interface CardioDao {
    @Query("SELECT * FROM cardio_sessions WHERE userId = :userId AND deleted = 0 ORDER BY date DESC, updatedAt DESC")
    fun getAllCardioSessions(userId: String): Flow<List<CardioSession>>

    @Query("SELECT * FROM cardio_sessions WHERE userId = :userId AND date = :date AND deleted = 0 ORDER BY updatedAt DESC")
    fun getCardioForDate(userId: String, date: String): Flow<List<CardioSession>>

    @Query("SELECT * FROM cardio_sessions WHERE id = :id AND deleted = 0")
    fun getCardioSession(id: String): Flow<CardioSession?>

    @Query("SELECT * FROM cardio_sessions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND deleted = 0 ORDER BY date DESC")
    fun getCardioForRange(userId: String, startDate: String, endDate: String): Flow<List<CardioSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardioSession(session: CardioSession)

    @Update
    suspend fun updateCardioSession(session: CardioSession)

    @Query("UPDATE cardio_sessions SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCardioSession(id: String, updatedAt: Long)

    @Query("SELECT * FROM cardio_sessions WHERE updatedAt > :since AND deleted = 0")
    suspend fun getCardioSessionsForSync(since: Long): List<CardioSession>

    @Query("SELECT SUM(kcal) FROM cardio_sessions WHERE userId = :userId AND date = :date AND deleted = 0")
    suspend fun getTotalKcalForDate(userId: String, date: String): Double?

    @Query("SELECT SUM(kcal) FROM cardio_sessions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND deleted = 0")
    suspend fun getTotalKcalForRange(userId: String, startDate: String, endDate: String): Double?
}