package com.example.jellyfinryan.viewmodel

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
import kotlinx.coroutines.flow.*
import androidx.lifecycle.SavedStateHandle
import javax.inject.Inject

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val repository: JellyfinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

 private val _isLoading = MutableStateFlow<Boolean>(false)
 val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

 private val _error = MutableStateFlow<String?>(null)
 val error: StateFlow<String?> = _error.asStateFlow()

 private val showId: String = savedStateHandle.get<String>("itemId") ?: ""

    private val _showDetails = MutableStateFlow<JellyfinItem?>(null)
    val showDetails: StateFlow<JellyfinItem?> = _showDetails.asStateFlow()

    private val _seasons = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val seasons: StateFlow<List<JellyfinItem>> = _seasons.asStateFlow()

 init {
 fetchShowDetails()
 fetchSeasons()
 }

 private fun fetchShowDetails() {
 viewModelScope.launch {
 repository.getItemDetails(showId).collect {
 _showDetails.value = it
 }
 }
 }

 private fun fetchSeasons() {
 viewModelScope.launch {
 repository.getLibraryItems(showId).collect { items ->
 // Assuming getLibraryItems can fetch seasons of a series when given seriesId
 _seasons.value = items
 }
 }
 }

 fun getServerUrl(): String = repository.getServerUrl()
}
