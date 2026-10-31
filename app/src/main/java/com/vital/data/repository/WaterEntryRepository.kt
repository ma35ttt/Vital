package com.vital.data.repository

import com.vital.data.dao.WaterEntryDao
import com.vital.data.entity.WaterEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterEntryRepository @Inject constructor(
    private val waterEntryDao: WaterEntryDao
) {
    fun getWaterEntriesForDate(userId: String, date: String): Flow<List<WaterEntry>> = waterEntryDao.getWaterEntriesForDate(userId, date)
    fun getWaterEntriesForRange(userId: String, startDate: String, endDate: String): Flow<List<WaterEntry>> = waterEntryDao.getWaterEntriesForRange(userId, startDate, endDate)
    fun getWaterEntry(id: String): Flow<WaterEntry?> = waterEntryDao.getWaterEntry(id)

    suspend fun insertWaterEntry(entry: WaterEntry) = waterEntryDao.insertWaterEntry(entry)
    suspend fun insertWaterEntries(entries: List<WaterEntry>) = waterEntryDao.insertWaterEntries(entries)
    suspend fun updateWaterEntry(entry: WaterEntry) = waterEntryDao.updateWaterEntry(entry)
    suspend fun softDeleteWaterEntry(id: String, updatedAt: Long) = waterEntryDao.softDeleteWaterEntry(id, updatedAt)
    suspend fun getWaterEntriesForSync(since: Long): List<WaterEntry> = waterEntryDao.getWaterEntriesForSync(since)
    suspend fun getTotalWaterForDate(userId: String, date: String): Int? = waterEntryDao.getTotalWaterForDate(userId, date)
}