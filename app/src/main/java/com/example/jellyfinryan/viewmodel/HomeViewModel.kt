package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _libraries = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val libraries: StateFlow<List<JellyfinItem>> = _libraries

    private val _recentItemsMap = MutableStateFlow<Map<String, List<JellyfinItem>>>(emptyMap())
    val recentItemsMap: StateFlow<Map<String, List<JellyfinItem>>> = _recentItemsMap

    init {
        fetchLibraries()
    }

    private fun fetchLibraries() {
        viewModelScope.launch {
            repository.getUserViews().collect { views ->
                _libraries.value = views
                views.forEach { view ->
                    fetchRecentItems(view.Id)
                }
            }
        }
    }

    private fun fetchRecentItems(libraryId: String) {
        viewModelScope.launch {
            repository.getLibraryItems(libraryId).collect { items ->
                _recentItemsMap.update { it + (libraryId to items) }
            }
        }
    }
}
