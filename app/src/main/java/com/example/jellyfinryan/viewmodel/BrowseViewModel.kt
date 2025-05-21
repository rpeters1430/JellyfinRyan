package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.jellyfinryan.ui.common.UiState

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<JellyfinItem>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadItems(libraryId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.getLibraryItemsFull(libraryId).collect {
                    _uiState.value = UiState.Success(it)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}

