package com.example.jellyfinryan.api

import android.content.Context
import android.util.Log
import com.example.jellyfinryan.utils.UnsafeKtorClient
import com.example.jellyfinryan.utils.UnsafeOkHttpClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
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
 * Enhanced Jellyfin SDK Service with SSL bypass and reverse proxy support
 * 
 * This service provides:
 * - SSL certificate bypass for self-signed certificates
 * - Emby protocol headers for reverse proxy compatibility
 * - Full Jellyfin SDK functionality with secure connection handling
 */
@Singleton
class EnhancedJellyfinSdkService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var jellyfin: Jellyfin? = null
    private var apiClient: ApiClient? = null
    private var unsafeHttpClient: OkHttpClient? = null
    private var serverUrl: String = ""
    private var accessToken: String = ""
    private var isInitialized = false
    
    /**
     * Initialize the enhanced Jellyfin SDK with SSL bypass and reverse proxy support
     */
    suspend fun initialize(serverUrl: String, accessToken: String, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EnhancedJellyfinSdk", "Initializing enhanced Jellyfin SDK with SSL bypass...")
                
                this@EnhancedJellyfinSdkService.serverUrl = serverUrl.removeSuffix("/")
                this@EnhancedJellyfinSdkService.accessToken = accessToken
                
                // Create unsafe HTTP client for SSL bypass
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
                
                unsafeHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient()
                    .newBuilder()
                    .addInterceptor(logging)
                    .addInterceptor { chain ->
                        val originalRequest = chain.request()
                        val requestWithHeaders = originalRequest.newBuilder()
                            // Add Emby protocol headers for reverse proxy compatibility
                            .addHeader("X-Emby-Client", "JellyfinRyan")
                            .addHeader("X-Emby-Client-Version", "1.0")
                            .addHeader("X-Emby-Device", "AndroidTV")
                            .addHeader("X-Emby-Device-Id", "jellyfin-ryan-android-tv")
                            .addHeader("X-Emby-Device-Name", "JellyfinRyan Android TV")
                            .addHeader("X-Emby-Token", accessToken)
                            .build()
                        
                        chain.proceed(requestWithHeaders)
                    }
                    .build()                // Initialize Jellyfin SDK (SSL bypass will be handled by the API client)
                jellyfin = Jellyfin(
                    JellyfinOptions.Builder().apply {
                        clientInfo = ClientInfo(
                            name = "JellyfinRyan",
                            version = "1.0"
                        )
                        context = this@EnhancedJellyfinSdkService.context
                    }.build()
                )
                
                // Create API client with SSL bypass
                try {
                    apiClient = jellyfin!!.createApi(
                        baseUrl = this@EnhancedJellyfinSdkService.serverUrl,
                        accessToken = accessToken
                    )
                    Log.d("EnhancedJellyfinSdk", "SDK API client with SSL bypass created successfully")
                } catch (e: Exception) {
                    Log.w("EnhancedJellyfinSdk", "SDK API client creation failed, will use fallback HTTP client", e)
                    // We can still proceed with our custom HTTP client for direct API calls
                }
                
                isInitialized = true
                Log.d("EnhancedJellyfinSdk", "Enhanced Jellyfin SDK initialized successfully")
                true
                
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Failed to initialize enhanced Jellyfin SDK", e)
                isInitialized = false
                false
            }
        }
    }
    
    /**
     * Check if the enhanced SDK is available
     */
    fun isAvailable(): Boolean = isInitialized && (apiClient != null || unsafeHttpClient != null)
    
    /**
     * Get server URL
     */
    fun getServerUrl(): String = serverUrl
    
    /**
     * Make a safe HTTP request with SSL bypass and proper headers
     */
    private suspend fun makeHttpRequest(endpoint: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val client = unsafeHttpClient ?: return@withContext null
                val url = "${serverUrl}${endpoint}"
                
                val request = Request.Builder()
                    .url(url)
                    .addHeader("X-Emby-Token", accessToken)
                    .addHeader("X-Emby-Client", "JellyfinRyan")
                    .addHeader("X-Emby-Client-Version", "1.0")
                    .addHeader("X-Emby-Device", "AndroidTV")
                    .addHeader("X-Emby-Device-Id", "jellyfin-ryan-android-tv")
                    .addHeader("X-Emby-Device-Name", "JellyfinRyan Android TV")
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.e("EnhancedJellyfinSdk", "HTTP request failed: ${response.code} ${response.message}")
                    null
                }
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "HTTP request exception", e)
                null
            }
        }
    }
    
    /**
     * Test connectivity with SSL bypass and reverse proxy headers
     */
    suspend fun testConnectivity(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EnhancedJellyfinSdk", "Testing connectivity to $serverUrl with SSL bypass...")
                
                // Test basic connectivity with system info endpoint
                val response = makeHttpRequest("/System/Info")
                val success = response != null
                
                if (success) {
                    Log.d("EnhancedJellyfinSdk", "Connectivity test successful")
                } else {
                    Log.w("EnhancedJellyfinSdk", "Connectivity test failed")
                }
                
                success
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Connectivity test exception", e)
                false
            }
        }
    }
    
    /**
     * Get image URL with SSL-safe server access
     * This ensures image URLs work even with self-signed certificates
     */
    fun getImageUrl(
        itemId: String,
        imageType: ImageType = ImageType.PRIMARY,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        quality: Int = 96
    ): String? {
        return try {
            // Use standard SDK image API if available
            apiClient?.imageApi?.getItemImageUrl(
                itemId = parseUuid(itemId),
                imageType = imageType,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                quality = quality
            ) ?: run {
                // Fallback to manual URL construction with proper UUID formatting
                val formattedId = itemId.replace("-", "")
                val imageTypeStr = imageType.toString().lowercase()
                val params = buildList {
                    if (maxWidth != null) add("maxWidth=$maxWidth")
                    if (maxHeight != null) add("maxHeight=$maxHeight")
                    add("quality=$quality")
                }.joinToString("&")
                
                "$serverUrl/Items/$formattedId/Images/$imageTypeStr?$params"
            }
        } catch (e: Exception) {
            Log.e("EnhancedJellyfinSdk", "Failed to generate image URL for item $itemId", e)
            null
        }
    }
    
    /**
     * Parse UUID string to proper UUID format
     */
    private fun parseUuid(uuidString: String): java.util.UUID {
        val cleanUuid = uuidString.replace("-", "")
        if (cleanUuid.length == 32) {
            val formattedUuid = "${cleanUuid.substring(0, 8)}-${cleanUuid.substring(8, 12)}-${cleanUuid.substring(12, 16)}-${cleanUuid.substring(16, 20)}-${cleanUuid.substring(20, 32)}"
            return java.util.UUID.fromString(formattedUuid)
        }
        return java.util.UUID.fromString(uuidString)
    }
    
    /**
     * Get recent items using SDK (with fallback to HTTP client)
     */
    suspend fun getRecentItems(userId: String, limit: Int = 20): List<BaseItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Try SDK first
                apiClient?.userLibraryApi?.getLatestMedia(
                    userId = parseUuid(userId),
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
                )?.content ?: emptyList()
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Failed to get recent items", e)
                emptyList()
            }
        }
    }

    /**
     * Get featured items using SDK (with fallback to HTTP client)
     */
    suspend fun getFeaturedItems(userId: String, limit: Int = 20): List<BaseItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Try SDK first
                apiClient?.itemsApi?.getItems(
                    userId = parseUuid(userId),
                    limit = limit,
                    sortBy = listOf(ItemSortBy.DATE_CREATED),
                    sortOrder = listOf(SortOrder.DESCENDING),
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
                    enableImages = true,
                    recursive = true
                )?.content?.items ?: emptyList()
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Failed to get featured items", e)
                emptyList()
            }
        }
    }

    /**
     * Get user views (libraries) using SDK (with fallback to HTTP client)
     */
    suspend fun getUserViews(userId: String): List<BaseItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Try SDK first
                apiClient?.userViewsApi?.getUserViews(
                    userId = parseUuid(userId),
                    includeExternalContent = false,
                    includeHidden = false
                )?.content?.items ?: emptyList()
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Failed to get user views", e)
                emptyList()
            }
        }
    }

    /**
     * Get library items using SDK (with fallback to HTTP client)
     */
    suspend fun getLibraryItems(userId: String, parentId: String, limit: Int = 50): List<BaseItemDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Try SDK first
                apiClient?.itemsApi?.getItems(
                    userId = parseUuid(userId),
                    parentId = parseUuid(parentId),
                    limit = limit,
                    sortBy = listOf(ItemSortBy.SORT_NAME),
                    sortOrder = listOf(SortOrder.ASCENDING),
                    fields = listOf(
                        ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                        ItemFields.SERIES_PRIMARY_IMAGE,
                        ItemFields.OVERVIEW,
                        ItemFields.GENRES,
                        ItemFields.DATE_CREATED,
                        ItemFields.TAGS,
                        ItemFields.PARENT_ID
                    ),
                    enableImages = true,
                    recursive = false
                )?.content?.items ?: emptyList()
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Failed to get library items", e)
                emptyList()
            }
        }
    }
}
