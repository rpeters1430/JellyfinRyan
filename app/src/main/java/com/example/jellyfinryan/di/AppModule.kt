package com.example.jellyfinryan.di

import android.content.Context
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.data.preferences.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideJellyfinRepository(
        dataStoreManager: DataStoreManager
    ): JellyfinRepository {
        return JellyfinRepository(dataStoreManager)
    }

    // 🚨 COMMENTED OUT - Enhanced SDK Repository (causing crashes)
    // We can re-enable this later when Ktor compatibility issues are resolved
    /*
    @Provides
    @Singleton
    fun provideJellyfinSdkRepository(
        @ApplicationContext context: Context
    ): JellyfinSdkRepository {
        return JellyfinSdkRepository(context)
    }
    */
}

