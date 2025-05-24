package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _libraries = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val libraries: StateFlow<List<JellyfinItem>> = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<String, List<JellyfinItem>>>(emptyMap())
    val libraryItems: StateFlow<Map<String, List<JellyfinItem>>> = _libraryItems.asStateFlow()

    private val _recentlyAddedItems = MutableStateFlow<Map<String, List<JellyfinItem>>>(emptyMap())
    val recentlyAddedItems: StateFlow<Map<String, List<JellyfinItem>>> = _recentlyAddedItems.asStateFlow()

    private val _featured = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val featured: StateFlow<List<JellyfinItem>> = _featured.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchLibraries()
        loadFeatured()
    }    private fun fetchLibraries() {
        viewModelScope.launch {
            try {
                repository.getUserViews().collect { views ->
                    _libraries.value = views
                    _errorMessage.value = null

                    // Only fetch items for libraries that have actual items
                    views.forEach { library ->
                        fetchItemsForLibrary(library.Id)
                        fetchRecentlyAddedForLibrary(library.Id)
                    }
                    
                    // Set loading to false once libraries are loaded
                    if (views.isNotEmpty()) {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load libraries: ${e.message}"
                _isLoading.value = false
            }
        }
    }    private fun fetchItemsForLibrary(libraryId: String) {
        viewModelScope.launch {
            try {
                repository.getLibraryItems(libraryId).collect { items ->
                    _libraryItems.update { current ->
                        current + (libraryId to items)
                    }
                }
            } catch (e: Exception) {
                // Log error but don't break the UI
                _errorMessage.value = "Failed to load items for library: ${e.message}"
            }
        }
    }

    private fun fetchRecentlyAddedForLibrary(libraryId: String) {
        viewModelScope.launch {
            try {
                repository.getRecentlyAddedForLibrary(libraryId).collect { items ->
                    _recentlyAddedItems.update { current ->
                        current + (libraryId to items)
                    }
                }
            } catch (e: Exception) {
                // Log error but don't break the UI
                _errorMessage.value = "Failed to load recently added items: ${e.message}"
            }
        }
    }

    private fun loadFeatured() {
        viewModelScope.launch {
            try {
                // Use the repository's getFeaturedItems method for better content
                repository.getFeaturedItems().collect { featuredItems ->
                    _featured.value = featuredItems.take(8) // Limit to 8 for TV carousel
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load featured content: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getServerUrl(): String = repository.getServerUrl()
}


