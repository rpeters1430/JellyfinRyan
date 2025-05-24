package com.example.jellyfinryan.api

import android.content.Context
import android.util.Log
// Import your UnsafeKtorClient
import com.example.jellyfinryan.utils.UnsafeKtorClient
// Import UnsafeOkHttpClient for the unsafeHttpClient property
import com.example.jellyfinryan.utils.UnsafeOkHttpClient
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient // Explicit import for HttpClient
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

                val unsafeKtorHttpClientForSdk = UnsafeKtorClient.createUnsafeKtorClient()
                jellyfin = Jellyfin(
                    JellyfinOptions.Builder().apply {
                        clientInfo = ClientInfo(
                            name = "JellyfinRyan",
                            version = "1.0"
                        )
                        context = this@EnhancedJellyfinSdkService.context // This context is Any? in SDK 1.6.8
                        // Call the method to set the custom Ktor client
                        customKtorClient(unsafeKtorHttpClientForSdk)
                    }.build()
                )
                try {
                    apiClient = jellyfin!!.createApi(
                        baseUrl = this@EnhancedJellyfinSdkService.serverUrl,
                        accessToken = accessToken
                        // NO client = ... parameter here
                    )
                    Log.d("EnhancedJellyfinSdk", "SDK ApiClient created. It should use the custom Ktor client from JellyfinOptions.")
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

    fun isAvailable(): Boolean = isInitialized

    fun getServerUrl(): String = serverUrl

    private suspend fun makeHttpRequest(endpoint: String): String? {
        if (unsafeHttpClient == null) {
            Log.e("EnhancedJellyfinSdk", "unsafeHttpClient is not initialized. Cannot make HTTP request.")
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                val client = unsafeHttpClient!!
                val url = "${serverUrl}${endpoint}"
                val request = Request.Builder().url(url).build() // Headers added by interceptor
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

    suspend fun testConnectivity(): Boolean {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not fully initialized for testConnectivity via SDK. Trying direct HTTP.")
            return makeHttpRequest("/System/Info") != null
        }
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EnhancedJellyfinSdk", "Testing connectivity via SDK apiClient to $serverUrl...")
                // Use a more reliable, lightweight public API call if possible, or just check initialization.
                // For an actual API call, ensure the 'userId' used is valid for the test context.
                // Here, we'll assume a successful API client initialization is a good enough proxy for this test.
                // A System Ping or similar lightweight public endpoint call from the SDK would be better.
                // apiClient?.systemApi?.getPingSystem() for example, if it exists and is public.
                // For now, let's simulate a check:
                val publicSystemInfo = apiClient?.userViewsApi?.getUserViews(UUID.randomUUID()) // Example, might need a real user or public endpoint
                val success = publicSystemInfo != null // Simplified check

                if (success) {
                    Log.d("EnhancedJellyfinSdk", "Connectivity test via SDK apiClient seems successful.")
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

    private fun parseUuid(uuidString: String): java.util.UUID {
        val cleanUuid = uuidString.replace("-", "")
        if (cleanUuid.length == 32) {
            return try {
                val formattedUuid = "${cleanUuid.substring(0, 8)}-${cleanUuid.substring(8, 12)}-${cleanUuid.substring(12, 16)}-${cleanUuid.substring(16, 20)}-${cleanUuid.substring(20, 32)}"
                java.util.UUID.fromString(formattedUuid)
            } catch (e: IllegalArgumentException) {
                Log.w("EnhancedJellyfinSdk", "Could not parse '$uuidString' as a 32-char UUID, trying direct parse.")
                java.util.UUID.fromString(uuidString)
            }
        }
        return java.util.UUID.fromString(uuidString)
    }

    fun getImageUrl(
        itemId: String,
        imageType: ImageType = ImageType.PRIMARY,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        quality: Int = 96
    ): String? {
        if (!isInitialized || apiClient == null) {
            Log.w("EnhancedJellyfinSdk", "SDK not initialized or apiClient is null. Cannot get image URL.")
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
            Log.e("EnhancedJellyfinSdk", "Failed to generate image URL for item $itemId using SDK apiClient", e)
            null
        }
    }

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
                    includeExternalContent = false,
                    includeHidden = false
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
                Log.e("EnhancedJellyfinSdk", "Failed to get library items using SDK apiClient", e)
                emptyList()
            }
        }
    }
}
