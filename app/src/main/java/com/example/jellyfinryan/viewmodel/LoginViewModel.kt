package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    suspend fun tryAutoLogin(): Boolean {
        return withContext(Dispatchers.IO) {
            repository.tryAutoLogin()
        }
    }

    suspend fun login(serverUrl: String, username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            repository.login(serverUrl, username, password).isSuccess
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun getServerUrl(): String = repository.getServerUrl()
}

