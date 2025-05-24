package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
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
    private val repository: JellyfinRepository,  // Using original working repository with SSL bypass
    private val dataStoreManager: DataStoreManager
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
        // Start loading data immediately using the working SSL bypass repository
        loadData()
    }

    /**
     * Load data using the original working repository (which has SSL bypass via UnsafeOkHttpClient)
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "🧪 SSL BYPASS TEST: Loading data with original repository...")

                // Test 1: Load libraries (user views)
                repository.getUserViews().collect { views ->
                    android.util.Log.d("HomeViewModel", "✅ SSL BYPASS SUCCESS: Loaded ${views.size} libraries")
                    _libraries.value = views
                    _errorMessage.value = null

                    // Load data for each library
                    views.forEach { library ->
                        loadLibraryData(library)
                    }

                    if (views.isNotEmpty()) {
                        _isLoading.value = false
                    }
                }

                // Test 2: Load featured content
                repository.getFeaturedItems().collect { featured ->
                    android.util.Log.d("HomeViewModel", "✅ SSL BYPASS SUCCESS: Featured content loaded - ${featured.size} items")
                    _featured.value = featured.take(8) // Limit to 8 for TV carousel
                }

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ SSL BYPASS FAILED: ${e.message}", e)
                _errorMessage.value = "Failed to load data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load items and recently added content for a specific library
     */
    private fun loadLibraryData(library: JellyfinItem) {
        viewModelScope.launch {
            try {
                // Load library items
                repository.getLibraryItems(library.Id).collect { items ->
                    _libraryItems.update { current ->
                        current + (library.Id to items)
                    }
                    android.util.Log.d("HomeViewModel", "✅ Loaded ${items.size} items for library: ${library.Name}")
                }

                // Load recently added items for this library
                repository.getRecentlyAddedForLibrary(library.Id).collect { recentItems ->
                    _recentlyAddedItems.update { current ->
                        current + (library.Id to recentItems)
                    }
                    android.util.Log.d("HomeViewModel", "✅ Loaded ${recentItems.size} recently added items for: ${library.Name}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Failed to load data for library ${library.Name}: ${e.message}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        // Retry loading data
        _isLoading.value = true
        loadData()
    }

    fun getServerUrl(): String {
        return repository.getServerUrl()
    }

    /**
     * Test SSL bypass manually (for debugging)
     */
    fun testSslBypassManual() {
        viewModelScope.launch {
            android.util.Log.d("SSL_TEST", "🧪 Manual SSL bypass test started...")
            try {
                // Test basic connectivity
                repository.getUserViews().collect { views ->
                    android.util.Log.d("SSL_TEST", "✅ Manual test successful: ${views.size} libraries loaded")
                }
            } catch (e: Exception) {
                android.util.Log.e("SSL_TEST", "❌ Manual SSL test failed", e)
            }
        }
    }
}


