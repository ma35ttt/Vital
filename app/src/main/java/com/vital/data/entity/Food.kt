package com.vital.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "foods")
@Serializable
data class Food(
    @PrimaryKey
    val id: String, // UUID
    val name: String,
    val variant: FoodVariant = FoodVariant.NONE,
    val proteinPer100: Double,
    val carbsPer100: Double,
    val fatPer100: Double,
    val kcalPer100: Double,
    val source: FoodSource = FoodSource.USER,
    val barcode: String? = null,
    val verified: Boolean = false,
    val referenceName: String? = null, // USDA/FCT reference name
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
) {
    companion object {
        fun create(
            name: String,
            variant: FoodVariant,
            proteinPer100: Double,
            carbsPer100: Double,
            fatPer100: Double,
            source: FoodSource = FoodSource.USER,
            barcode: String? = null,
            referenceName: String? = null
        ): Food {
            val kcal = (proteinPer100 * 4) + (carbsPer100 * 4) + (fatPer100 * 9)
            return Food(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                variant = variant,
                proteinPer100 = proteinPer100,
                carbsPer100 = carbsPer100,
                fatPer100 = fatPer100,
                kcalPer100 = kcal,
                source = source,
                barcode = barcode,
                referenceName = referenceName
            )
        }
    }
}

enum class FoodVariant {
    RAW, COOKED, NONE
}

enum class FoodSource {
    SEED, USER, AI, OFF
}