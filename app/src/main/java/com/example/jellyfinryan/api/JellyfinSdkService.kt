package com.example.jellyfinryan.api

import android.content.Context
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.JellyfinOptions
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Jellyfin SDK Service using the official Jellyfin Kotlin SDK
 * This is the proper way to connect to Jellyfin servers and get images
 */
@Singleton
class JellyfinSdkService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var jellyfin: Jellyfin? = null
    private var apiClient: ApiClient? = null
      /**
     * Initialize the Jellyfin SDK with server connection
     */    suspend fun initialize(serverUrl: String, apiKey: String, userId: String) {
        jellyfin = Jellyfin(
            JellyfinOptions.Builder().apply {
                clientInfo = ClientInfo(
                    name = "JellyfinRyan",
                    version = "1.0"
                )
            }.build()
        )
        
        apiClient = jellyfin!!.createApi(
            baseUrl = serverUrl,
            accessToken = apiKey
        )
    }
    
    /**
     * Get recent items (movies, series, episodes)
     */    suspend fun getRecentItems(userId: String, limit: Int = 20): List<BaseItemDto> {
        return try {
            val response = apiClient?.userLibraryApi?.getLatestMedia(
                userId = java.util.UUID.fromString(userId),
                limit = limit,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SERIES_PRIMARY_IMAGE,
                    ItemFields.OVERVIEW,
                    ItemFields.GENRES,
                    ItemFields.DATE_CREATED,
                    ItemFields.TAGS,
                    ItemFields.PARENT_ID,
                    ItemFields.SERIES_STUDIO
                ),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES, BaseItemKind.EPISODE),
                enableImages = true
            )
            response?.content ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get featured items for carousel
     */    suspend fun getFeaturedItems(userId: String, limit: Int = 10): List<BaseItemDto> {
        return try {
            val response = apiClient?.itemsApi?.getItems(
                userId = java.util.UUID.fromString(userId),
                limit = limit,
                recursive = true,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SERIES_PRIMARY_IMAGE,
                    ItemFields.OVERVIEW,
                    ItemFields.GENRES,
                    ItemFields.DATE_CREATED,
                    ItemFields.TAGS,
                    ItemFields.PARENT_ID,
                    ItemFields.SERIES_STUDIO
                ),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                sortBy = listOf(org.jellyfin.sdk.model.api.ItemSortBy.DATE_CREATED),
                sortOrder = listOf(SortOrder.DESCENDING),
                enableImages = true,
                imageTypeLimit = 3
            )
            response?.content?.items ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get libraries
     */    suspend fun getLibraries(userId: String): List<BaseItemDto> {
        return try {
            val response = apiClient?.userViewsApi?.getUserViews(
                userId = java.util.UUID.fromString(userId)
            )
            response?.content?.items ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get the proper image URL using the official SDK
     * This is the key difference - the SDK knows how to construct proper image URLs
     */    fun getImageUrl(
        item: BaseItemDto,
        imageType: ImageType = ImageType.PRIMARY,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        quality: Int? = 96
    ): String? {
        return try {
            apiClient?.imageApi?.getItemImageUrl(
                itemId = item.id!!,
                imageType = imageType,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                quality = quality
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get backdrop image URL
     */
    fun getBackdropImageUrl(
        item: BaseItemDto,
        maxWidth: Int = 1280,
        maxHeight: Int = 720,
        quality: Int = 96
    ): String? {
        return getImageUrl(item, ImageType.BACKDROP, maxWidth, maxHeight, quality)
    }
    
    /**
     * Get primary image URL with specific sizing
     */
    fun getPrimaryImageUrl(
        item: BaseItemDto,
        maxWidth: Int = 400,
        maxHeight: Int = 600,
        quality: Int = 96
    ): String? {
        return getImageUrl(item, ImageType.PRIMARY, maxWidth, maxHeight, quality)
    }
    
    /**
     * Get thumbnail image URL
     */
    fun getThumbImageUrl(
        item: BaseItemDto,
        maxWidth: Int = 560,
        maxHeight: Int = 315,
        quality: Int = 96
    ): String? {
        return getImageUrl(item, ImageType.THUMB, maxWidth, maxHeight, quality)
    }
}
