package com.vital.data.repository

import com.vital.data.dao.BodyMetricsDao
import com.vital.data.entity.BodyMetrics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyMetricsRepository @Inject constructor(
    private val bodyMetricsDao: BodyMetricsDao
) {
    fun getAllBodyMetrics(userId: String): Flow<List<BodyMetrics>> = bodyMetricsDao.getAllBodyMetrics(userId)
    fun getBodyMetricsForDate(userId: String, date: String): Flow<BodyMetrics?> = bodyMetricsDao.getBodyMetricsForDate(userId, date)
    fun getBodyMetrics(id: String): Flow<BodyMetrics?> = bodyMetricsDao.getBodyMetrics(id)
    fun getBodyMetricsForRange(userId: String, startDate: String, endDate: String): Flow<List<BodyMetrics>> = bodyMetricsDao.getBodyMetricsForRange(userId, startDate, endDate)
    fun getLatestBodyMetrics(userId: String): Flow<BodyMetrics?> = bodyMetricsDao.getLatestBodyMetrics(userId)

    suspend fun insertBodyMetrics(metrics: BodyMetrics) = bodyMetricsDao.insertBodyMetrics(metrics)
    suspend fun updateBodyMetrics(metrics: BodyMetrics) = bodyMetricsDao.updateBodyMetrics(metrics)
    suspend fun softDeleteBodyMetrics(id: String, updatedAt: Long) = bodyMetricsDao.softDeleteBodyMetrics(id, updatedAt)
    suspend fun getBodyMetricsForSync(since: Long): List<BodyMetrics> = bodyMetricsDao.getBodyMetricsForSync(since)
}