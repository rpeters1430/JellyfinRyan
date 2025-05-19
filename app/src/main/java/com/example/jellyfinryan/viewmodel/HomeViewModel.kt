package com.example.jellyfinryan.viewmodel

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
class HomeViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _libraries = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val libraries: StateFlow<List<JellyfinItem>> = _libraries.asStateFlow()

    private val _recentItems = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val recentItems: StateFlow<List<JellyfinItem>> = _recentItems.asStateFlow()

    private val _continueWatching = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val continueWatching: StateFlow<List<JellyfinItem>> = _continueWatching.asStateFlow()

    fun loadUserViews() {
        viewModelScope.launch {
            repository.getUserViews().collect { items ->
                _libraries.value = items
            }
        }
    }

    fun loadRecentItems() {
        // For now, just use an empty list
        _recentItems.value = emptyList()
    }

    fun loadContinueWatching() {
        // For now, just use an empty list
        _continueWatching.value = emptyList()
    }

    fun getServerUrl(): String = repository.getServerUrl()
}