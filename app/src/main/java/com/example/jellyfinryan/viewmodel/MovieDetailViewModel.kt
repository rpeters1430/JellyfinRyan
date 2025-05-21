package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: JellyfinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: String = savedStateHandle.get<String>("movieId") ?: ""

    private val _movieDetails = MutableStateFlow<JellyfinItem?>(null)
    val movieDetails: StateFlow<JellyfinItem?> = _movieDetails.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchMovieDetails(movieId)
    }

    private fun fetchMovieDetails(movieId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                repository.getItemDetails(movieId).collect {
                    _movieDetails.value = it
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}