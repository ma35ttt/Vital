package com.vital.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "meal_log_entries",
    indices = [Index("date"), Index("userId")]
)
@Serializable
data class MealLogEntry(
    @PrimaryKey
    val id: String, // UUID
    val userId: String,
    val date: String, // yyyy-MM-dd
    val mealSlot: MealSlot,
    val foodId: String? = null,
    val recipeId: String? = null,
    val foodName: String, // Snapshotted
    val variant: FoodVariant = FoodVariant.NONE,
    val grams: Double,
    val proteinG: Double, // Snapshotted macros
    val carbsG: Double,
    val fatG: Double,
    val kcal: Double,
    val source: FoodSource = FoodSource.USER,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            userId: String,
            date: String,
            mealSlot: MealSlot,
            foodId: String?,
            recipeId: String?,
            foodName: String,
            variant: FoodVariant,
            grams: Double,
            proteinG: Double,
            carbsG: Double,
            fatG: Double,
            kcal: Double,
            source: FoodSource = FoodSource.USER
        ): MealLogEntry {
            return MealLogEntry(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                mealSlot = mealSlot,
                foodId = foodId,
                recipeId = recipeId,
                foodName = foodName,
                variant = variant,
                grams = grams,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG,
                kcal = kcal,
                source = source
            )
        }
    }
}

enum class MealSlot {
    BREAKFAST, LUNCH, DINNER, SNACK
}