package com.example.jellyfinryan.api

import android.util.Log
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.model.JellyfinItemDetails
import com.example.jellyfinryan.api.model.ShowSeason
import com.example.jellyfinryan.data.preferences.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class JellyfinRepository @Inject constructor(
    private val jellyfinApiService: JellyfinApiService,
    private val dataStoreManager: DataStoreManager
) {
    private var serverUrl: String = ""
    private var userId: String = ""
    private var accessToken: String = ""

    fun setServerInfo(url: String) {
        serverUrl = url.removeSuffix("/")
    }

    fun isLoggedIn(): Boolean {
        return userId.isNotEmpty() && accessToken.isNotEmpty()
    }

    fun getServerUrl(): String = serverUrl

    suspend fun login(serverUrl: String, username: String, password: String): Result<Boolean> {
        return try {
            setServerInfo(serverUrl)
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            // Construct the X-Emby-Authorization header
            val authHeader = buildAuthorizationHeader()

            // Send authentication request with header and body
            val response = api.authenticateUserByName(
                authHeader,
                AuthenticateUserByNameRequest(username, password)
            )

            accessToken = response.AccessToken
            userId = response.User.Id

            // Save credentials
            dataStoreManager.saveCredentials(
                url = serverUrl,
                userId = userId,
                accessToken = accessToken
            )

            Result.success(true)
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Login failed", e)
            Result.failure(e)
        }
    }

    suspend fun tryAutoLogin(): Boolean {
        val creds = dataStoreManager.getCredentials()
        if (creds.serverUrl != null && creds.userId != null && creds.accessToken != null) {
            serverUrl = creds.serverUrl
            userId = creds.userId
            accessToken = creds.accessToken
            return true
        }
        return false
    }

    suspend fun logout() {
        dataStoreManager.clearCredentials()
        serverUrl = ""
        userId = ""
        accessToken = ""
    }

    private fun createRetrofit(serverUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl("$serverUrl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    suspend fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)

        val views = api.getUserViews(
            userId = userId,
            authToken = accessToken
        )
        emit(views.Items)
    }

    suspend fun getLibraryItems(libraryId: String): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)

        val items = api.getItems(
            userId = userId,
            parentId = libraryId,
            sortBy = "DateCreated",
            sortOrder = "Descending",
            limit = 10,
            authToken = accessToken
        )
        emit(items.Items)
    }

    suspend fun getLibraryItemsFull(libraryId: String): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)

        val response = api.getItems(
            userId = userId,
            parentId = libraryId,
            sortBy = "SortName",
            sortOrder = "Ascending",
            limit = null, // Remove limit to fetch full list
            authToken = accessToken
        )
        emit(response.Items)
    }

    suspend fun getItemDetails(itemId: String): Result<JellyfinItemDetails> {
        return try {
            val userId = dataStoreManager.getUserId().first()
            val token = dataStoreManager.getAccessToken().first()
            // The base URL is already configured in your AppModule for Retrofit
            // No need to prepend serverAddress here for the API call itself.

            if (userId.isNullOrEmpty() || token.isNullOrEmpty()) {
                return Result.failure(Exception("User ID or Token is missing."))
            }

            val response =
                jellyfinApiService.getItemDetails(userId = userId, itemId = itemId, token = token)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        "Error fetching item details: ${response.code()} - ${response.message()} - ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShowSeasons(showId: String): Result<List<ShowSeason>> {
        return try {
            val userId = dataStoreManager.getUserId().first()
            val token = dataStoreManager.getAccessToken().first()

            if (userId.isNullOrEmpty() || token.isNullOrEmpty()) {
                return Result.failure(Exception("User ID or Token is missing."))
            }

            val response =
                jellyfinApiService.getShowSeasons(showId = showId, token = token, userId = userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.items) // Extract items from BaseItemDtoQueryResult
            } else {
                Result.failure(
                    Exception(
                        "Error fetching show seasons: ${response.code()} - ${response.message()} - ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
    suspend fun getItemDetails(itemId: String): Flow<JellyfinItem?> = flow {
        emit(null)
    }

    suspend fun getPlaybackUrl(itemId: String): String? {
        return null
    }

    private fun buildAuthorizationHeader(): String {
        val app = "JellyfinRyan"
        val version = "1.0.0"
        val device = "AndroidTV"
        val deviceId = "android-emulator"

        return "MediaBrowser Client=\"$app\", Device=\"$device\", DeviceId=\"$deviceId\", Version=\"$version\""
    }

/**
 * Constructs a full image URL for a Jellyfin item.
 *
 * @param itemId The ID of the item (e.g., show, movie, season, episode).
 * @param imageTag The specific tag for the image (e.g., from item.ImageTags["Primary"]).
 * @param imageType The type of image (Primary, Backdrop, Thumb, Logo, etc.).
 * @param maxWidth Optional maximum width for the image.
 * @param maxHeight Optional maximum height for the image.
 * @param quality Optional image quality (0-100).
 * @return The full image URL string, or null if essential parts are missing.
 */
suspend fun getImageUrl(
    itemId: String,
    imageTag: String?,
    imageType: String = "Primary", // Default to Primary
    maxWidth: Int? = null,
    maxHeight: Int? = null,
    quality: Int = 90 // Default quality
): String? {
    val serverAddress = dataStoreManager.getServerAddress().first()
    if (serverAddress.isNullOrEmpty() || imageTag.isNullOrEmpty()) {
        return null // Or a placeholder URL
    }

    // Ensure serverAddress does not end with a slash before appending
    val cleanServerAddress = serverAddress.removeSuffix("/")
    var url = "$cleanServerAddress/Items/$itemId/Images/$imageType?tag=$imageTag&quality=$quality"

    maxWidth?.let { url += "&maxWidth=$it" }
    maxHeight?.let { url += "&maxHeight=$it" }
    // You can also use fillWidth and fillHeight if you prefer scaling behavior over max dimensions
    // maxHeight?.let { url += "&fillHeight=$it" }
    // maxWidth?.let { url += "&fillWidth=$it" }

    return url
}


