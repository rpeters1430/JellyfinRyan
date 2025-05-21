package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.jellyfinryan.ui.common.UiState // Import UiState
import androidx.lifecycle.ViewModel
import com.example.jellyfinryan.api.MediaTechnicalDetails
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

    private val _uiState = MutableStateFlow<UiState<JellyfinItem?>>(UiState.Loading) // Use UiState
    val uiState: StateFlow<UiState<JellyfinItem?>> = _uiState.asStateFlow()

    private val _technicalDetails = MutableStateFlow<MediaTechnicalDetails?>(null)
    val technicalDetails: StateFlow<MediaTechnicalDetails?> = _technicalDetails.asStateFlow()

    init {
        fetchMovieDetails(movieId)
        _uiState.value = UiState.Loading // Emit loading at the start
    }

    private fun fetchMovieDetails(movieId: String) {
        viewModelScope.launch {
            try {
                repository.getItemDetails(movieId).collect {
                    _uiState.value = UiState.Success(it) // Emit Success with the data
                    _technicalDetails.value = repository.getMediaTechnicalDetails(movieId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}