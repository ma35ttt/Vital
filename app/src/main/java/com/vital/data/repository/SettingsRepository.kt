package com.vital.data.repository

import com.vital.data.dao.SettingsDao
import com.vital.data.entity.Settings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    fun getSettings(userId: String): Flow<Settings?> = settingsDao.getSettings(userId)
    suspend fun insertSettings(settings: Settings) = settingsDao.insertSettings(settings)
    suspend fun updateSettings(settings: Settings) = settingsDao.updateSettings(settings)
}