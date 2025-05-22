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
    val libraries: StateFlow<List<JellyfinItem>> = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<String, List<JellyfinItem>>>(emptyMap())
    val libraryItems: StateFlow<Map<String, List<JellyfinItem>>> = _libraryItems.asStateFlow()

    init {
        fetchLibraries()
    }

    private fun fetchLibraries() {
        viewModelScope.launch {
            repository.getUserViews().collect { views ->
                _libraries.value = views

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
    }

    fun getServerUrl(): String = repository.getServerUrl()
}

