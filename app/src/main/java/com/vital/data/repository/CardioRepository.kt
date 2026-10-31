package com.vital.data.repository

import com.vital.data.dao.CardioDao
import com.vital.data.entity.CardioSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardioRepository @Inject constructor(
    private val cardioDao: CardioDao
) {
    fun getAllCardioSessions(userId: String): Flow<List<CardioSession>> = cardioDao.getAllCardioSessions(userId)
    fun getCardioForDate(userId: String, date: String): Flow<List<CardioSession>> = cardioDao.getCardioForDate(userId, date)
    fun getCardioSession(id: String): Flow<CardioSession?> = cardioDao.getCardioSession(id)
    fun getCardioForRange(userId: String, startDate: String, endDate: String): Flow<List<CardioSession>> = cardioDao.getCardioForRange(userId, startDate, endDate)

    suspend fun insertCardioSession(session: CardioSession) = cardioDao.insertCardioSession(session)
    suspend fun updateCardioSession(session: CardioSession) = cardioDao.updateCardioSession(session)
    suspend fun softDeleteCardioSession(id: String, updatedAt: Long) = cardioDao.softDeleteCardioSession(id, updatedAt)
    suspend fun getCardioSessionsForSync(since: Long): List<CardioSession> = cardioDao.getCardioSessionsForSync(since)
    suspend fun getTotalKcalForDate(userId: String, date: String): Double? = cardioDao.getTotalKcalForDate(userId, date)
    suspend fun getTotalKcalForRange(userId: String, startDate: String, endDate: String): Double? = cardioDao.getTotalKcalForRange(userId, startDate, endDate)
}