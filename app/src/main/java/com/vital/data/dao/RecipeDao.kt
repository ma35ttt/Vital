package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE deleted = 0 ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id AND deleted = 0")
    fun getRecipe(id: String): Flow<Recipe?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("UPDATE recipes SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteRecipe(id: String, updatedAt: Long)

    // Sync reads include tombstones (deleted rows) so removals propagate to other devices.
    @Query("SELECT * FROM recipes WHERE updatedAt > :since")
    suspend fun getRecipesForSync(since: Long): List<Recipe>
}