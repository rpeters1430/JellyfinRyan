package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    private val repository: JellyfinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
 val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

 private val _error = MutableStateFlow<String?>(null)
 val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchLibraryDetails()
        fetchFeaturedItems()
        fetchAllLibraryItems()
    }

    private fun fetchLibraryDetails() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
 try {
 _libraryDetails.value = repository.getItemDetails(libraryId)
 } catch (e: Exception) {
 _error.value = e.message
 } finally {
                _isLoading.value = false
 }
        }
    }

    private fun fetchFeaturedItems() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
 try {
 _featuredItems.value = repository.getLibraryItems(libraryId).take(10) // Taking a limited number for now
 } catch (e: Exception) {
 _error.value = e.message
 } finally {
                _isLoading.value = false
 }
        }
    }

    private val libraryId: String = savedStateHandle.get<String>("libraryId") ?: ""

    private val _libraryDetails = MutableStateFlow<JellyfinItem?>(null)
    val libraryDetails: StateFlow<JellyfinItem?> = _libraryDetails.asStateFlow()

    private val _featuredItems = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val featuredItems: StateFlow<List<JellyfinItem>> = _featuredItems.asStateFlow()

    private val _libraryItems = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val libraryItems: StateFlow<List<JellyfinItem>> = _libraryItems.asStateFlow()

    private fun fetchAllLibraryItems() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
 try {
 _libraryItems.value = repository.getLibraryItems(libraryId)
 } catch (e: Exception) {
 _error.value = e.message
 } finally {
                _isLoading.value = false
 }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()

}