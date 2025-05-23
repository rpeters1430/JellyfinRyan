package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class) // Required for flatMapLatest
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _libraries = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val libraries: StateFlow<List<JellyfinItem>> = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<String, List<JellyfinItem>>>(emptyMap())
    val libraryItems: StateFlow<Map<String, List<JellyfinItem>>> = _libraryItems.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    // Items for the androidx.tv.material3.Carousel for the selected library.
    // Filters for Movies and TV Shows (Series) from the recently added items.
    val carouselItemsForSelectedLibrary: StateFlow<List<JellyfinItem>> =
        _selectedTabIndex.flatMapLatest { tabIndex ->
            _libraries.flatMapLatest { libs ->
                if (libs.isNotEmpty() && tabIndex >= 0 && tabIndex < libs.size) {
                    val selectedLibraryId = libs[tabIndex].Id
                    _libraryItems.map { allItemsMap ->
                        val itemsFromLibrary = allItemsMap[selectedLibraryId] ?: emptyList()
                        itemsFromLibrary
                            .filter { it.Type == "Movie" || it.Type == "Series" } // Filter for Movies and Series
                            .take(10) // Take up to 10 items
                    }
                } else {
                    flowOf(emptyList())
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        fetchLibraries()
    }

    private fun fetchLibraries() {
        viewModelScope.launch {
            repository.getUserViews().collect { views ->
                _libraries.value = views
                if (_selectedTabIndex.value >= views.size && views.isNotEmpty()) {
                    _selectedTabIndex.value = 0
                } else if (views.isEmpty()) {
                    _selectedTabIndex.value = 0
                }
                views.forEach { library ->
                    if (library.Type == "CollectionFolder") {
                        fetchItemsForLibrary(library.Id)
                    }
                }
            }
        }
    }

    private fun fetchItemsForLibrary(libraryId: String) {
        viewModelScope.launch {
            // Assuming getLibraryItems fetches items sorted by DateCreated Descending
            repository.getLibraryItems(libraryId).collect { items ->
                _libraryItems.update { currentMap ->
                    currentMap + (libraryId to items)
                }
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        if (tabIndex >= 0 && tabIndex < _libraries.value.size) {
            _selectedTabIndex.value = tabIndex
        } else if (_libraries.value.isNotEmpty()){
            _selectedTabIndex.value = 0
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}
