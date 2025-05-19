package com.example.jellyfinryan.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface JellyfinApiService {
    @POST("/Users/AuthenticateUserByDeviceId")
    suspend fun login(
        @Query("Username") username: String,
        @Query("Password") password: String,
        @Query("Pw") pw: String = "false",
        @Query("DeviceId") deviceId: String = "TV"
    ): Response<AuthResponse>
}

data class AuthResponse(
    val UserId: String,
    val AccessToken: String
)