package com.example.jellyfinryan.api

import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.model.Season
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import kotlin.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinRepository @Inject constructor(
    private val apiService: JellyfinApiService
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
            val response = apiService.login(username, password, "false", "TV")
            if (response.isSuccessful && response.body() != null) {
                userId = response.body()!!.UserId
                accessToken = response.body()!!.AccessToken
                Result.success(true)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        emit(emptyList())
    }

    suspend fun getLibraryItems(libraryId: String): Flow<List<JellyfinItem>> = flow {
        emit(emptyList())
    }

    suspend fun getItemDetails(itemId: String): Flow<JellyfinItem?> = flow {
        emit(null)
    }

    suspend fun getPlaybackUrl(itemId: String): String? {
        return null
    }

    suspend fun getSeasons(itemId: String): Flow<List<Season>> = flow {
        emit(emptyList())
    }
}

