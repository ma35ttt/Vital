package com.vital.data.repository

import com.vital.data.dao.UserDao
import com.vital.data.entity.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getUser(userId: String): Flow<User?> = userDao.getUser(userId)
    fun getLatestUser(): Flow<User?> = userDao.getLatestUser()
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun softDeleteUser(userId: String, updatedAt: Long) = userDao.softDeleteUser(userId, updatedAt)
    suspend fun getUserSync(userId: String): User? = userDao.getUserSync(userId)
}