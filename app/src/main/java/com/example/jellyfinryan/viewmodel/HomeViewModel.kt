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
                android.util.Log.d(
                    "HomeViewModel",
                    "🧪 SSL BYPASS TEST: Loading data with original repository..."
                )

                // Test 1: Load libraries (user views)
                repository.getUserViews().collect { views ->
                    android.util.Log.d(
                        "HomeViewModel",
                        "✅ SSL BYPASS SUCCESS: Loaded ${views.size} libraries"
                    )
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
                repository.getFeaturedMovies().collect { movies ->
                    android.util.Log.d("HomeViewModel", "✅ Featured movies loaded - ${movies.size} movies")
                    _featured.value = movies // This will be the last 3 movies
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
                    android.util.Log.d(
                        "HomeViewModel",
                        "✅ Loaded ${items.size} items for library: ${library.Name}"
                    )
                }

                // Load recently added items for this library
                repository.getRecentlyAddedForLibrary(library.Id).collect { recentItems ->
                    _recentlyAddedItems.update { current ->
                        current + (library.Id to recentItems)
                    }
                    android.util.Log.d(
                        "HomeViewModel",
                        "✅ Loaded ${recentItems.size} recently added items for: ${library.Name}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "HomeViewModel",
                    "Failed to load data for library ${library.Name}: ${e.message}"
                )
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
                    android.util.Log.d(
                        "SSL_TEST",
                        "✅ Manual test successful: ${views.size} libraries loaded"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SSL_TEST", "❌ Manual SSL test failed", e)
            }
        }
    }

    /**
     * Test image URL generation for debugging (remove after fixing)
     */
    fun testImageUrls() {
        viewModelScope.launch {
            android.util.Log.d("IMAGE_TEST", "🧪 Testing image URL generation...")

            // Test with featured items
            val currentFeatured = _featured.value
            android.util.Log.d("IMAGE_TEST", "Found ${currentFeatured.size} featured items")

            currentFeatured.take(3).forEach { item ->
                android.util.Log.d("IMAGE_TEST", "--- Testing item: ${item.Name} ---")
                android.util.Log.d("IMAGE_TEST", "Type: ${item.Type}")
                android.util.Log.d("IMAGE_TEST", "PrimaryImageTag: ${item.PrimaryImageTag}")
                android.util.Log.d("IMAGE_TEST", "BackdropImageTags: ${item.BackdropImageTags}")
                android.util.Log.d("IMAGE_TEST", "ImageTags: ${item.ImageTags}")

                val serverUrl = getServerUrl()
                val imageUrl = item.getImageUrl(serverUrl)
                val horizontalUrl = item.getHorizontalImageUrl(serverUrl)
                val carouselUrl = item.getFeaturedCarouselImageUrl(serverUrl)

                android.util.Log.d("IMAGE_TEST", "Primary image URL: $imageUrl")
                android.util.Log.d("IMAGE_TEST", "Horizontal image URL: $horizontalUrl")
                android.util.Log.d("IMAGE_TEST", "Carousel image URL: $carouselUrl")
                android.util.Log.d("IMAGE_TEST", "Server URL: $serverUrl")
                android.util.Log.d("IMAGE_TEST", "--------------------------------")
            }

            // Test with library items
            val currentLibraries = _libraries.value
            android.util.Log.d("IMAGE_TEST", "Found ${currentLibraries.size} libraries")

            currentLibraries.take(2).forEach { library ->
                android.util.Log.d("IMAGE_TEST", "--- Testing library: ${library.Name} ---")
                val libraryItems = _libraryItems.value[library.Id] ?: emptyList()
                android.util.Log.d("IMAGE_TEST", "Library has ${libraryItems.size} items")

                libraryItems.take(2).forEach { item ->
                    val imageUrl = item.getImageUrl(getServerUrl())
                    android.util.Log.d("IMAGE_TEST", "Library item ${item.Name}: $imageUrl")
                }
            }

            // Test recently added items
            val recentItems = _recentlyAddedItems.value
            android.util.Log.d(
                "IMAGE_TEST",
                "Found recently added items for ${recentItems.keys.size} libraries"
            )

            recentItems.forEach { (libraryId, items) ->
                val libraryName = _libraries.value.find { it.Id == libraryId }?.Name ?: "Unknown"
                android.util.Log.d(
                    "IMAGE_TEST",
                    "--- Recently added in $libraryName (${items.size} items) ---"
                )
                items.take(2).forEach { item ->
                    val horizontalUrl = item.getHorizontalImageUrl(getServerUrl())
                    android.util.Log.d("IMAGE_TEST", "Recent item ${item.Name}: $horizontalUrl")
                }
            }
        }
    }
}



