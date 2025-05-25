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
    private val repository: JellyfinRepository,
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

    private val _recentTvEpisodes = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val recentTvEpisodes: StateFlow<List<JellyfinItem>> = _recentTvEpisodes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load all data with proper sequencing to avoid data mixing
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "🚀 Starting data load sequence...")

                // ✅ STEP 1: Load libraries first
                loadLibraries()

                // ✅ STEP 2: Load featured movies (MOVIES ONLY)
                loadFeaturedMovies()

                // ✅ STEP 3: Load recent TV episodes
                loadRecentTvEpisodes()

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ Data loading failed: ${e.message}", e)
                _errorMessage.value = "Failed to load data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load libraries and their recently added items
     */
    private suspend fun loadLibraries() {
        try {
            repository.getUserViews().collect { views ->
                android.util.Log.d("HomeViewModel", "✅ Loaded ${views.size} libraries")
                _libraries.value = views
                _errorMessage.value = null

                // Load recently added items for each library
                views.forEach { library ->
                    loadLibraryRecentItems(library)
                }

                if (views.isNotEmpty()) {
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Failed to load libraries: ${e.message}")
            throw e
        }
    }

    /**
     * ✅ FIXED: Load featured movies ONLY (not libraries)
     */
    private suspend fun loadFeaturedMovies() {
        try {
            android.util.Log.d("HomeViewModel", "🎬 Loading featured movies...")

            repository.getFeaturedMovies().collect { movies ->
                // ✅ DOUBLE CHECK: Ensure we only get movies
                val movieItems = movies.filter { it.Type == "Movie" }
                android.util.Log.d("HomeViewModel", "✅ Featured movies loaded - ${movieItems.size} movies")

                // Log each movie for debugging
                movieItems.forEachIndexed { index, movie ->
                    android.util.Log.d("HomeViewModel", "Featured Movie $index: ${movie.Name} (Type: ${movie.Type})")
                }

                _featured.value = movieItems.take(4) // Only take 4 movies max
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Failed to load featured movies: ${e.message}")
            // Don't throw - featured is optional
        }
    }

    /**
     * Load recent TV episodes from all libraries
     */
    private suspend fun loadRecentTvEpisodes() {
        try {
            android.util.Log.d("HomeViewModel", "📺 Loading recent TV episodes...")

            repository.getRecentTvEpisodes().collect { episodes ->
                android.util.Log.d("HomeViewModel", "✅ Loaded ${episodes.size} recent TV episodes")
                _recentTvEpisodes.value = episodes.take(10) // Max 10 episodes
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Failed to load recent TV episodes: ${e.message}")
            // Don't throw - episodes are optional
        }
    }

    /**
     * Load recently added items for a specific library
     */
    private suspend fun loadLibraryRecentItems(library: JellyfinItem) {
        try {
            repository.getRecentlyAddedForLibrary(library.Id).collect { recentItems ->
                _recentlyAddedItems.update { current ->
                    current + (library.Id to recentItems.take(15)) // Max 15 per library
                }
                android.util.Log.d(
                    "HomeViewModel",
                    "✅ Loaded ${recentItems.size} recently added items for: ${library.Name}"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "HomeViewModel",
                "Failed to load recent items for library ${library.Name}: ${e.message}"
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _isLoading.value = true
        loadData()
    }

    fun clearErrorMessage() {
        clearError()
    }

    fun getServerUrl(): String {
        return repository.getServerUrl()
    }

    /**
     * Get library by ID for display purposes
     */
    fun getLibraryById(libraryId: String): JellyfinItem? {
        return _libraries.value.find { it.Id == libraryId }
    }

    /**
     * Debug method to check data integrity
     */
    fun debugDataIntegrity() {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "=== DATA INTEGRITY CHECK ===")

            val currentFeatured = _featured.value
            android.util.Log.d("HomeViewModel", "Featured items: ${currentFeatured.size}")
            currentFeatured.forEach { item ->
                android.util.Log.d("HomeViewModel", "  - ${item.Name} (${item.Type})")
            }

            val currentLibraries = _libraries.value
            android.util.Log.d("HomeViewModel", "Libraries: ${currentLibraries.size}")
            currentLibraries.forEach { library ->
                android.util.Log.d("HomeViewModel", "  - ${library.Name} (${library.Type})")
            }

            val currentEpisodes = _recentTvEpisodes.value
            android.util.Log.d("HomeViewModel", "Recent episodes: ${currentEpisodes.size}")
            currentEpisodes.take(3).forEach { episode ->
                android.util.Log.d("HomeViewModel", "  - ${episode.Name} (${episode.Type}) from ${episode.SeriesName}")
            }

            android.util.Log.d("HomeViewModel", "=== END DATA CHECK ===")
        }
    }
}



