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

data class ApiResponse<T>(val Items: List<T>)
data class User(val Id: String)
data class UserViewsResponse(val Items: List<JellyfinItem>)
data class LibraryItemsResponse(val Items: List<JellyfinItem>)

interface JellyfinApiService {

    @POST("Users/AuthenticateByName")
    suspend fun authenticateUserByName(
        @Header("X-Emby-Authorization") authHeader: String,
        @Body request: AuthenticateUserByNameRequest
    ): AuthenticateResponse

    @GET("Users/{userId}/Views")
    suspend fun getUserViews(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") authToken: String
    ): UserViewsResponse

    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("SortBy") sortBy: String = "DateCreated",
        @Query("SortOrder") sortOrder: String = "Descending",
        @Query("Limit") limit: Int? = 10,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Header("X-Emby-Token") authToken: String
    ): LibraryItemsResponse

    @GET("Users/{userId}/Items/Latest")
    suspend fun getLatestItems(
        @Path("userId") userId: String,
        @Query("Limit") limit: Int = 10,
        @Query("Fields") fields: String = "BasicSyncInfo,CanDelete,PrimaryImageAspectRatio,Overview,BackdropImageTags,ImageTags,PrimaryImageTag",
        @Query("ImageTypeLimit") imageTypeLimit: Int = 1,
        @Query("EnableImageTypes") enableImageTypes: String = "Primary,Backdrop,Thumb",
        @Header("X-Emby-Token") authToken: String
    ): List<JellyfinItem>

    @GET("Shows/{showId}/Seasons")
    suspend fun getSeasons(
        @Path("showId") showId: String,
        @Header("X-Emby-Token") authToken: String
    ): ApiResponse<JellyfinItem>

    @GET("Shows/{showId}/Episodes")
    suspend fun getEpisodes(
        @Path("showId") seasonId: String,
        @Header("X-Emby-Token") authToken: String
    ): ApiResponse<JellyfinItem>

    // FIXED: Enhanced API call with comprehensive image fields
    @GET("Users/{userId}/Items")
    suspend fun getItemsWithImages(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("SortBy") sortBy: String = "DateCreated",
        @Query("SortOrder") sortOrder: String = "Descending",
        @Query("Limit") limit: Int? = 10,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("Fields") fields: String = "BasicSyncInfo,CanDelete,PrimaryImageAspectRatio,ProductionYear,Overview,Genres,Tags,Taglines,Studios,People,MediaStreams,ProviderIds,ParentId,PrimaryImageTag,BackdropImageTags,ImageTags,SeriesPrimaryImageTag,SeasonUserData,ScreenshotImageTags,ThumbImageTags",
        @Query("ImageTypeLimit") imageTypeLimit: Int = 2, // Allow multiple images
        @Query("EnableImageTypes") enableImageTypes: String = "Primary,Backdrop,Thumb,Screenshot,Logo,Banner,Art,Disc",
        @Query("EnableImages") enableImages: Boolean = true,
        @Query("EnableUserData") enableUserData: Boolean = true,
        @Header("X-Emby-Token") authToken: String
    ): LibraryItemsResponse

    // NEW: Specific endpoint for recently added items with better image support
    @GET("Users/{userId}/Items")
    suspend fun getRecentlyAddedItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("SortBy") sortBy: String = "DateAdded",
        @Query("SortOrder") sortOrder: String = "Descending",
        @Query("Limit") limit: Int = 20,
        @Query("Recursive") recursive: Boolean = true,
        @Query("Fields") fields: String = "BasicSyncInfo,PrimaryImageAspectRatio,ProductionYear,Overview,BackdropImageTags,ImageTags,PrimaryImageTag,SeriesPrimaryImageTag,ParentBackdropImageTags,ParentPrimaryImageTag",
        @Query("ImageTypeLimit") imageTypeLimit: Int = 2,
        @Query("EnableImageTypes") enableImageTypes: String = "Primary,Backdrop,Thumb,Logo,Banner",
        @Query("EnableImages") enableImages: Boolean = true,
        @Query("IncludeItemTypes") includeItemTypes: String = "Movie,Series,Episode,Season",
        @Header("X-Emby-Token") authToken: String
    ): LibraryItemsResponse
}




