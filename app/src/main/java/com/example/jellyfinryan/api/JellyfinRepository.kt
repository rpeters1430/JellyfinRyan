package com.example.jellyfinryan.api

import android.util.Log
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.data.preferences.DataStoreManager
import com.example.jellyfinryan.utils.UnsafeOkHttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinRepository @Inject constructor(
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

    fun getUserId(): String = userId

    fun getAccessToken(): String = accessToken

    suspend fun login(serverUrl: String, username: String, password: String): Result<Boolean> {
        return try {
            setServerInfo(serverUrl)
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            val authHeader = buildAuthorizationHeader()
            val response = api.authenticateUserByName(
                authHeader,
                AuthenticateUserByNameRequest(username, password)
            )

            accessToken = response.AccessToken
            userId = response.User.Id

            dataStoreManager.saveCredentials(
                url = serverUrl,
                userId = userId,
                accessToken = accessToken
            )

            Log.d("JellyfinRepository", "Login successful. Token: ${accessToken.take(10)}...")
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
            Log.d("JellyfinRepository", "Auto-login successful")
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
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Use the unsafe HTTP client that accepts all SSL certificates
        val client = UnsafeOkHttpClient.getUnsafeOkHttpClient()
            .newBuilder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("$serverUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            Log.d("JellyfinRepository", "Getting user views for user: $userId")
            val views = api.getUserViews(userId, accessToken)
            emit(views.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load user views: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading user views: ${e.message}")
            emit(emptyList())
        }
    }

    fun getLibraryItems(libraryId: String): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            val items = api.getItems(
                userId = userId,
                parentId = libraryId,
                sortBy = "DateCreated",
                sortOrder = "Descending",
                limit = 10,
                includeItemTypes = null,
                authToken = accessToken
            )
            emit(items.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load library items: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading library items: ${e.message}")
            emit(emptyList())
        }
    }

    fun getLibraryItemsFull(libraryId: String): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            val response = api.getItems(
                userId = userId,
                parentId = libraryId,
                sortBy = "SortName",
                sortOrder = "Ascending",
                limit = null,
                includeItemTypes = null,
                authToken = accessToken
            )
            emit(response.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load full library items: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading full library items: ${e.message}")
            emit(emptyList())
        }
    }

    fun getFeaturedItems(): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            Log.d("JellyfinRepository", "Getting latest items from server with token: ${accessToken.take(10)}...")
            val latestItems = api.getLatestItems(
                userId = userId,
                limit = 10,
                authToken = accessToken
            )

            // Filter to only include items with good backdrop images for the carousel
            val filteredItems = latestItems.filter { item ->
                item.Type in listOf("Movie", "Series", "Episode") &&
                        !item.getImageUrl(serverUrl).isNullOrEmpty()
            }

            emit(filteredItems)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load latest items: ${e.code()} ${e.message()}")
            // Fallback to regular items query
            try {
                val api = createRetrofit(serverUrl).create(JellyfinApiService::class.java)
                val response = api.getItems(
                    userId = userId,
                    parentId = null,
                    sortBy = "DateCreated",
                    sortOrder = "Descending",
                    limit = 10,
                    includeItemTypes = "Movie,Series,Episode",
                    authToken = accessToken
                )
                emit(response.Items.filter { !it.getImageUrl(serverUrl).isNullOrEmpty() })
            } catch (fallbackError: Exception) {
                Log.e("JellyfinRepository", "Fallback featured items failed: ${fallbackError.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading latest items: ${e.message}")
            emit(emptyList())
        }
    }

    fun getSeasonItems(showId: String): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            val response = api.getSeasons(showId, accessToken)
            emit(response.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load seasons: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading seasons: ${e.message}")
            emit(emptyList())
        }
    }

    fun getEpisodeItems(seasonId: String): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            val response = api.getEpisodes(seasonId, accessToken)
            emit(response.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load episodes: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading episodes: ${e.message}")
            emit(emptyList())
        }
    }

    fun getItemDetails(itemId: String): Flow<JellyfinItem?> = flow {
        emit(null)
    }

    fun getPlaybackUrl(itemId: String): String? {
        return null
    }

    private fun buildAuthorizationHeader(): String {
        val app = "JellyfinRyan"
        val version = "1.0.0"
        val device = "AndroidTV"
        val deviceId = "android-emulator"

        return "MediaBrowser Client=\"$app\", Device=\"$device\", DeviceId=\"$deviceId\", Version=\"$version\""
    }

    fun getRecentlyAddedForLibrary(libraryId: String): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            Log.d("JellyfinRepository", "Getting recently added items for library: $libraryId")

            // FIXED: Use the standard Items endpoint with proper sorting for library-specific recent items
            val response = api.getItems(
                userId = userId,
                parentId = libraryId, // This ensures we only get items from this library
                sortBy = "DateCreated", // Sort by when items were added to the server
                sortOrder = "Descending",
                limit = 20,
                includeItemTypes = "Movie,Series,Episode", // Only get media items, not folders
                authToken = accessToken
            )

            Log.d("JellyfinRepository", "Library $libraryId returned ${response.Items.size} recently added items")

            response.Items.forEachIndexed { index, item ->
                val imageUrl = item.getHorizontalImageUrl(serverUrl)
                Log.d("JellyfinRepository", "Recent item $index: ${item.Name} (${item.Type}) - Image: $imageUrl")
            }

            emit(response.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load recently added for library $libraryId: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading recently added for library $libraryId: ${e.message}")
            emit(emptyList())
        }
    }

    fun getFeaturedMovies(): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            Log.d("JellyfinRepository", "🎬 Getting featured movies with proper filtering...")

            // ✅ FIXED: Use proper API call with explicit Movie filtering
            val response = api.getItemsWithImages(
                userId = userId,
                parentId = null, // Search all libraries
                sortBy = "DateCreated", // Sort by when added to server
                sortOrder = "Descending", // Most recent first
                limit = 8, // Get more to ensure we have enough movies after filtering
                includeItemTypes = "Movie", // ✅ ONLY MOVIES - This is key!
                authToken = accessToken
            )

            Log.d("JellyfinRepository", "Featured movies API returned ${response.Items.size} items")

            // ✅ DOUBLE CHECK: Filter to ensure only movies with images
            val movieItems = response.Items.filter { item ->
                item.Type == "Movie" && !item.getImageUrl(serverUrl).isNullOrEmpty()
            }

            Log.d("JellyfinRepository", "✅ Filtered to ${movieItems.size} movies with images")

            // Log each movie for debugging
            movieItems.forEachIndexed { index, movie ->
                Log.d("JellyfinRepository", "Featured Movie $index: ${movie.Name} (Type: ${movie.Type}) - Image: ${movie.getImageUrl(serverUrl)}")
            }

            emit(movieItems.take(4)) // Take max 4 movies for featured carousel
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load featured movies: ${e.code()} ${e.message()}")

            // ✅ FALLBACK: Try alternative approach if the enhanced API fails
            try {
                Log.d("JellyfinRepository", "Trying fallback approach for featured movies...")
                val api = createRetrofit(serverUrl).create(JellyfinApiService::class.java)
                val response = api.getItems(
                    userId = userId,
                    parentId = null,
                    sortBy = "DateCreated",
                    sortOrder = "Descending",
                    limit = 8,
                    includeItemTypes = "Movie", // ✅ EXPLICIT MOVIE FILTERING
                    authToken = accessToken
                )

                val movieItems = response.Items.filter { item ->
                    item.Type == "Movie" && !item.getImageUrl(serverUrl).isNullOrEmpty()
                }

                Log.d("JellyfinRepository", "✅ Fallback got ${movieItems.size} movies")
                emit(movieItems.take(4))
            } catch (fallbackError: Exception) {
                Log.e("JellyfinRepository", "Fallback featured movies also failed: ${fallbackError.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading featured movies: ${e.message}")
            emit(emptyList())
        }
    }
    // ✅ ADDED: Get recent TV episodes from all libraries
    fun getRecentTvEpisodes(): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            Log.d("JellyfinRepository", "Getting recent TV episodes with proper API parameters")

            // Use the enhanced API call to get recent episodes with comprehensive image fields
            val response = api.getItemsWithImages(
                userId = userId,
                parentId = null, // Search all libraries
                sortBy = "DateCreated", // Sort by when episodes were added to server
                sortOrder = "Descending", // Most recent first
                limit = 10, // Last 10 episodes
                includeItemTypes = "Episode", // Only episodes
                authToken = accessToken
            )

            Log.d("JellyfinRepository", "Recent TV episodes API returned ${response.Items.size} episodes")

            // Log each episode for debugging
            response.Items.forEachIndexed { index, episode ->
                val imageUrl = episode.getHorizontalImageUrl(serverUrl)
                Log.d("JellyfinRepository", "Episode $index: ${episode.Name} (Series: ${episode.SeriesName}) - Image: $imageUrl")
            }

            emit(response.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load recent TV episodes: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error loading recent TV episodes: ${e.message}")
            emit(emptyList())
        }
    }
}


