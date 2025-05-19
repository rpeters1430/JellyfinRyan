package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        if (repository.isLoggedIn()) {
            _loginState.value = LoginState(isLoggedIn = true)
        }
    }

    fun login(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            val result = repository.login(serverUrl, username, password)

            result.fold(
                onSuccess = {
                    _loginState.value = LoginState(isLoggedIn = true)
                },
                onFailure = { e ->
                    _loginState.value = LoginState(errorMessage = e.message ?: "Login failed")
                }
            )
        }
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)