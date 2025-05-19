package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _loginSuccess = MutableSharedFlow<Boolean>()
    val loginSuccess: SharedFlow<Boolean> = _loginSuccess.asSharedFlow()

    fun login(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            // Perform login logic
            val result = repository.login(serverUrl, username, password)
            if (result.isSuccessful) {
                _loginSuccess.emit(true)
            } else {
                _loginSuccess.emit(false)
            }
        }
    }
}