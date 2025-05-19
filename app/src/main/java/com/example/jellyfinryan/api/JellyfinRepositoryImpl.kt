package com.example.jellyfinryan.api

import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.JellyfinApiService
import com.example.jellyfinryan.api.JellyfinRepository
import kotlinx.coroutines.flow.Flow
import kotlin.Result
import javax.inject.Inject

class JellyfinRepositoryImpl @Inject constructor(
    private val apiService: JellyfinApiService
) : JellyfinRepository() {

    override fun setServerInfo(url: String) {
        super.setServerInfo(url)
    }

    override fun isLoggedIn(): Boolean {
        return super.isLoggedIn()
    }

    override fun getServerUrl(): String {
        return super.getServerUrl()
    }

    override suspend fun login(serverUrl: String, username: String, password: String): Result<Boolean> {
        return super.login(serverUrl, username, password)
    }

    override suspend fun getUserViews(): Flow<List<JellyfinItem>> {
        return super.getUserViews()
    }

    override suspend fun getLibraryItems(libraryId: String): Flow<List<JellyfinItem>> {
        return super.getLibraryItems(libraryId)
    }

    override suspend fun getItemDetails(itemId: String): Flow<JellyfinItem?> {
        return super.getItemDetails(itemId)
    }

    override suspend fun getPlaybackUrl(itemId: String): String? {
        return super.getPlaybackUrl(itemId)
    }
}