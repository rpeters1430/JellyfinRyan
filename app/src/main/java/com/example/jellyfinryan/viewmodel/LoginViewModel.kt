package com.example.jellyfinryan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _loginSuccess = MutableSharedFlow<Boolean>()
    val loginSuccess: SharedFlow<Boolean> = _loginSuccess.asSharedFlow()

    fun login(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Starting login...")
                val result = repository.login(serverUrl, username, password)
                if (result.isSuccess) {
                    Log.d("LoginViewModel", "Login succeeded!")
                    _loginSuccess.emit(true)
                } else {
                    Log.e("LoginViewModel", "Login failed: ${result.exceptionOrNull()?.message}")
                    _loginSuccess.emit(false)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception during login", e)
                _loginSuccess.emit(false)
            }
        }
    }

}
