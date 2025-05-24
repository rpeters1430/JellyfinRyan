package com.example.jellyfinryan.api

import android.content.Context
import android.util.Log
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.model.JellyfinLibrary
import com.example.jellyfinryan.util.NetworkUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Jellyfin Repository using the official Jellyfin SDK
 * This provides proper image URL generation that working Jellyfin clients use
 */
@Singleton
class JellyfinSdkRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
      private var jellyfin: Jellyfin? = null
    private var apiClient: ApiClient? = null
    private var isInitialized = false
    private var serverUrl: String = ""
    private var userId: String = ""
      /**
     * Initialize the SDK with server credentials
     */
    suspend fun initialize(serverUrl: String, accessToken: String, userId: String): Boolean {
        return try {
            Log.d("JellyfinSdkRepository", "Starting SDK initialization...")
            
            // Check network connectivity first
            NetworkUtil.logNetworkStatus(context)
            if (!NetworkUtil.isNetworkAvailable(context)) {
                Log.w("JellyfinSdkRepository", "No network connectivity available")
                return false
            }
              this.serverUrl = serverUrl.removeSuffix("/")
            this.userId = userId
            
            Log.d("JellyfinSdkRepository", "Creating Jellyfin instance with ClientInfo")
            jellyfin = Jellyfin(
                JellyfinOptions.Builder().apply {
                    clientInfo = ClientInfo(
                        name = "JellyfinRyan",
                        version = "1.0"
                    )
                }.build()
            )
            
            Log.d("JellyfinSdkRepository", "Creating API client for server: ${this.serverUrl}")
            apiClient = jellyfin!!.createApi(
                baseUrl = this.serverUrl,
                accessToken = accessToken
            )
            
            isInitialized = true
            Log.d("JellyfinSdkRepository", "SDK initialized successfully")
            true
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Failed to initialize SDK: ${e.message}", e)
            isInitialized = false
            false
        }
    }
    
    /**
     * Check if SDK is available and initialized
     */
    fun isAvailable(): Boolean = isInitialized && apiClient != null && jellyfin != null
    
    /**
     * Get the current server URL
     */
    fun getServerUrl(): String = serverUrl
    
    /**
     * Get proper image URL using the official SDK
     * This is the key - using the SDK's built-in image URL generation
     */
    fun getImageUrl(
        itemId: String,
        imageType: ImageType = ImageType.PRIMARY,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        quality: Int = 96
    ): String? {
        return if (!isAvailable()) {
            null        } else {
            try {
                apiClient?.imageApi?.getItemImageUrl(
                    itemId = java.util.UUID.fromString(itemId),
                    imageType = imageType,
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    quality = quality
                )
            } catch (e: Exception) {
                Log.e("JellyfinSdkRepository", "Failed to get image URL for item $itemId", e)
                null
            }
        }
    }
    
    /**
     * Enhanced image URL methods for different UI contexts
     */
    fun getPrimaryImageUrl(itemId: String, width: Int = 267, height: Int = 400): String? {
        return getImageUrl(itemId, ImageType.PRIMARY, width, height, 96)
    }
    
    fun getBackdropImageUrl(itemId: String, width: Int = 1280, height: Int = 720): String? {
        return getImageUrl(itemId, ImageType.BACKDROP, width, height, 96)
    }
    
    fun getHorizontalImageUrl(itemId: String, width: Int = 560, height: Int = 315): String? {
        // Try backdrop first for horizontal layout
        getBackdropImageUrl(itemId, width, height)?.let { return it }
        
        // Try thumb
        getImageUrl(itemId, ImageType.THUMB, width, height, 96)?.let { return it }
        
        // Fallback to primary with horizontal sizing
        return getPrimaryImageUrl(itemId, width, height)
    }
    
    fun getFeaturedCarouselImageUrl(itemId: String): String? {
        return getBackdropImageUrl(itemId, 1280, 720) 
            ?: getPrimaryImageUrl(itemId, 1280, 720)
    }
    
    /**
     * Get recent items using SDK
     */
    fun getRecentItems(limit: Int = 20): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        try {            val response = apiClient?.userLibraryApi?.getLatestMedia(
                userId = java.util.UUID.fromString(userId),
                limit = limit,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SERIES_PRIMARY_IMAGE,
                    ItemFields.OVERVIEW,
                    ItemFields.GENRES,
                    ItemFields.DATE_CREATED,
                    ItemFields.TAGS,
                    ItemFields.PARENT_ID
                ),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES, BaseItemKind.EPISODE),
                enableImages = true
            )
            
            val items = response?.content?.map { baseItem -> 
                convertSdkItemToJellyfinItem(baseItem)
            } ?: emptyList()
            
            Log.d("JellyfinSdkRepository", "SDK returned ${items.size} recent items")
            emit(items)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Failed to get recent items from SDK", e)
            emit(emptyList())
        }
    }
    
    /**
     * Get featured items for carousel using SDK
     */
    fun getFeaturedItems(limit: Int = 10): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        try {            val response = apiClient?.itemsApi?.getItems(
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
                    ItemFields.PARENT_ID
                ),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                sortBy = listOf(ItemSortBy.DATE_CREATED),
                sortOrder = listOf(SortOrder.DESCENDING),
                enableImages = true,
                imageTypeLimit = 3
            )
            
            val items = response?.content?.items?.map { baseItem -> 
                convertSdkItemToJellyfinItem(baseItem)
            } ?: emptyList()
            
            Log.d("JellyfinSdkRepository", "SDK returned ${items.size} featured items")
            emit(items)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Failed to get featured items from SDK", e)
            emit(emptyList())
        }
    }
    
    /**
     * Get libraries using SDK
     */
    fun getLibraries(): Flow<List<JellyfinLibrary>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        try {            val response = apiClient?.userViewsApi?.getUserViews(
                userId = java.util.UUID.fromString(userId)
            )
              val libraries = response?.content?.items?.map { baseItem ->
                JellyfinLibrary(
                    Id = baseItem.id.toString(),
                    Name = baseItem.name ?: "",
                    CollectionType = baseItem.collectionType?.toString(),
                    PrimaryImageItemId = baseItem.id.toString(),
                    PrimaryImageTag = null, // Not used with SDK
                    ImageTags = null // Not used with SDK
                )
            } ?: emptyList()
            
            Log.d("JellyfinSdkRepository", "SDK returned ${libraries.size} libraries")
            emit(libraries)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Failed to get libraries from SDK", e)
            emit(emptyList())
        }
    }
    
    /**
     * Get user views (libraries) - alias for getLibraries to match ViewModel expectations
     */
    fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        try {
            val response = apiClient?.userViewsApi?.getUserViews(
                userId = java.util.UUID.fromString(userId)
            )
            
            val libraries = response?.content?.items?.map { baseItem ->
                JellyfinItem(
                    Id = baseItem.id.toString(),
                    Name = baseItem.name ?: "",
                    Type = baseItem.type?.toString() ?: "UserView",
                    PrimaryImageTag = null, // Not needed with SDK
                    Overview = baseItem.overview,
                    PremiereDate = null,
                    CommunityRating = null,
                    OfficialRating = null,
                    RunTimeTicks = null,
                    ImageTags = null, // Not needed with SDK
                    ParentId = baseItem.parentId?.toString(),
                    BackdropImageTags = null, // Not needed with SDK
                    SeriesPrimaryImageTag = null, // Not needed with SDK
                    ParentPrimaryImageTag = null, // Not needed with SDK
                    ParentBackdropImageTags = null, // Not needed with SDK
                    ThumbImageTags = null, // Not needed with SDK
                    ScreenshotImageTags = null, // Not needed with SDK
                    ProductionYear = baseItem.productionYear,
                    ParentThumbImageTag = null, // Not needed with SDK
                    SeriesThumbImageTag = null // Not needed with SDK
                )
            } ?: emptyList()
            
            Log.d("JellyfinSdkRepository", "SDK returned ${libraries.size} user views")
            emit(libraries)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Failed to get user views from SDK", e)
            emit(emptyList())
        }
    }
    
    /**
     * Get items for a specific library
     */
    fun getLibraryItems(libraryId: String, limit: Int = 50): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        try {
            val response = apiClient?.itemsApi?.getItems(
                userId = java.util.UUID.fromString(userId),
                parentId = java.util.UUID.fromString(libraryId),
                limit = limit,
                recursive = true,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SERIES_PRIMARY_IMAGE,
                    ItemFields.OVERVIEW,
                    ItemFields.GENRES,
                    ItemFields.DATE_CREATED,
                    ItemFields.TAGS,
                    ItemFields.PARENT_ID
                ),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES, BaseItemKind.EPISODE),
                sortBy = listOf(ItemSortBy.SORT_NAME),
                sortOrder = listOf(SortOrder.ASCENDING),
                enableImages = true
            )
            
            val items = response?.content?.items?.map { baseItem -> 
                convertSdkItemToJellyfinItem(baseItem)
            } ?: emptyList()
            
            Log.d("JellyfinSdkRepository", "SDK returned ${items.size} items for library $libraryId")
            emit(items)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Failed to get library items from SDK", e)
            emit(emptyList())
        }
    }
    
    /**
     * Get recently added items for a specific library
     */
    fun getRecentlyAddedForLibrary(libraryId: String, limit: Int = 20): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        try {
            val response = apiClient?.userLibraryApi?.getLatestMedia(
                userId = java.util.UUID.fromString(userId),
                parentId = java.util.UUID.fromString(libraryId),
                limit = limit,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SERIES_PRIMARY_IMAGE,
                    ItemFields.OVERVIEW,
                    ItemFields.GENRES,
                    ItemFields.DATE_CREATED,
                    ItemFields.TAGS,
                    ItemFields.PARENT_ID
                ),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES, BaseItemKind.EPISODE),
                enableImages = true
            )
            
            val items = response?.content?.map { baseItem -> 
                convertSdkItemToJellyfinItem(baseItem)
            } ?: emptyList()
            
            Log.d("JellyfinSdkRepository", "SDK returned ${items.size} recently added items for library $libraryId")
            emit(items)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Failed to get recently added items from SDK", e)
            emit(emptyList())
        }
    }

    /**
     * Convert SDK BaseItemDto to our JellyfinItem model
     * The key advantage: we can now use SDK-generated image URLs
     */
    private fun convertSdkItemToJellyfinItem(baseItem: BaseItemDto): JellyfinItem {
        return JellyfinItem(
            Id = baseItem.id.toString(),
            Name = baseItem.name ?: "",
            Type = baseItem.type?.toString() ?: "",
            PrimaryImageTag = null, // Not needed with SDK
            Overview = baseItem.overview,
            PremiereDate = baseItem.premiereDate?.toString(),
            CommunityRating = baseItem.communityRating?.toFloat(),
            OfficialRating = baseItem.officialRating,
            RunTimeTicks = baseItem.runTimeTicks,
            ImageTags = null, // Not needed with SDK
            ParentId = baseItem.parentId?.toString(),
            BackdropImageTags = null, // Not needed with SDK
            SeriesPrimaryImageTag = null, // Not needed with SDK
            ParentPrimaryImageTag = null, // Not needed with SDK
            ParentBackdropImageTags = null, // Not needed with SDK
            ThumbImageTags = null, // Not needed with SDK
            ScreenshotImageTags = null, // Not needed with SDK
            ProductionYear = baseItem.productionYear,
            ParentThumbImageTag = null, // Not needed with SDK
            SeriesThumbImageTag = null // Not needed with SDK
        )
    }
}
