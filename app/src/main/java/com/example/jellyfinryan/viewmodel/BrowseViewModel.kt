package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _allItems = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val allItems: StateFlow<List<JellyfinItem>> = _allItems

    fun loadAllItems(libraryId: String) {
        viewModelScope.launch {
            repository.getLibraryItems(libraryId).collect {
                _allItems.value = it
            }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}

