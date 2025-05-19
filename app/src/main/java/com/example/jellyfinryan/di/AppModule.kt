package com.example.jellyfinryan.di

import com.example.jellyfinryan.api.JellyfinApiService
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.JellyfinRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJellyfinRepository(apiService: JellyfinApiService): JellyfinRepository {
        return JellyfinRepositoryImpl(apiService)
    }
}