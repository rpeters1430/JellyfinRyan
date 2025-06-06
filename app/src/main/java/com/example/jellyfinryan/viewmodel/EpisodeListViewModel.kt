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
class EpisodeListViewModel @Inject constructor(
    private val repository: JellyfinRepository
) : ViewModel() {

    private val _episodes = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val episodes: StateFlow<List<JellyfinItem>> = _episodes

    fun loadEpisodes(seasonId: String) {
        viewModelScope.launch {
            repository.getEpisodeItems(seasonId).collect {
                _episodes.value = it
            }
        }
    }

    fun getServerUrl(): String = repository.getServerUrl()
}
