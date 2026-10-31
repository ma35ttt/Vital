package com.vital.di

import android.content.Context
import androidx.work.WorkManager
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
    fun provideWorkManager(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}