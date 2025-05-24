package com.example.jellyfinryan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.EnhancedJellyfinSdkService
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
) : ViewModel() {
    private val _libraries = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val libraries: StateFlow<List<JellyfinItem>> = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<String, List<JellyfinItem>>>(emptyMap())
    val libraryItems: StateFlow<Map<String, List<JellyfinItem>>> = _libraryItems.asStateFlow()

    private val _recentlyAddedItems = MutableStateFlow<Map<String, List<JellyfinItem>>>(emptyMap())
    val recentlyAddedItems: StateFlow<Map<String, List<JellyfinItem>>> =
        _recentlyAddedItems.asStateFlow()

    private val _featured = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val featured: StateFlow<List<JellyfinItem>> = _featured.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        initializeSdk()
    }

    /**
     * Initialize the SDK for proper image URL generation and then load data
     */
    private fun initializeSdk() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "Starting SDK initialization...")
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

                    if (success) {
                        // 🧪 ADD THIS SSL BYPASS TEST
                        testSslBypass(credentials)

                        // Continue with normal data loading
                        android.util.Log.d("HomeViewModel", "Starting data fetch...")
                        fetchLibraries()
                        loadFeatured()
                    } else {
                        _errorMessage.value = "Failed to connect to Jellyfin server"
                        _isLoading.value = false
                    }
                } else {
                    android.util.Log.w("HomeViewModel", "Missing credentials for SDK initialization")
                    _errorMessage.value = "Missing server credentials"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "SDK initialization failed", e)
                _errorMessage.value = "Failed to initialize: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    /**
     * Get the SDK repository for image URL generation
     */
    fun getSdkRepository(): JellyfinSdkRepository = sdkRepository
    private fun fetchLibraries() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "Starting to fetch libraries...")
                sdkRepository.getUserViews().collect { views ->
                    android.util.Log.d("HomeViewModel", "Received ${views.size} libraries from SDK")
                    _libraries.value = views
                    _errorMessage.value = null

                    // Only fetch items for libraries that have actual items
                    views.forEach { library ->
                        android.util.Log.d(
                            "HomeViewModel",
                            "Fetching items for library: ${library.Name} (${library.Id})"
                        )
                        fetchItemsForLibrary(library.Id)
                        fetchRecentlyAddedForLibrary(library.Id)
                    }

                    // Set loading to false once libraries are loaded
                    if (views.isNotEmpty()) {
                        android.util.Log.d(
                            "HomeViewModel",
                            "Libraries loaded successfully, setting loading to false"
                        )
                        _isLoading.value = false
                    } else {
                        android.util.Log.w(
                            "HomeViewModel",
                            "No libraries found, setting loading to false"
                        )
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Failed to fetch libraries", e)
                _errorMessage.value = "Failed to load libraries: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun fetchItemsForLibrary(libraryId: String) {
        viewModelScope.launch {
            try {
                sdkRepository.getLibraryItems(libraryId).collect { items ->
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
                sdkRepository.getRecentlyAddedForLibrary(libraryId).collect { items ->
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
                android.util.Log.d("HomeViewModel", "Starting to load featured content...")
                // Use the sdkRepository's getFeaturedItems method for better content
                sdkRepository.getFeaturedItems().collect { featuredItems ->
                    android.util.Log.d(
                        "HomeViewModel",
                        "Received ${featuredItems.size} featured items from SDK"
                    )
                    _featured.value = featuredItems.take(8) // Limit to 8 for TV carousel
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Failed to load featured content", e)
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
    // Add this to your HomeViewModel or create a test activity
    private suspend fun testSslBypass(credentials: DataStoreManager.Credentials) {
        try {
            android.util.Log.d("SSL_TEST", "=== STARTING SSL BYPASS TEST ===")

            // Test 1: Basic SDK connectivity
            val connectivityTest = sdkRepository.testConnectivity(credentials.accessToken!!)
            android.util.Log.d("SSL_TEST", "✅ SDK Connectivity Test: $connectivityTest")

            // Test 2: Try to fetch user views (libraries)
            sdkRepository.getUserViews().collect { views ->
                android.util.Log.d("SSL_TEST", "✅ Libraries Fetched: ${views.size} libraries via SSL bypass")
                if (views.isNotEmpty()) {
                    android.util.Log.d("SSL_TEST", "✅ First library: ${views.first().Name}")
                }
            }

            // Test 3: Try to fetch featured items
            sdkRepository.getFeaturedItems().collect { featured ->
                android.util.Log.d("SSL_TEST", "✅ Featured Items Fetched: ${featured.size} items via SSL bypass")
            }

            android.util.Log.d("SSL_TEST", "=== SSL BYPASS TEST COMPLETED SUCCESSFULLY ===")

        } catch (e: Exception) {
            android.util.Log.e("SSL_TEST", "❌ SSL bypass test failed", e)
        }
    }
}


