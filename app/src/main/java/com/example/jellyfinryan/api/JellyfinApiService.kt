package com.example.jellyfinryan.api

import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.model.JellyfinItemDetails
import com.example.jellyfinryan.api.model.ShowSeason
import com.google.gson.annotations.SerializedName
import retrofit2.Response
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

data class User(
    val Id: String
)
data class UserViewsResponse(val Items: List<JellyfinItem>)
data class LibraryItemsResponse(val Items: List<JellyfinItem>)

data class BaseItemDtoQueryResult<T>(
    @SerializedName("Items") val items: List<T>,
    @SerializedName("TotalRecordCount") val totalRecordCount: Int,
    @SerializedName("StartIndex") val startIndex: Int
)

interface JellyfinApiService {
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

    @GET("Users/{userId}/Items/{itemId}")
    suspend fun getItemDetails(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String,
        @Header("X-Emby-Token") authToken: String
    ): Response<JellyfinItemDetails>

    @GET("Shows/{showId}/Season")
    suspend fun getShowSeasons(
        @Path("showId") showId: String,
        @Header("X-Emby-Token") authToken: String,
        @Query("userId") userId: String
    ): Response<BaseItemDtoQueryResult<ShowSeason>>
}


