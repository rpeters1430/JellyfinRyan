package com.example.jellyfinryan.api

import android.util.Log
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.data.preferences.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException


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
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("$serverUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    suspend fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)

        val views = api.getUserViews(userId, accessToken)
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
            includeItemTypes = null,
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
            limit = null,
            includeItemTypes = null,
            authToken = accessToken
        )
        emit(response.Items)
    }

    suspend fun getFeaturedItems(): Flow<List<JellyfinItem>> = flow {
        try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)

            val response = api.getItems(
                userId = userId,
                parentId = null,
                sortBy = "DateCreated",
                sortOrder = "Descending",
                limit = 10,
                includeItemTypes = "Movie,Series,Episode",
                authToken = accessToken
            )
            emit(response.Items)
        } catch (e: HttpException) {
            Log.e("JellyfinRepository", "Failed to load featured items: ${e.code()} ${e.message()}")
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Unexpected error: ${e.message}")
            emit(emptyList())
        }
    }

    suspend fun getSeasonItems(showId: String): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)

        val response = api.getSeasons(showId, accessToken)
        emit(response.Items.flatten())
    }

    suspend fun getEpisodeItems(seasonId: String): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)

        val response = api.getEpisodes(seasonId, accessToken)
        emit(response.Items.flatten())
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
}


