package com.vital.data.repository

import com.vital.data.dao.RecipeDao
import com.vital.data.entity.Recipe
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
    fun getRecipe(id: String): Flow<Recipe?> = recipeDao.getRecipe(id)
    suspend fun insertRecipe(recipe: Recipe) = recipeDao.insertRecipe(recipe)
    suspend fun insertRecipes(recipes: List<Recipe>) = recipeDao.insertRecipes(recipes)
    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)
    suspend fun softDeleteRecipe(id: String, updatedAt: Long) = recipeDao.softDeleteRecipe(id, updatedAt)
    suspend fun getRecipesForSync(since: Long): List<Recipe> = recipeDao.getRecipesForSync(since)
}