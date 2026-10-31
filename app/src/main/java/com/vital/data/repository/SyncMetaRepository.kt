package com.vital.data.repository

import com.vital.data.dao.SyncMetaDao
import com.vital.data.entity.SyncMeta
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMetaRepository @Inject constructor(
    private val syncMetaDao: SyncMetaDao
) {
    fun getSyncMeta(): Flow<SyncMeta?> = syncMetaDao.getSyncMeta()
    suspend fun insertSyncMeta(meta: SyncMeta) = syncMetaDao.insertSyncMeta(meta)
    suspend fun updateSyncMeta(meta: SyncMeta) = syncMetaDao.updateSyncMeta(meta)
}