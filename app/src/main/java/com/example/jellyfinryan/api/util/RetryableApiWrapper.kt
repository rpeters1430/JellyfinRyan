package com.example.jellyfinryan.api.util

import android.util.Log
import com.example.jellyfinryan.auth.AuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class RetryableApiWrapper(private val authManager: AuthManager) {

    suspend fun <T> executeApiCall(apiCall: suspend () -> T): Flow<T> = flow {
        try {
            emit(apiCall())
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Log.w("RetryableApiWrapper", "401 received. Attempting token refresh...")
                val refreshSuccess = authManager.refreshAccessToken()
                if (refreshSuccess) {
                    emit(apiCall())
                } else {
                    throw e
                }
            } else {
                throw e
            }
        }
    }
}