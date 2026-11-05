package com.vital.di

import android.content.Context
import com.vital.data.dao.BodyMetricsDao
import com.vital.data.dao.CardioDao
import com.vital.data.dao.ExerciseDao
import com.vital.data.dao.FoodDao
import com.vital.data.dao.MealLogDao
import com.vital.data.dao.RecipeDao
import com.vital.data.dao.SettingsDao
import com.vital.data.dao.UserDao
import com.vital.data.dao.WorkoutDao
import com.vital.data.database.VitalDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): VitalDatabase {
        return VitalDatabase.getDatabase(context)
    }

    // Room's generated database caches each DAO instance internally, so these need no scope
    // of their own. RepositoryModule consumes them. Only the DAOs that SeedLoader or a screen
    // reaches are bound so far; any other repository starts failing to resolve the moment
    // something asks for it, and its DAO gets a matching @Provides then.
    @Provides fun provideUserDao(db: VitalDatabase): UserDao = db.userDao()
    @Provides fun provideFoodDao(db: VitalDatabase): FoodDao = db.foodDao()
    @Provides fun provideMealLogDao(db: VitalDatabase): MealLogDao = db.mealLogDao()
    @Provides fun provideRecipeDao(db: VitalDatabase): RecipeDao = db.recipeDao()
    @Provides fun provideExerciseDao(db: VitalDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideWorkoutDao(db: VitalDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideCardioDao(db: VitalDatabase): CardioDao = db.cardioDao()
    @Provides fun provideBodyMetricsDao(db: VitalDatabase): BodyMetricsDao = db.bodyMetricsDao()
    @Provides fun provideSettingsDao(db: VitalDatabase): SettingsDao = db.settingsDao()
}