package com.vital.data.repository

import com.vital.data.dao.FoodDao
import com.vital.data.entity.Food
import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao
) {
    fun getAllFoods(): Flow<List<Food>> = foodDao.getAllFoods()
    fun getFood(id: String): Flow<Food?> = foodDao.getFood(id)
    fun getFoodByBarcode(barcode: String): Flow<Food?> = foodDao.getFoodByBarcode(barcode)
    fun searchFoods(query: String): Flow<List<Food>> = foodDao.searchFoods("%$query%")
    fun getFoodByNameAndVariant(name: String, variant: FoodVariant): Flow<Food?> = foodDao.getFoodByNameAndVariant(name, variant)
    fun fuzzySearchFoods(query: String): Flow<List<Food>> = foodDao.fuzzySearchFoods("%$query%", "%${query.replace(" ", "%")}%")
    fun getFoodsBySource(source: FoodSource): Flow<List<Food>> = foodDao.getFoodsBySource(source)

    suspend fun insertFood(food: Food) = foodDao.insertFood(food)
    suspend fun insertFoods(foods: List<Food>) = foodDao.insertFoods(foods)
    suspend fun updateFood(food: Food) = foodDao.updateFood(food)
    suspend fun softDeleteFood(id: String, updatedAt: Long) = foodDao.softDeleteFood(id, updatedAt)
    suspend fun getFoodsForSync(since: Long): List<Food> = foodDao.getFoodsForSync(since)
    suspend fun getFoodCount(): Int = foodDao.getFoodCount()
    suspend fun getFoodsByIds(ids: List<String>): List<Food> = foodDao.getFoodsByIds(ids)
}