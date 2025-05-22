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

    private val _items = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val items: StateFlow<List<JellyfinItem>> = _items

    fun loadItems(libraryId: String) {
        viewModelScope.launch {
            repository.getLibraryItemsFull(libraryId).collect {
                _items.value = it
            }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}


