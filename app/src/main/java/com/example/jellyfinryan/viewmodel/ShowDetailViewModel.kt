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
class ShowDetailViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _seasons = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val seasons: StateFlow<List<JellyfinItem>> = _seasons

    fun loadSeasons(seriesId: String) {
        viewModelScope.launch {
            repository.getSeasonItems(seriesId).collect {
                _seasons.value = it
            }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}
