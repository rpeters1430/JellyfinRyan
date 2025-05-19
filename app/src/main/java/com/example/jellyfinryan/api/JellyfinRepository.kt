package com.example.jellyfinryan.api

import com.example.jellyfinryan.api.model.JellyfinItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinRepository @Inject constructor() {
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
        // For now, just simulate a successful login
        setServerInfo(serverUrl)
        userId = "userId"
        accessToken = "accessToken"
        return Result.success(true)
    }

    suspend fun getUserViews(): Flow<List<JellyfinItem>> = flow {
        // For now, return empty list
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
}