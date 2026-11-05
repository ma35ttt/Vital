package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.Food
import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE deleted = 0 ORDER BY name ASC")
    fun getAllFoods(): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE id = :id AND deleted = 0")
    fun getFood(id: String): Flow<Food?>

    @Query("SELECT * FROM foods WHERE barcode = :barcode AND deleted = 0")
    fun getFoodByBarcode(barcode: String): Flow<Food?>

    @Query("SELECT * FROM foods WHERE name LIKE :query AND deleted = 0 ORDER BY CASE WHEN source = 'SEED' THEN 0 ELSE 1 END, name ASC LIMIT 20")
    fun searchFoods(query: String): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE name = :name AND variant = :variant AND deleted = 0")
    fun getFoodByNameAndVariant(name: String, variant: FoodVariant): Flow<Food?>

    @Query("SELECT * FROM foods WHERE (name LIKE :query OR name LIKE :query2) AND deleted = 0 ORDER BY name ASC LIMIT 50")
    fun fuzzySearchFoods(query: String, query2: String): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE source = :source AND deleted = 0")
    fun getFoodsBySource(source: FoodSource): Flow<List<Food>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<Food>)

    @Update
    suspend fun updateFood(food: Food)

    @Query("UPDATE foods SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteFood(id: String, updatedAt: Long)

    // Sync reads include tombstones and skip seeded/OpenFoodFacts rows (re-derivable caches);
    // user- and AI-created foods carry the data worth syncing.
    @Query("SELECT * FROM foods WHERE updatedAt > :since AND source NOT IN ('SEED', 'OFF')")
    suspend fun getFoodsForSync(since: Long): List<Food>

    @Query("SELECT COUNT(*) FROM foods WHERE deleted = 0")
    suspend fun getFoodCount(): Int

    @Query("SELECT * FROM foods WHERE id IN (:ids)")
    suspend fun getFoodsByIds(ids: List<String>): List<Food>
}