package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinSdkRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.data.preferences.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sdkRepository: JellyfinSdkRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {private val _libraries = MutableStateFlow<List<JellyfinItem>>(emptyList())
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
        initializeSdk()
    }
    
    /**
     * Initialize the SDK for proper image URL generation
     */
    private fun initializeSdk() {
        viewModelScope.launch {
            try {
                val credentials = dataStoreManager.getCredentials()
                if (!credentials.serverUrl.isNullOrEmpty() && 
                    !credentials.accessToken.isNullOrEmpty() && 
                    !credentials.userId.isNullOrEmpty()) {
                    
                    val success = sdkRepository.initialize(
                        credentials.serverUrl, 
                        credentials.accessToken, 
                        credentials.userId
                    )
                    android.util.Log.d("HomeViewModel", "SDK initialization ${if (success) "successful" else "failed"}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "SDK initialization failed", e)
                // SDK initialization failed, but app should still work with fallback
            }
        }
    }    /**
     * Get the SDK repository for image URL generation
     */
    fun getSdkRepository(): JellyfinSdkRepository = sdkRepository
    
    private fun fetchLibraries() {
        viewModelScope.launch {
            try {                sdkRepository.getUserViews().collect { views ->
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
            try {                sdkRepository.getLibraryItems(libraryId).collect { items ->
                    _libraryItems.update { current ->
                        current + (libraryId to items)
                    }
                }
            } catch (e: Exception) {
                // Log error but don't break the UI
                _errorMessage.value = "Failed to load items for library: ${e.message}"
            }
        }
    }    private fun fetchRecentlyAddedForLibrary(libraryId: String) {
        viewModelScope.launch {
            try {                sdkRepository.getRecentlyAddedForLibrary(libraryId).collect { items ->
                    _recentlyAddedItems.update { current ->
                        current + (libraryId to items)
                    }
                }            } catch (e: Exception) {
                // Log error but don't break the UI
                _errorMessage.value = "Failed to load recently added items: ${e.message}"
            }
        }
    }
    
    private fun loadFeatured() {
        viewModelScope.launch {
            try {
                // Use the sdkRepository's getFeaturedItems method for better content
                sdkRepository.getFeaturedItems().collect { featuredItems ->
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

    fun getServerUrl(): String {
        return try {
            // This is a simple synchronous method, but in real usage we'd need to make it suspend
            // For now, return the stored URL from sdkRepository if available
            sdkRepository.getServerUrl()
        } catch (e: Exception) {
            ""
        }
    }
}


