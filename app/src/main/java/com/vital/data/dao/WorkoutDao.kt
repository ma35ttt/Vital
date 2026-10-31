package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.WorkoutSession
import com.vital.data.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    // Workout Sessions
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND deleted = 0 ORDER BY date DESC, startedAt DESC")
    fun getAllSessions(userId: String): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND date = :date AND deleted = 0 ORDER BY startedAt DESC")
    fun getSessionsForDate(userId: String, date: String): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id AND deleted = 0")
    fun getSession(id: String): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND endedAt IS NULL AND deleted = 0 ORDER BY startedAt DESC LIMIT 1")
    fun getActiveSession(userId: String): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND deleted = 0 ORDER BY date DESC")
    fun getSessionsForRange(userId: String, startDate: String, endDate: String): Flow<List<WorkoutSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession)

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("UPDATE workout_sessions SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteSession(id: String, updatedAt: Long)

    @Query("SELECT * FROM workout_sessions WHERE updatedAt > :since AND deleted = 0")
    suspend fun getSessionsForSync(since: Long): List<WorkoutSession>

    // Workout Sets
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId AND deleted = 0 ORDER BY setIndex ASC")
    fun getSetsForSession(sessionId: String): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId AND exerciseId = :exerciseId AND deleted = 0 ORDER BY setIndex ASC")
    fun getSetsForExercise(sessionId: String, exerciseId: String): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND deleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestSetForExercise(exerciseId: String): Flow<WorkoutSet?>

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND isWarmup = 0 AND deleted = 0 ORDER BY updatedAt DESC LIMIT 5")
    fun getRecentWorkingSets(exerciseId: String): Flow<List<WorkoutSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSet>)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Query("UPDATE workout_sets SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteSet(id: String, updatedAt: Long)

    @Query("SELECT * FROM workout_sets WHERE updatedAt > :since AND deleted = 0")
    suspend fun getSetsForSync(since: Long): List<WorkoutSet>

    // Volume calculation
    @Query("SELECT SUM(weightKg * reps) FROM workout_sets ws JOIN workout_sessions wss ON ws.sessionId = wss.id WHERE wss.userId = :userId AND wss.date BETWEEN :startDate AND :endDate AND ws.deleted = 0 AND wss.deleted = 0")
    suspend fun getVolumeForRange(userId: String, startDate: String, endDate: String): Double?
}