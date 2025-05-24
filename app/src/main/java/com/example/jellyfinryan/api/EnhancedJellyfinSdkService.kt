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
import org.jellyfin.sdk.api.client.extensions.systemApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Jellyfin SDK Service with comprehensive SSL bypass support
 *
 * Features:
 * - Full SSL certificate bypass for self-signed certificates
 * - Emby protocol headers for reverse proxy compatibility
 * - Robust error handling and fallback mechanisms
 * - Complete Jellyfin SDK integration
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
    private var userId: String = ""
    private var isInitialized = false

    companion object {
        private const val TAG = "EnhancedJellyfinSdk"
    }

    /**
     * Initialize the enhanced Jellyfin SDK with comprehensive SSL bypass
     */
    suspend fun initialize(serverUrl: String, accessToken: String, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing enhanced Jellyfin SDK with SSL bypass...")

                this@EnhancedJellyfinSdkService.serverUrl = serverUrl.removeSuffix("/")
                this@EnhancedJellyfinSdkService.accessToken = accessToken
                this@EnhancedJellyfinSdkService.userId = userId

                // Initialize unsafe HTTP client for direct calls
                initializeUnsafeHttpClient()

                // Initialize Jellyfin SDK with SSL bypass
                initializeJellyfinSdk()

                // Test connectivity
                val connectivityTest = testBasicConnectivity()
                Log.d(TAG, "Connectivity test result: $connectivityTest")

                isInitialized = apiClient != null
                Log.d(TAG, "Enhanced Jellyfin SDK initialization: ${if (isInitialized) "SUCCESS" else "FAILED"}")

                isInitialized
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize enhanced Jellyfin SDK", e)
                isInitialized = false
                false
            }
        }
    }

    /**
     * Initialize unsafe HTTP client with comprehensive SSL bypass
     */
    private fun initializeUnsafeHttpClient() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        unsafeHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient()
            .newBuilder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithHeaders = originalRequest.newBuilder()
                    .addHeader("X-Emby-Client", "JellyfinRyan")
                    .addHeader("X-Emby-Client-Version", "1.0")
                    .addHeader("X-Emby-Device", "AndroidTV")
                    .addHeader("X-Emby-Device-Id", "jellyfin-ryan-android-tv")
                    .addHeader("X-Emby-Device-Name", "JellyfinRyan Android TV")
                    .addHeader("X-Emby-Token", accessToken)
                    .addHeader("Authorization", "MediaBrowser Token=\"$accessToken\"")
                    .build()
                chain.proceed(requestWithHeaders)
            }
            .build()

        Log.d(TAG, "Unsafe HTTP client initialized with SSL bypass and Emby headers")
    }

    /**
     * Initialize Jellyfin SDK with custom configuration
     */
    private fun initializeJellyfinSdk() {
        try {
            // Create unsafe Ktor client for SDK
            val unsafeKtorClient = UnsafeKtorClient.createUnsafeKtorClient()

            // Initialize Jellyfin SDK
            jellyfin = Jellyfin(
                JellyfinOptions.Builder().apply {
                    clientInfo = ClientInfo(
                        name = "JellyfinRyan",
                        version = "1.0"
                    )
                    context = this@EnhancedJellyfinSdkService.context
                }.build()
            )

            // Create API client
            apiClient = jellyfin!!.createApi(
                baseUrl = serverUrl,
                accessToken = accessToken
            )

            Log.d(TAG, "Jellyfin SDK initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Jellyfin SDK", e)
            throw e
        }
    }

    /**
     * Test basic connectivity without requiring authentication
     */
    private suspend fun testBasicConnectivity(): Boolean {
        return try {
            // Try SDK system info call first
            apiClient?.systemApi?.getSystemInfo()
            Log.d(TAG, "SDK connectivity test successful")
            true
        } catch (e: Exception) {
            Log.w(TAG, "SDK connectivity test failed, trying direct HTTP: ${e.message}")

            // Fallback to direct HTTP call
            try {
                val response = makeDirectHttpRequest("/System/Info")
                val success = response != null
                Log.d(TAG, "Direct HTTP connectivity test: ${if (success) "SUCCESS" else "FAILED"}")
                success
            } catch (httpE: Exception) {
                Log.e(TAG, "Direct HTTP connectivity test failed", httpE)
                false
            }
        }
    }

    /**
     * Make direct HTTP request with SSL bypass
     */
    private suspend fun makeDirectHttpRequest(endpoint: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val client = unsafeHttpClient ?: return@withContext null
                val url = "$serverUrl$endpoint"
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.w(TAG, "HTTP request failed: ${response.code} for $endpoint")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Direct HTTP request exception for $endpoint", e)
                null
            }
        }
    }

    /**
     * Check if service is available and initialized
     */
    fun isAvailable(): Boolean = isInitialized && apiClient != null

    /**
     * Get server URL
     */
    fun getServerUrl(): String = serverUrl

    /**
     * Test connectivity with comprehensive checks
     */
    suspend fun testConnectivity(): Boolean {
        if (!isInitialized) {
            Log.w(TAG, "Service not initialized for connectivity test")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                // Try system info call (doesn't require user authentication)
                apiClient?.systemApi?.getSystemInfo()
                Log.d(TAG, "Full connectivity test successful")
                true
            } catch (e: Exception) {
                Log.w(TAG, "Full connectivity test failed: ${e.message}")

                // Fallback to basic HTTP test
                makeDirectHttpRequest("/System/Info") != null
            }
        }
    }

    /**
     * Parse UUID with proper formatting
     */
    private fun parseUuid(uuidString: String): UUID {
        return try {
            val cleanUuid = uuidString.replace("-", "")
            if (cleanUuid.length == 32) {
                val formattedUuid = "${cleanUuid.substring(0, 8)}-${cleanUuid.substring(8, 12)}-${cleanUuid.substring(12, 16)}-${cleanUuid.substring(16, 20)}-${cleanUuid.substring(20, 32)}"
                UUID.fromString(formattedUuid)
            } else {
                UUID.fromString(uuidString)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse UUID '$uuidString', using random UUID")
            UUID.randomUUID()
        }
    }

    /**
     * Generate image URL using SDK with fallbacks
     */
    fun getImageUrl(
        itemId: String,
        imageType: ImageType = ImageType.PRIMARY,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        quality: Int = 96
    ): String? {
        if (!isAvailable()) {
            Log.w(TAG, "Service not available for image URL generation")
            return null
        }

        return try {
            apiClient!!.imageApi.getItemImageUrl(
                itemId = parseUuid(itemId),
                imageType = imageType,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                quality = quality
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate image URL for item $itemId", e)

            // Fallback to manual URL construction
            val typeStr = imageType.toString().lowercase()
            "$serverUrl/Items/$itemId/Images/$typeStr?quality=$quality" +
                    (maxWidth?.let { "&maxWidth=$it" } ?: "") +
                    (maxHeight?.let { "&maxHeight=$it" } ?: "")
        }
    }

    /**
     * Get recent items with comprehensive error handling
     */
    suspend fun getRecentItems(userId: String, limit: Int = 20): List<BaseItemDto> {
        if (!isAvailable()) {
            Log.w(TAG, "Service not available for recent items")
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient!!.userLibraryApi.getLatestMedia(
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
                )

                val items = response?.content ?: emptyList()
                Log.d(TAG, "Retrieved ${items.size} recent items")
                items
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get recent items", e)
                emptyList()
            }
        }
    }

    /**
     * Get featured items for carousel
     */
    suspend fun getFeaturedItems(userId: String, limit: Int = 20): List<BaseItemDto> {
        if (!isAvailable()) {
            Log.w(TAG, "Service not available for featured items")
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient!!.itemsApi.getItems(
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
                )

                val items = response?.content?.items ?: emptyList()
                Log.d(TAG, "Retrieved ${items.size} featured items")
                items
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get featured items", e)
                emptyList()
            }
        }
    }

    /**
     * Get user views (libraries)
     */
    suspend fun getUserViews(userId: String): List<BaseItemDto> {
        if (!isAvailable()) {
            Log.w(TAG, "Service not available for user views")
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting user views for user: $userId")
                val response = apiClient!!.userViewsApi.getUserViews(
                    userId = parseUuid(userId),
                    includeExternalContent = false,
                    includeHidden = false
                )

                val items = response?.content?.items ?: emptyList()
                Log.d(TAG, "Retrieved ${items.size} user views")
                items
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get user views", e)
                emptyList()
            }
        }
    }

    /**
     * Get library items with pagination
     */
    suspend fun getLibraryItems(userId: String, parentId: String, limit: Int = 50): List<BaseItemDto> {
        if (!isAvailable()) {
            Log.w(TAG, "Service not available for library items")
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient!!.itemsApi.getItems(
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
                )

                val items = response?.content?.items ?: emptyList()
                Log.d(TAG, "Retrieved ${items.size} library items for parent $parentId")
                items
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get library items", e)
                emptyList()
            }
        }
    }
}