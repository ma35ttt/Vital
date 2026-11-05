package com.vital.data.seed

import androidx.room.TypeConverter
import com.vital.data.entity.FoodVariant
import com.vital.data.entity.FoodSource
import com.vital.data.entity.MealSlot
import com.vital.data.entity.MuscleGroup
import com.vital.data.entity.Equipment
import com.vital.data.entity.CardioType
import com.vital.data.entity.KcalSource
import com.vital.data.entity.StrokeType
import com.vital.data.entity.Intensity
import com.vital.data.entity.PhotoType
import com.vital.data.entity.AiProvider
import com.vital.data.entity.Sex
import com.vital.data.entity.ActivityLevel
import com.vital.data.entity.GoalType
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import com.vital.data.entity.RecipeIngredient
import com.vital.data.entity.BodyMeasurements

class Converters {
    @TypeConverter
    fun fromFoodVariant(value: String?): FoodVariant? {
        return value?.let { FoodVariant.valueOf(it) }
    }

    @TypeConverter
    fun toFoodVariant(variant: FoodVariant?): String? {
        return variant?.name
    }

    @TypeConverter
    fun fromFoodSource(value: String?): FoodSource? {
        return value?.let { FoodSource.valueOf(it) }
    }

    @TypeConverter
    fun toFoodSource(source: FoodSource?): String? {
        return source?.name
    }

    @TypeConverter
    fun fromMealSlot(value: String?): MealSlot? {
        return value?.let { MealSlot.valueOf(it) }
    }

    @TypeConverter
    fun toMealSlot(slot: MealSlot?): String? {
        return slot?.name
    }

    @TypeConverter
    fun fromMuscleGroup(value: String?): MuscleGroup? {
        return value?.let { MuscleGroup.valueOf(it) }
    }

    @TypeConverter
    fun toMuscleGroup(group: MuscleGroup?): String? {
        return group?.name
    }

    @TypeConverter
    fun fromEquipment(value: String?): Equipment? {
        return value?.let { Equipment.valueOf(it) }
    }

    @TypeConverter
    fun toEquipment(equip: Equipment?): String? {
        return equip?.name
    }

    @TypeConverter
    fun fromCardioType(value: String?): CardioType? {
        return value?.let { CardioType.valueOf(it) }
    }

    @TypeConverter
    fun toCardioType(type: CardioType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromKcalSource(value: String?): KcalSource? {
        return value?.let { KcalSource.valueOf(it) }
    }

    @TypeConverter
    fun toKcalSource(source: KcalSource?): String? {
        return source?.name
    }

    @TypeConverter
    fun fromStrokeType(value: String?): StrokeType? {
        return value?.let { StrokeType.valueOf(it) }
    }

    @TypeConverter
    fun toStrokeType(type: StrokeType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromIntensity(value: String?): Intensity? {
        return value?.let { Intensity.valueOf(it) }
    }

    @TypeConverter
    fun toIntensity(value: Intensity?): String? {
        return value?.name
    }

    @TypeConverter
    fun fromPhotoType(value: String?): PhotoType? {
        return value?.let { PhotoType.valueOf(it) }
    }

    @TypeConverter
    fun toPhotoType(type: PhotoType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromAiProvider(value: String?): AiProvider? {
        return value?.let { AiProvider.valueOf(it) }
    }

    @TypeConverter
    fun toAiProvider(provider: AiProvider?): String? {
        return provider?.name
    }

    @TypeConverter
    fun fromSex(value: String?): Sex? {
        return value?.let { Sex.valueOf(it) }
    }

    @TypeConverter
    fun toSex(sex: Sex?): String? {
        return sex?.name
    }

    @TypeConverter
    fun fromActivityLevel(value: String?): ActivityLevel? {
        return value?.let { ActivityLevel.valueOf(it) }
    }

    @TypeConverter
    fun toActivityLevel(level: ActivityLevel?): String? {
        return level?.name
    }

    @TypeConverter
    fun fromGoalType(value: String?): GoalType? {
        return value?.let { GoalType.valueOf(it) }
    }

    @TypeConverter
    fun toGoalType(type: GoalType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromRecipeIngredients(value: String?): List<RecipeIngredient> {
        return value?.let { Json.decodeFromString<List<RecipeIngredient>>(it) } ?: emptyList()
    }

    @TypeConverter
    fun toRecipeIngredients(list: List<RecipeIngredient>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun fromBodyMeasurements(value: String?): BodyMeasurements? {
        return value?.let { Json.decodeFromString<BodyMeasurements>(it) }
    }

    @TypeConverter
    fun toBodyMeasurements(measurements: BodyMeasurements?): String? {
        return measurements?.let { Json.encodeToString(it) }
    }
}