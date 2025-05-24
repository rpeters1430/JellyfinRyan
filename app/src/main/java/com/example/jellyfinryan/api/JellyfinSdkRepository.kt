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
 * Enhanced Jellyfin Repository using the official Jellyfin SDK with SSL bypass support
 * This provides proper image URL generation and handles self-signed certificates
 */
@Singleton
class JellyfinSdkRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var enhancedSdkService: EnhancedJellyfinSdkService? = null
    private var jellyfin: Jellyfin? = null
    private var apiClient: ApiClient? = null
    private var isInitialized = false
    private var serverUrl: String = ""
    private var userId: String = ""
      /**
     * Format a 32-character UUID string to proper UUID format with hyphens
     */
    private fun formatUuid(uuidString: String): java.util.UUID {
        val cleanUuid = uuidString.replace("-", "")
        if (cleanUuid.length != 32) {
            throw IllegalArgumentException("UUID string must be 32 characters long, got: '${uuidString}' (${cleanUuid.length} chars)")
        }
        
        val formattedUuid = "${cleanUuid.substring(0, 8)}-${cleanUuid.substring(8, 12)}-${cleanUuid.substring(12, 16)}-${cleanUuid.substring(16, 20)}-${cleanUuid.substring(20, 32)}"
        Log.d("JellyfinSdkRepository", "Formatted UUID from '$uuidString' to '$formattedUuid'")
        return java.util.UUID.fromString(formattedUuid)
    }    /**
     * Initialize the SDK with server credentials and SSL bypass support
     */
    // In your JellyfinSdkRepository.kt, modify the initialize method:

    /**
     * Initialize the SDK with server credentials and SSL bypass support
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

            // 🚨 SKIP ENHANCED SDK FOR NOW - COMMENT OUT THIS SECTION:
            /*
            // Initialize the enhanced SDK service with SSL bypass
            Log.d("JellyfinSdkRepository", "Creating EnhancedJellyfinSdkService...")
            enhancedSdkService = EnhancedJellyfinSdkService(context)

            // Initialize the enhanced SDK service
            val enhancedInitialized = enhancedSdkService!!.initialize(this.serverUrl, accessToken, this.userId)
            if (!enhancedInitialized) {
                Log.w("JellyfinSdkRepository", "Enhanced SDK initialization failed")
                // Continue anyway - standard SDK might still work
            }
            */

            // Initialize the standard SDK ONLY
            Log.d("JellyfinSdkRepository", "Creating standard Jellyfin SDK instance...")
            jellyfin = Jellyfin(
                JellyfinOptions.Builder().apply {
                    clientInfo = ClientInfo(
                        name = "JellyfinRyan",
                        version = "1.0"
                    )
                    context = this@JellyfinSdkRepository.context
                }.build()
            )

            Log.d("JellyfinSdkRepository", "Creating API client for server: ${this.serverUrl}")
            apiClient = jellyfin!!.createApi(
                baseUrl = this.serverUrl,
                accessToken = accessToken
            )

            isInitialized = true
            Log.d("JellyfinSdkRepository", "Standard SDK initialized successfully")
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
    fun isAvailable(): Boolean = isInitialized && (apiClient != null || enhancedSdkService != null)
    
    /**
     * Get the current server URL
     */
    fun getServerUrl(): String = serverUrl
      /**
     * Get proper image URL using the official SDK with enhanced fallback
     * This is the key - using the SDK's built-in image URL generation
     */
    fun getImageUrl(
        itemId: String,
        imageType: ImageType = ImageType.PRIMARY,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        quality: Int = 96
    ): String? {
        if (!isAvailable()) {
            return null
        }
        
        // Try standard SDK first
        try {
            val sdkUrl = apiClient?.imageApi?.getItemImageUrl(
                itemId = formatUuid(itemId),
                imageType = imageType,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                quality = quality
            )
            if (sdkUrl != null) {
                return sdkUrl
            }
        } catch (e: Exception) {
            Log.w("JellyfinSdkRepository", "Standard SDK failed for image URL, trying enhanced service: ${e.message}")
        }
          // Fallback to enhanced service with SSL bypass
        return try {
            enhancedSdkService?.getImageUrl(
                itemId = itemId,
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
     * Get recent items using SDK with enhanced fallback
     */
    fun getRecentItems(limit: Int = 20): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }

        // Try standard SDK first
        try {
            val response = apiClient?.userLibraryApi?.getLatestMedia(
                userId = formatUuid(userId),
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
            return@flow
        } catch (e: Exception) {
            Log.w("JellyfinSdkRepository", "Standard SDK failed for recent items, trying enhanced service: ${e.message}")
        }
          // Fallback to enhanced service
        try {
            val baseItems = enhancedSdkService?.getRecentItems(userId, limit) ?: emptyList()
            val items = baseItems.map { baseItem ->
                JellyfinItem(
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
            Log.d("JellyfinSdkRepository", "Enhanced service returned ${items.size} recent items")
            emit(items)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Enhanced service also failed for recent items: ${e.message}")
            emit(emptyList())
        }
    }
      /**
     * Get featured items for carousel using SDK with enhanced fallback
     */
    fun getFeaturedItems(limit: Int = 10): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }

        // Try standard SDK first
        try {
            val response = apiClient?.itemsApi?.getItems(
                userId = formatUuid(userId),
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
            
            Log.d("JellyfinSdkRepository", "getFeaturedItems API response: ${response?.content?.items?.size ?: 0} items")
            
            val items = response?.content?.items?.map { baseItem ->
                convertSdkItemToJellyfinItem(baseItem)
            } ?: emptyList()
            
            Log.d("JellyfinSdkRepository", "SDK returned ${items.size} featured items")
            emit(items)
            return@flow
        } catch (e: Exception) {
            Log.w("JellyfinSdkRepository", "Standard SDK failed for featured items, trying enhanced service: ${e.message}")
        }
          // Fallback to enhanced service
        try {
            val baseItems = enhancedSdkService?.getFeaturedItems(userId, limit) ?: emptyList()
            val items = baseItems.map { baseItem ->
                JellyfinItem(
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
            Log.d("JellyfinSdkRepository", "Enhanced service returned ${items.size} featured items")
            emit(items)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Enhanced service also failed for featured items: ${e.message}")
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
                userId = formatUuid(userId)
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
     * Get user views (libraries) with enhanced fallback - alias for getLibraries to match ViewModel expectations
     */
    fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        // Try standard SDK first
        try {
            Log.d("JellyfinSdkRepository", "Making getUserViews API call with userId: ${userId}")
            val response = apiClient?.userViewsApi?.getUserViews(
                userId = formatUuid(userId)
            )
            
            Log.d("JellyfinSdkRepository", "getUserViews API response: ${response?.content?.items?.size ?: 0} items")
            
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
            return@flow
        } catch (e: Exception) {
            Log.w("JellyfinSdkRepository", "Standard SDK failed for user views, trying enhanced service: ${e.message}")
        }
          // Fallback to enhanced service
        try {
            val baseItems = enhancedSdkService?.getUserViews(userId) ?: emptyList()
            val libraries = baseItems.map { baseItem ->
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
            }
            Log.d("JellyfinSdkRepository", "Enhanced service returned ${libraries.size} user views")
            emit(libraries)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Enhanced service also failed for user views: ${e.message}")
            emit(emptyList())
        }
    }
      /**
     * Get items for a specific library with enhanced fallback
     */
    fun getLibraryItems(libraryId: String, limit: Int = 50): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }

        // Try standard SDK first
        try {
            Log.d("JellyfinSdkRepository", "Making getLibraryItems API call with userId: ${userId}")
            val response = apiClient?.itemsApi?.getItems(
                userId = formatUuid(userId),
                parentId = formatUuid(libraryId),
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
            return@flow
        } catch (e: Exception) {
            Log.w("JellyfinSdkRepository", "Standard SDK failed for library items, trying enhanced service: ${e.message}")
        }
          // Fallback to enhanced service
        try {
            val baseItems = enhancedSdkService?.getLibraryItems(userId, libraryId, limit) ?: emptyList()
            val items = baseItems.map { baseItem ->
                JellyfinItem(
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
            Log.d("JellyfinSdkRepository", "Enhanced service returned ${items.size} items for library $libraryId")
            emit(items)
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Enhanced service also failed for library items: ${e.message}")
            emit(emptyList())
        }
    }
    
    /**
     * Get recently added items for a specific library
     */    fun getRecentlyAddedForLibrary(libraryId: String, limit: Int = 20): Flow<List<JellyfinItem>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        try {
            val response = apiClient?.userLibraryApi?.getLatestMedia(
                userId = formatUuid(userId),
                parentId = formatUuid(libraryId),
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

    /**
     * Test connectivity with both standard SDK and enhanced service
     * This method helps debug SSL certificate issues
     */
    suspend fun testConnectivity(accessToken: String): Boolean {
        if (!isAvailable()) {
            Log.w("JellyfinSdkRepository", "Repository not initialized")
            return false
        }
        
        Log.d("JellyfinSdkRepository", "Testing connectivity to server: $serverUrl")
          // Test enhanced service first (with SSL bypass)
        val enhancedResult = try {
            enhancedSdkService?.testConnectivity() ?: false
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Enhanced service connectivity test failed: ${e.message}")
            false
        }
        
        // Test standard SDK
        val sdkResult = try {
            val response = apiClient?.userViewsApi?.getUserViews(
                userId = formatUuid(userId)
            )
            response?.content?.items?.isNotEmpty() == true
        } catch (e: Exception) {
            Log.e("JellyfinSdkRepository", "Standard SDK connectivity test failed: ${e.message}")
            false
        }
        
        Log.d("JellyfinSdkRepository", "Connectivity test results - Enhanced: $enhancedResult, SDK: $sdkResult")
        return enhancedResult || sdkResult
    }
}
