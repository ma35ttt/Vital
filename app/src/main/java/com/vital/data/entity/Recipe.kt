package com.vital.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "recipes")
@Serializable
data class Recipe(
    @PrimaryKey
    val id: String, // UUID
    val name: String,
    val totalCookedWeightG: Double,
    val servingsNote: String? = null,
    val ingredientsJson: String, // JSON array of RecipeIngredient
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            name: String,
            totalCookedWeightG: Double,
            ingredientsJson: String,
            servingsNote: String? = null
        ): Recipe {
            return Recipe(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                totalCookedWeightG = totalCookedWeightG,
                servingsNote = servingsNote,
                ingredientsJson = ingredientsJson
            )
        }
    }
}

@Serializable
data class RecipeIngredient(
    val foodId: String,
    val foodName: String, // Denormalized for display
    val grams: Double,
    val variant: FoodVariant = FoodVariant.NONE
)