package com.example.jellyfinryan.api

import com.example.jellyfinryan.api.model.JellyfinItem
import com.google.gson.annotations.SerializedName
import retrofit2.http.*

data class AuthenticateUserByNameRequest(
    @SerializedName("Username")
    val username: String,

    @SerializedName("Pw")
    val password: String
)

data class AuthenticateResponse(
    val AccessToken: String,
    val User: User
)

data class ApiResponse<T>(
    val Items: List<T>
)

data class User(
    val Id: String
)

data class UserViewsResponse(val Items: List<JellyfinItem>)
data class LibraryItemsResponse(val Items: List<JellyfinItem>)

interface JellyfinApiService {

    // Authentication
    @POST("Users/AuthenticateByName")
    suspend fun authenticateUserByName(
        @Header("X-Emby-Authorization") authHeader: String,
        @Body request: AuthenticateUserByNameRequest
    ): AuthenticateResponse

    // Fetch libraries
    @GET("Users/{userId}/Views")
    suspend fun getUserViews(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") authToken: String
    ): UserViewsResponse

    // Fetch library-specific items
    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String,
        @Query("SortBy") sortBy: String,
        @Query("SortOrder") sortOrder: String,
        @Query("Limit") limit: Int?,
        @Header("X-Emby-Token") authToken: String
    ): LibraryItemsResponse

    // Fetch general items (e.g., for featured carousel)
    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("SortBy") sortBy: String,
        @Query("SortOrder") sortOrder: String,
        @Query("Limit") limit: Int?,
        @Header("X-Emby-Token") authToken: String
    ): LibraryItemsResponse

    // Fetch seasons for a show
    @GET("Shows/{showId}/Seasons")
    suspend fun getSeasons(
        @Path("showId") showId: String,
        @Header("Authorization") authToken: String
    ): ApiResponse<List<JellyfinItem>>

    // Fetch episodes for a season
    @GET("Seasons/{seasonId}/Episodes")
    suspend fun getEpisodes(
        @Path("seasonId") seasonId: String,
        @Header("Authorization") authToken: String
    ): ApiResponse<List<JellyfinItem>>
}



