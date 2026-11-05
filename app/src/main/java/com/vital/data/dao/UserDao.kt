package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId AND deleted = 0")
    fun getUser(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE deleted = 0 ORDER BY createdAt DESC LIMIT 1")
    fun getLatestUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET deleted = 1, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun softDeleteUser(userId: String, updatedAt: Long)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserSync(userId: String): User?

    // Sync reads include tombstones so an account removal propagates.
    @Query("SELECT * FROM users WHERE updatedAt > :since")
    suspend fun getUsersForSync(since: Long): List<User>
}