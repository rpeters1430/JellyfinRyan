package com.example.jellyfinryan.di

import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.data.preferences.DataStoreManager
import com.example.jellyfinryan.utils.UnsafeOkHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
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

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        // Create logging interceptor for image requests
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // Use the same unsafe HTTP client as the API calls for SSL bypass
        val unsafeHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient()
            .newBuilder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithHeaders = originalRequest.newBuilder()
                    .addHeader("User-Agent", "JellyfinRyan/1.0 (Android TV)")
                    .build()

                android.util.Log.d("CoilImageLoader", "🖼️ Loading image: ${requestWithHeaders.url}")
                val response = chain.proceed(requestWithHeaders)
                android.util.Log.d("CoilImageLoader", "🖼️ Image response: ${response.code} for ${requestWithHeaders.url}")
                response
            }
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(unsafeHttpClient) // Use SSL bypass for images
            .logger(DebugLogger()) // Enable Coil debug logging
            .respectCacheHeaders(false) // Don't respect cache headers for debugging
            .build()
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

