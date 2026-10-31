package com.vital.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vital.data.dao.*
import com.vital.data.entity.*
import com.vital.data.seed.Converters

@Database(
    entities = [
        User::class,
        Food::class,
        Recipe::class,
        MealLogEntry::class,
        Exercise::class,
        WorkoutSession::class,
        WorkoutSet::class,
        CardioSession::class,
        BodyMetrics::class,
        ProgressPhoto::class,
        WaterEntry::class,
        Settings::class,
        SyncMeta::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VitalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun cardioDao(): CardioDao
    abstract fun bodyMetricsDao(): BodyMetricsDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun syncMetaDao(): SyncMetaDao

    companion object {
        @Volatile
        private var INSTANCE: VitalDatabase? = null

        fun getDatabase(context: Context): VitalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VitalDatabase::class.java,
                    "vital.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}