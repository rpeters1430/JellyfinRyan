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

    private val _featured = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val featured: StateFlow<List<JellyfinItem>> = _featured.asStateFlow()

    init {
        fetchLibraries()
        loadFeatured()
    }

    private fun fetchLibraries() {
        viewModelScope.launch {
            repository.getUserViews().collect { views ->
                _libraries.value = views

                // Only fetch items for libraries that have actual items
                views.forEach { library ->
                    fetchItemsForLibrary(library.Id)
                }
            }
        }
    }

    private fun fetchItemsForLibrary(libraryId: String) {
        viewModelScope.launch {
            repository.getLibraryItems(libraryId).collect { items ->
                _libraryItems.update { current ->
                    current + (libraryId to items)
                }
            }
        }
    }    private fun loadFeatured() {
        viewModelScope.launch {
            // Use the repository's getFeaturedItems method for better content
            repository.getFeaturedItems().collect { featuredItems ->
                _featured.value = featuredItems.take(8) // Limit to 8 for TV carousel
            }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}


