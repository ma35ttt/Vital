package com.vital.data.repository

import com.vital.data.dao.MealLogDao
import com.vital.data.entity.MealLogEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealLogRepository @Inject constructor(
    private val mealLogDao: MealLogDao
) {
    fun getMealsForDate(userId: String, date: String): Flow<List<MealLogEntry>> = mealLogDao.getMealsForDate(userId, date)
    fun getMealsForDateRange(userId: String, startDate: String, endDate: String): Flow<List<MealLogEntry>> = mealLogDao.getMealsForDateRange(userId, startDate, endDate)
    fun getLatestMealForFood(userId: String, foodId: String): Flow<MealLogEntry?> = mealLogDao.getLatestMealForFood(userId, foodId)

    suspend fun insertMealLog(entry: MealLogEntry) = mealLogDao.insertMealLog(entry)
    suspend fun insertMealLogs(entries: List<MealLogEntry>) = mealLogDao.insertMealLogs(entries)
    suspend fun updateMealLog(entry: MealLogEntry) = mealLogDao.updateMealLog(entry)
    suspend fun softDeleteMealLog(id: String, updatedAt: Long) = mealLogDao.softDeleteMealLog(id, updatedAt)
    suspend fun getMealLogsForSync(since: Long): List<MealLogEntry> = mealLogDao.getMealLogsForSync(since)
    suspend fun getTotalKcalForDate(userId: String, date: String): Double? = mealLogDao.getTotalKcalForDate(userId, date)
    suspend fun getTotalMacrosForDate(userId: String, date: String) = mealLogDao.getTotalMacrosForDate(userId, date)
    suspend fun getTotalKcalForRange(userId: String, startDate: String, endDate: String): Double? = mealLogDao.getTotalKcalForRange(userId, startDate, endDate)
}