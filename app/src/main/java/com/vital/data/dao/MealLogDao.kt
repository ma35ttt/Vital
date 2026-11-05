package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.MealLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_log_entries WHERE userId = :userId AND date = :date AND deleted = 0 ORDER BY mealSlot, updatedAt")
    fun getMealsForDate(userId: String, date: String): Flow<List<MealLogEntry>>

    @Query("SELECT * FROM meal_log_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND deleted = 0 ORDER BY date DESC, mealSlot")
    fun getMealsForDateRange(userId: String, startDate: String, endDate: String): Flow<List<MealLogEntry>>

    @Query("SELECT * FROM meal_log_entries WHERE userId = :userId AND foodId = :foodId AND deleted = 0 ORDER BY date DESC LIMIT 1")
    fun getLatestMealForFood(userId: String, foodId: String): Flow<MealLogEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(entry: MealLogEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLogs(entries: List<MealLogEntry>)

    @Update
    suspend fun updateMealLog(entry: MealLogEntry)

    @Query("UPDATE meal_log_entries SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteMealLog(id: String, updatedAt: Long)

    // Sync reads include tombstones (deleted rows) so removals propagate to other devices.
    @Query("SELECT * FROM meal_log_entries WHERE updatedAt > :since")
    suspend fun getMealLogsForSync(since: Long): List<MealLogEntry>

    @Query("SELECT SUM(kcal) FROM meal_log_entries WHERE userId = :userId AND date = :date AND deleted = 0")
    suspend fun getTotalKcalForDate(userId: String, date: String): Double?

    @Query("SELECT SUM(proteinG) as protein, SUM(carbsG) as carbs, SUM(fatG) as fat FROM meal_log_entries WHERE userId = :userId AND date = :date AND deleted = 0")
    suspend fun getTotalMacrosForDate(userId: String, date: String): MacrosSum?

    @Query("SELECT SUM(kcal) FROM meal_log_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND deleted = 0")
    suspend fun getTotalKcalForRange(userId: String, startDate: String, endDate: String): Double?
}

data class MacrosSum(
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?
)