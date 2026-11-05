package com.vital.di

import android.content.Context
import com.vital.data.dao.BodyMetricsDao
import com.vital.data.dao.CardioDao
import com.vital.data.dao.ExerciseDao
import com.vital.data.dao.FoodDao
import com.vital.data.dao.MealLogDao
import com.vital.data.dao.RecipeDao
import com.vital.data.dao.SettingsDao
import com.vital.data.dao.SyncCursorDao
import com.vital.data.dao.UserDao
import com.vital.data.dao.WaterEntryDao
import com.vital.data.dao.WorkoutDao
import com.vital.data.sync.CloudStore
import com.vital.data.sync.FirestoreCloudStore
import com.vital.data.sync.RoomSyncCursorStore
import com.vital.data.sync.SyncCursorStore
import com.vital.data.sync.SyncTables
import com.vital.data.sync.buildSyncTables
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideCloudStore(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context
    ): CloudStore = FirestoreCloudStore(context)

    @Provides
    @Singleton
    fun provideSyncCursorStore(dao: SyncCursorDao): SyncCursorStore = RoomSyncCursorStore(dao)

    @Provides
    @Singleton
    fun provideSyncTables(
        userDao: UserDao,
        foodDao: FoodDao,
        recipeDao: RecipeDao,
        mealLogDao: MealLogDao,
        exerciseDao: ExerciseDao,
        workoutDao: WorkoutDao,
        cardioDao: CardioDao,
        bodyMetricsDao: BodyMetricsDao,
        waterEntryDao: WaterEntryDao,
        settingsDao: SettingsDao
    ): SyncTables = buildSyncTables(
        userDao = userDao,
        foodDao = foodDao,
        recipeDao = recipeDao,
        mealLogDao = mealLogDao,
        exerciseDao = exerciseDao,
        workoutDao = workoutDao,
        cardioDao = cardioDao,
        bodyMetricsDao = bodyMetricsDao,
        waterEntryDao = waterEntryDao,
        settingsDao = settingsDao
    )
}
