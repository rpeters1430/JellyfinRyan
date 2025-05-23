package com.example.jellyfinryan.api

import android.util.Log
import com.example.jellyfinryan.api.model.AuthenticateUserByNameRequest
import com.example.jellyfinryan.api.model.AuthenticateResponse
import com.example.jellyfinryan.data.preferences.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    suspend fun refreshAuthToken(): Boolean = withContext(Dispatchers.IO) {
        val credentials = dataStoreManager.getCredentials()
        val serverUrl = credentials.serverUrl
        val userId = credentials.userId
        val accessToken = credentials.accessToken

        if (serverUrl.isNullOrBlank() || userId.isNullOrBlank() || accessToken.isNullOrBlank()) {
            Log.e("AuthManager", "No credentials available for token refresh")
            return@withContext false
        }

        val username = dataStoreManager.getStoredUsername()
        val password = dataStoreManager.getStoredPassword()

        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            Log.e("AuthManager", "Username or password missing for token refresh")
            return@withContext false
        }

        return@withContext try {
            val retrofit = createRetrofit(serverUrl)
            val api = retrofit.create(JellyfinApiService::class.java)
            val authHeader = buildAuthorizationHeader()
            val response: AuthenticateResponse = api.authenticateUserByName(
                authHeader,
                AuthenticateUserByNameRequest(username, password)
            )

            dataStoreManager.saveCredentials(
                url = serverUrl,
                userId = response.User.Id,
                accessToken = response.AccessToken
            )

            Log.d("AuthManager", "Token refreshed successfully")
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "Token refresh failed: ${e.message}", e)
            false
        }
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

    private fun buildAuthorizationHeader(): String {
        val app = "JellyfinRyan"
        val version = "1.0.0"
        val device = "AndroidTV"
        val deviceId = "android-emulator"

        return "MediaBrowser Client=\"$app\", Device=\"$device\", DeviceId=\"$deviceId\", Version=\"$version\""
    }
}
