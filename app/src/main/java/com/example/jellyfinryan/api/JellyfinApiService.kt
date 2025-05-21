package com.example.jellyfinryan.api

import com.example.jellyfinryan.api.model.JellyfinItem
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

data class QuickConnectInitiateResponse(
    val Code: String
)

data class QuickConnectConnectResponse(
    val AuthenticationInfo: AuthenticationInfo?,
    val Servers: List<ServerInfo>?
)

data class AuthenticationInfo(
    val AccessToken: String,
    val User: User
)

data class ServerInfo(
    val Id: String,
    val LocalAddress: String?,
    val RemoteAddress: String?,
    val ServerName: String?
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
    data class PlaybackInfoDto(
        @SerializedName("MediaSources")
        val MediaSources: List<MediaSourceDto>
    )

    data class MediaSourceDto(
        @SerializedName("Id") val Id: String?,
        @SerializedName("Path") val Path: String?,
        @SerializedName("Container") val Container: String?,
    )
    // Fetch all seasons for a show
    @GET("Shows/{showId}/Seasons")
    suspend fun getSeasons(
        @Path("showId") showId: String,
        @Header("Authorization") authToken: String
    ): ApiResponse<List<JellyfinItem>>
    // Fetch all episodes for a season
    @GET("Seasons/{seasonId}/Episodes")
    suspend fun getEpisodes(
        @Path("seasonId") seasonId: String,
        @Header("Authorization") authToken: String
    ): ApiResponse<List<JellyfinItem>>
    @POST("Users/AuthenticateByName")
    suspend fun authenticateUserByName(
        @Header("X-Emby-Authorization") authHeader: String,
        @Body request: AuthenticateUserByNameRequest
    ): AuthenticateResponse
    @GET("Users/{userId}/Views") // <-- This was missing
    suspend fun getUserViews(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") authToken: String
    ): UserViewsResponse
    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String,
        @Query("SortBy") sortBy: String,
        @Query("SortOrder") sortOrder: String,
        @Query("Limit") limit: Int?,
        @Header("X-Emby-Token") authToken: String
    ): LibraryItemsResponse

    @GET("Items/{itemId}/PlaybackInfo")
    suspend fun getPlaybackInfo(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String,
        @Header("X-Emby-Token") authToken: String
    ): PlaybackInfoDto

    @POST("QuickConnect/Initiate")
    suspend fun initiateQuickConnect(): QuickConnectInitiateResponse

    @GET("QuickConnect/Connect")
    suspend fun connectQuickConnect(@Query("secret") secret: String): QuickConnectConnectResponse
}


