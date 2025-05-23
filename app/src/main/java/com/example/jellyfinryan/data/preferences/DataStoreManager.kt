package com.example.jellyfinryan.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val PASSWORD_KEY = stringPreferencesKey("password")
    }

    suspend fun saveCredentials(
        url: String,
        userId: String,
        accessToken: String,
        username: String,
        password: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL_KEY] = url
            prefs[USER_ID_KEY] = userId
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[USERNAME_KEY] = username
            prefs[PASSWORD_KEY] = password
        }
    }

    data class Credentials(
        val serverUrl: String?,
        val userId: String?,
        val accessToken: String?,
        val username: String?,
        val password: String?
    )

    suspend fun getCredentials(): Credentials {
        val prefs = context.dataStore.data.first()
        return Credentials(
            serverUrl = prefs[SERVER_URL_KEY],
            userId = prefs[USER_ID_KEY],
            accessToken = prefs[ACCESS_TOKEN_KEY],
            username = prefs[USERNAME_KEY],
            password = prefs[PASSWORD_KEY]
        )
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { prefs ->
            prefs.remove(SERVER_URL_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(USERNAME_KEY)
            prefs.remove(PASSWORD_KEY)
        }
    }
}


