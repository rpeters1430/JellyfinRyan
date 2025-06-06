package com.example.jellyfinryan.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }

    suspend fun saveCredentials(url: String, userId: String, accessToken: String) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL_KEY] = url
            prefs[USER_ID_KEY] = userId
            prefs[ACCESS_TOKEN_KEY] = accessToken
        }
    }

    data class Credentials(
        val serverUrl: String?,
        val userId: String?,
        val accessToken: String?
    )

    suspend fun getCredentials(): Credentials {
        val prefs = context.dataStore.data.first()
        return Credentials(
            serverUrl = prefs[SERVER_URL_KEY],
            userId = prefs[USER_ID_KEY],
            accessToken = prefs[ACCESS_TOKEN_KEY]
        )
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { prefs ->
            prefs.remove(SERVER_URL_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(ACCESS_TOKEN_KEY)
        }
    }
}

