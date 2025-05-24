package com.example.jellyfinryan.api

import android.content.Context
import android.util.Log
// Import your UnsafeKtorClient
import com.example.jellyfinryan.utils.UnsafeKtorClient //
// Import UnsafeOkHttpClient for the unsafeHttpClient property
import com.example.jellyfinryan.utils.UnsafeOkHttpClient //
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient // For the unsafeHttpClient property
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
import java.util.UUID // For parseUuid
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Jellyfin SDK Service with SSL bypass and reverse proxy support
 *
 * This service provides:
 * - SSL certificate bypass for self-signed certificates (by injecting a custom Ktor client into the SDK)
 * - Emby protocol headers for reverse proxy compatibility (via a separate OkHttpClient interceptor for direct calls)
 * - Full Jellyfin SDK functionality with secure connection handling
 */
@Singleton
class EnhancedJellyfinSdkService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var jellyfin: Jellyfin? = null
    private var apiClient: ApiClient? = null // This will be configured with UnsafeKtorClient
    private var unsafeHttpClient: OkHttpClient? = null // Your existing OkHttpClient for direct calls
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

                // Configure unsafeHttpClient (OkHttp) for direct calls if needed (e.g., makeHttpRequest)
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
                            .build()
                        chain.proceed(requestWithHeaders)
                    }
                    .build()

                // Create the unsafe Ktor HttpClient that the Jellyfin SDK will use
                val unsafeKtorHttpClientForSdk = UnsafeKtorClient.createUnsafeKtorClient()

                // Initialize Jellyfin SDK core, providing the custom Ktor client via options
                jellyfin = Jellyfin(
                    JellyfinOptions.Builder().apply {
                        clientInfo = ClientInfo(
                            name = "JellyfinRyan",
                            version = "1.0"
                        )
                        context = this@EnhancedJellyfinSdkService.context
                        // *** THIS IS THE KEY CHANGE FOR SDK 1.6.8 ***
                        customKtorClient = unsafeKtorHttpClientForSdk
                    }.build()
                )

                // Create the ApiClient. It will now use the customKtorClient provided in options.
                // The `client` parameter is REMOVED from this call.
                try {
                    apiClient = jellyfin!!.createApi(
                        baseUrl = this@EnhancedJellyfinSdkService.serverUrl,
                        accessToken = accessToken
                        // NO client = ... parameter here for SDK 1.6.8
                    )
                    Log.d("EnhancedJellyfinSdk", "SDK ApiClient created. It should use the custom Ktor client from JellyfinOptions.")
                } catch (e: Exception) {
                    Log.e("EnhancedJellyfinSdk", "Failed to create SDK ApiClient", e)
                    // apiClient will remain null if this fails
                }

                isInitialized = apiClient != null
                if (isInitialized) {
                    Log.d("EnhancedJellyfinSdk", "Enhanced Jellyfin SDK initialized successfully.")
                } else {
                    Log.e("EnhancedJellyfinSdk", "Enhanced Jellyfin SDK initialization failed (ApiClient not created).")
                }
                isInitialized

            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Overall failure in enhanced Jellyfin SDK initialization", e)
                isInitialized = false
                false
            }
        }
    }
    /**
     * Check if the enhanced SDK is available
     * For SDK functions, apiClient must be non-null.
     * For direct makeHttpRequest, unsafeHttpClient must be non-null.
     */
    fun isAvailable(): Boolean = isInitialized // Relies on apiClient being successfully initialized

    /**
     * Get server URL
     */
    fun getServerUrl(): String = serverUrl

    /**
     * Make a direct HTTP request using the separately configured unsafe OkHttpClient.
     * This is a fallback or for endpoints not covered by the SDK's ApiClient.
     */
    private suspend fun makeHttpRequest(endpoint: String): String? {
        if (unsafeHttpClient == null) {
            Log.e("EnhancedJellyfinSdk", "unsafeHttpClient is not initialized. Cannot make HTTP request.")
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                val client = unsafeHttpClient!!
                val url = "${serverUrl}${endpoint}"

                val request = Request.Builder()
                    .url(url)
                    // Headers are already added by the interceptor in unsafeHttpClient
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.e("EnhancedJellyfinSdk", "HTTP request via makeHttpRequest failed: ${response.code} ${response.message} for URL: $url")
                    null
                }
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "HTTP request exception in makeHttpRequest", e)
                null
            }
        }
    }

    /**
     * Test connectivity (preferably using an SDK call if apiClient is initialized)
     */
    suspend fun testConnectivity(): Boolean {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not fully initialized for testConnectivity via SDK. Trying direct HTTP.")
            return makeHttpRequest("/System/Info") != null
        }
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EnhancedJellyfinSdk", "Testing connectivity via SDK apiClient to $serverUrl...")
                // Try a lightweight SDK call, e.g., get public system info if available or a similar benign call
                // For now, using getUserViews as a test, ensure userId is valid or use a public endpoint
                // As a simple test, just check if apiClient is not null after initialization.
                // A more robust test would be a lightweight API call.
                val systemInfo = apiClient?.userViewsApi?.getUserViews(userId = UUID.randomUUID()) // Using a random UUID as placeholder for a generic check
                val success = systemInfo != null // Or check response.isSuccess if the call returns a Response object

                if (success) {
                    Log.d("EnhancedJellyfinSdk", "Connectivity test via SDK apiClient successful.")
                } else {
                    Log.w("EnhancedJellyfinSdk", "Connectivity test via SDK apiClient failed or returned no data.")
                }
                success
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Connectivity test via SDK apiClient exception", e)
                false
            }
        }
    }

    /**
     * Parse UUID string to proper UUID format. Handles strings with or without hyphens.
     */
    private fun parseUuid(uuidString: String): java.util.UUID {
        val cleanUuid = uuidString.replace("-", "")
        if (cleanUuid.length == 32) {
            return try {
                val formattedUuid = "${cleanUuid.substring(0, 8)}-${cleanUuid.substring(8, 12)}-${cleanUuid.substring(12, 16)}-${cleanUuid.substring(16, 20)}-${cleanUuid.substring(20, 32)}"
                java.util.UUID.fromString(formattedUuid)
            } catch (e: IllegalArgumentException) {
                Log.w("EnhancedJellyfinSdk", "Could not parse '$uuidString' as a 32-char UUID, trying direct parse.")
                java.util.UUID.fromString(uuidString) // Fallback for already formatted or other UUID forms
            }
        }
        // If not 32 chars after cleaning, try to parse directly, might throw error which is fine
        return java.util.UUID.fromString(uuidString)
    }

    /**
     * Get image URL using the SDK's apiClient.
     */
    fun getImageUrl(
        itemId: String,
        imageType: ImageType = ImageType.PRIMARY,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        quality: Int = 96
    ): String? {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not initialized or apiClient is null. Cannot get image URL.")
            return null // Or fallback to manual construction if absolutely necessary
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
            Log.e("EnhancedJellyfinSdk", "Failed to generate image URL for item $itemId using SDK apiClient", e)
            null
        }
    }

    // SDK-dependent methods using apiClient
    suspend fun getRecentItems(userId: String, limit: Int = 20): List<BaseItemDto> {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not initialized or apiClient is null. Cannot get recent items.")
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            try {
                apiClient!!.userLibraryApi.getLatestMedia(
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
                Log.e("EnhancedJellyfinSdk", "Failed to get recent items using SDK apiClient", e)
                emptyList()
            }
        }
    }

    suspend fun getFeaturedItems(userId: String, limit: Int = 20): List<BaseItemDto> {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not initialized or apiClient is null. Cannot get featured items.")
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            try {
                apiClient!!.itemsApi.getItems(
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
                Log.e("EnhancedJellyfinSdk", "Failed to get featured items using SDK apiClient", e)
                emptyList()
            }
        }
    }

    suspend fun getUserViews(userId: String): List<BaseItemDto> {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not initialized or apiClient is null. Cannot get user views.")
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EnhancedJellyfinSdk", "Attempting to get user views with SDK apiClient for user: $userId")
                apiClient!!.userViewsApi.getUserViews(
                    userId = parseUuid(userId),
                    includeExternalContent = false, // Optional: set as needed
                    includeHidden = false         // Optional: set as needed
                )?.content?.items ?: emptyList<BaseItemDto>().also {
                    Log.d("EnhancedJellyfinSdk", "getUserViews returned null or empty items from SDK.")
                }
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Failed to get user views using SDK apiClient. Error: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getLibraryItems(userId: String, parentId: String, limit: Int = 50): List<BaseItemDto> {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not initialized or apiClient is null. Cannot get library items.")
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            try {
                apiClient!!.itemsApi.getItems(
                    userId = parseUuid(userId),
                    parentId = parseUuid(parentId), // Assuming parentId is a library ID
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
                    recursive = false // Typically false for items directly under a library view
                )?.content?.items ?: emptyList()
            } catch (e: Exception) {
                Log.e("EnhancedJellyfinSdk", "Failed to get library items using SDK apiClient", e)
                emptyList()
            }
        }
    }
}
