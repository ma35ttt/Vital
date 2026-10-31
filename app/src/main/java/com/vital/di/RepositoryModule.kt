package com.vital.di

import com.vital.data.repository.BodyMetricsRepository
import com.vital.data.repository.CardioRepository
import com.vital.data.repository.ExerciseRepository
import com.vital.data.repository.FoodRepository
import com.vital.data.repository.MealLogRepository
import com.vital.data.repository.ProgressPhotoRepository
import com.vital.data.repository.RecipeRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.SyncMetaRepository
import com.vital.data.repository.UserRepository
import com.vital.data.repository.WaterEntryRepository
import com.vital.data.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton fun provideUserRepository(dao: com.vital.data.dao.UserDao) = UserRepository(dao)
    @Provides @Singleton fun provideFoodRepository(dao: com.vital.data.dao.FoodDao) = FoodRepository(dao)
    @Provides @Singleton fun provideRecipeRepository(dao: com.vital.data.dao.RecipeDao) = RecipeRepository(dao)
    @Provides @Singleton fun provideMealLogRepository(dao: com.vital.data.dao.MealLogDao) = MealLogRepository(dao)
    @Provides @Singleton fun provideExerciseRepository(dao: com.vital.data.dao.ExerciseDao) = ExerciseRepository(dao)
    @Provides @Singleton fun provideWorkoutRepository(dao: com.vital.data.dao.WorkoutDao) = WorkoutRepository(dao)
    @Provides @Singleton fun provideCardioRepository(dao: com.vital.data.dao.CardioDao) = CardioRepository(dao)
    @Provides @Singleton fun provideBodyMetricsRepository(dao: com.vital.data.dao.BodyMetricsDao) = BodyMetricsRepository(dao)
    @Provides @Singleton fun provideProgressPhotoRepository(dao: com.vital.data.dao.ProgressPhotoDao) = ProgressPhotoRepository(dao)
    @Provides @Singleton fun provideWaterEntryRepository(dao: com.vital.data.dao.WaterEntryDao) = WaterEntryRepository(dao)
    @Provides @Singleton fun provideSettingsRepository(dao: com.vital.data.dao.SettingsDao) = SettingsRepository(dao)
    @Provides @Singleton fun provideSyncMetaRepository(dao: com.vital.data.dao.SyncMetaDao) = SyncMetaRepository(dao)
}