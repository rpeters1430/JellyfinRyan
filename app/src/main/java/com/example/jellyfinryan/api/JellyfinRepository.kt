package com.example.jellyfinryan.api

import android.util.Log
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.data.preferences.DataStoreManager
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

    fun isLoggedIn(): Boolean = userId.isNotEmpty() && accessToken.isNotEmpty()

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
                accessToken = accessToken,
                username = username,
                password = password
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

    suspend fun refreshTokenIfNeeded(): Boolean {
        val creds = dataStoreManager.getCredentials()
        if (creds.username != null && creds.password != null && creds.serverUrl != null) {
            val result = login(creds.serverUrl, creds.username, creds.password)
            return result.isSuccess
        }
        return false
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): T? {
        return try {
            apiCall()
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Log.w("JellyfinRepository", "401 Unauthorized. Attempting to refresh token.")
                val refreshed = refreshTokenIfNeeded()
                if (refreshed) apiCall() else null
            } else {
                throw e
            }
        }
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

    fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)
        val result = safeApiCall { api.getUserViews(userId, accessToken) }
        result?.let { emit(it.Items) } ?: emit(emptyList())
    }

    fun getLibraryItems(libraryId: String): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)
        val result = safeApiCall {
            api.getItems(
                userId = userId,
                parentId = libraryId,
                sortBy = "DateCreated",
                sortOrder = "Descending",
                limit = 10,
                includeItemTypes = null,
                authToken = accessToken
            )
        }
        result?.let { emit(it.Items) } ?: emit(emptyList())
    }

    fun getFeaturedItems(): Flow<List<JellyfinItem>> = flow {
        val retrofit = createRetrofit(serverUrl)
        val api = retrofit.create(JellyfinApiService::class.java)
        val result = safeApiCall {
            api.getItems(
                userId = userId,
                parentId = null,
                sortBy = "DateCreated",
                sortOrder = "Descending",
                limit = 10,
                includeItemTypes = "Movie,Series,Episode",
                authToken = accessToken
            )
        }
        result?.let { emit(it.Items) } ?: emit(emptyList())
    }

    // Add similar safeApiCall wrapper to other API calls if needed
}




