package com.vital.data.repository

import com.vital.data.dao.ProgressPhotoDao
import com.vital.data.entity.ProgressPhoto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressPhotoRepository @Inject constructor(
    private val progressPhotoDao: ProgressPhotoDao
) {
    fun getAllProgressPhotos(userId: String): Flow<List<ProgressPhoto>> = progressPhotoDao.getAllProgressPhotos(userId)
    fun getProgressPhotosByType(userId: String, type: com.vital.data.entity.PhotoType): Flow<List<ProgressPhoto>> = progressPhotoDao.getProgressPhotosByType(userId, type)
    fun getProgressPhoto(id: String): Flow<ProgressPhoto?> = progressPhotoDao.getProgressPhoto(id)
    fun getProgressPhotosForDate(userId: String, date: String): Flow<List<ProgressPhoto>> = progressPhotoDao.getProgressPhotosForDate(userId, date)

    suspend fun insertProgressPhoto(photo: ProgressPhoto) = progressPhotoDao.insertProgressPhoto(photo)
    suspend fun updateProgressPhoto(photo: ProgressPhoto) = progressPhotoDao.updateProgressPhoto(photo)
    suspend fun softDeleteProgressPhoto(id: String, updatedAt: Long) = progressPhotoDao.softDeleteProgressPhoto(id, updatedAt)
    suspend fun getProgressPhotosForSync(since: Long): List<ProgressPhoto> = progressPhotoDao.getProgressPhotosForSync(since)
}