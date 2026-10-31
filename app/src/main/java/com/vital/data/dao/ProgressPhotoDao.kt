package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.ProgressPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {
    @Query("SELECT * FROM progress_photos WHERE userId = :userId AND deleted = 0 ORDER BY date DESC")
    fun getAllProgressPhotos(userId: String): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos WHERE userId = :userId AND type = :type AND deleted = 0 ORDER BY date DESC")
    fun getProgressPhotosByType(userId: String, type: com.vital.data.entity.PhotoType): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos WHERE id = :id AND deleted = 0")
    fun getProgressPhoto(id: String): Flow<ProgressPhoto?>

    @Query("SELECT * FROM progress_photos WHERE userId = :userId AND date = :date AND deleted = 0")
    fun getProgressPhotosForDate(userId: String, date: String): Flow<List<ProgressPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPhoto(photo: ProgressPhoto)

    @Update
    suspend fun updateProgressPhoto(photo: ProgressPhoto)

    @Query("UPDATE progress_photos SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteProgressPhoto(id: String, updatedAt: Long)

    @Query("SELECT * FROM progress_photos WHERE updatedAt > :since AND deleted = 0")
    suspend fun getProgressPhotosForSync(since: Long): List<ProgressPhoto>
}