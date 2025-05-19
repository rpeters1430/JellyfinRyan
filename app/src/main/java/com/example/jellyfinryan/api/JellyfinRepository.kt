package com.example.jellyfinryan.api

import com.example.jellyfinryan.api.model.JellyfinItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.Result
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
open class JellyfinRepository @Inject constructor(
    private val apiService: JellyfinApiService
) {
    private var serverUrl: String = ""
    private var userId: String = ""
    private var accessToken: String = ""

    open fun setServerInfo(url: String) {
        serverUrl = url.removeSuffix("/")
    }

    open fun isLoggedIn(): Boolean {
        return userId.isNotEmpty() && accessToken.isNotEmpty()
    }

    open fun getServerUrl(): String = serverUrl

    open suspend fun login(serverUrl: String, username: String, password: String): Result<Boolean> {
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
    open suspend fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        emit(emptyList())
    }

    open suspend fun getLibraryItems(libraryId: String): Flow<List<JellyfinItem>> = flow {
        emit(emptyList())
    }

    open suspend fun getItemDetails(itemId: String): Flow<JellyfinItem?> = flow {
        emit(null)
    }

    open suspend fun getPlaybackUrl(itemId: String): String? {
        return null
    }
}