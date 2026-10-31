package com.vital.di

import com.vital.ai.AiProvider
import com.vital.ai.CoachChatService
import com.vital.ai.FoodAnalysisService
import com.vital.ai.GeminiClient
import com.vital.ai.OpenRouterClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGeminiClient(client: OkHttpClient): GeminiClient = GeminiClient(client)

    @Provides
    @Singleton
    fun provideOpenRouterClient(client: OkHttpClient): OpenRouterClient = OpenRouterClient(client)

    @Provides
    @Singleton
    fun provideFoodAnalysisService(
        gemini: GeminiClient,
        openRouter: OpenRouterClient
    ): FoodAnalysisService = FoodAnalysisService(gemini, openRouter)

    @Provides
    @Singleton
    fun provideCoachChatService(
        gemini: GeminiClient,
        openRouter: OpenRouterClient
    ): CoachChatService = CoachChatService(gemini, openRouter)
}