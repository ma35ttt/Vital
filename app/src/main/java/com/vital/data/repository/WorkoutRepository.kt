package com.vital.data.repository

import com.vital.data.dao.WorkoutDao
import com.vital.data.entity.WorkoutSession
import com.vital.data.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {
    // Sessions
    fun getAllSessions(userId: String): Flow<List<WorkoutSession>> = workoutDao.getAllSessions(userId)
    fun getSessionsForDate(userId: String, date: String): Flow<List<WorkoutSession>> = workoutDao.getSessionsForDate(userId, date)
    fun getSession(id: String): Flow<WorkoutSession?> = workoutDao.getSession(id)
    fun getActiveSession(userId: String): Flow<WorkoutSession?> = workoutDao.getActiveSession(userId)
    fun getSessionsForRange(userId: String, startDate: String, endDate: String): Flow<List<WorkoutSession>> = workoutDao.getSessionsForRange(userId, startDate, endDate)

    suspend fun insertSession(session: WorkoutSession) = workoutDao.insertSession(session)
    suspend fun updateSession(session: WorkoutSession) = workoutDao.updateSession(session)
    suspend fun softDeleteSession(id: String, updatedAt: Long) = workoutDao.softDeleteSession(id, updatedAt)
    suspend fun getSessionsForSync(since: Long): List<WorkoutSession> = workoutDao.getSessionsForSync(since)

    // Sets
    fun getSetsForSession(sessionId: String): Flow<List<WorkoutSet>> = workoutDao.getSetsForSession(sessionId)
    fun getSetsForExercise(sessionId: String, exerciseId: String): Flow<List<WorkoutSet>> = workoutDao.getSetsForExercise(sessionId, exerciseId)
    fun getLatestSetForExercise(exerciseId: String): Flow<WorkoutSet?> = workoutDao.getLatestSetForExercise(exerciseId)
    fun getRecentWorkingSets(exerciseId: String): Flow<List<WorkoutSet>> = workoutDao.getRecentWorkingSets(exerciseId)

    suspend fun insertSet(set: WorkoutSet) = workoutDao.insertSet(set)
    suspend fun insertSets(sets: List<WorkoutSet>) = workoutDao.insertSets(sets)
    suspend fun updateSet(set: WorkoutSet) = workoutDao.updateSet(set)
    suspend fun softDeleteSet(id: String, updatedAt: Long) = workoutDao.softDeleteSet(id, updatedAt)
    suspend fun getSetsForSync(since: Long): List<WorkoutSet> = workoutDao.getSetsForSync(since)

    // Volume
    suspend fun getVolumeForRange(userId: String, startDate: String, endDate: String): Double? = workoutDao.getVolumeForRange(userId, startDate, endDate)
}