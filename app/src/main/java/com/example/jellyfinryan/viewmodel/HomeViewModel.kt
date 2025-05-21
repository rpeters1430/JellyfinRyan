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

    private val _featuredItems = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val featuredItems: StateFlow<List<JellyfinItem>> = _featuredItems.asStateFlow()

 private val _isLoading = MutableStateFlow(false)
 val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

 private val _error = MutableStateFlow<String?>(null)
 val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchLibraries()
    }

    private fun fetchLibraries() {
        viewModelScope.launch {
            repository.getUserViews().collect { views ->
                _isLoading.value = true
 _error.value = null
 try {
 _libraries.value = views

 views.forEach { library ->
 fetchItemsForLibrary(library.Id)
 }

 val tvLibrary = views.find { it.CollectionType == "tvshows" }
 tvLibrary?.let {
 fetchFeaturedItems(it.Id)
 }
 } catch (e: Exception) {
 _error.value = e.message
 } finally {
 _isLoading.value = false
 }
            }
        }
    }

    private fun fetchItemsForLibrary(libraryId: String) {
        viewModelScope.launch {
 _isLoading.value = true
 _error.value = null
 try {
            repository.getLibraryItems(libraryId).collect { items ->
                _libraryItems.update { current ->
                    current + (libraryId to items)
                }
            }
 } catch (e: Exception) {
 _error.value = e.message
 } finally {
 _isLoading.value = false
 }
        }
    }

    fun fetchFeaturedItems(libraryId: String) {
        viewModelScope.launch {
 _isLoading.value = true
 _error.value = null
 try {
            repository.getLibraryItems(libraryId).collect { items ->
                _featuredItems.value = items
            }
        }
    }
    fun getServerUrl(): String = repository.getServerUrl()
}

