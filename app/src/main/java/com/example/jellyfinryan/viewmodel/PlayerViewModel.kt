package com.example.jellyfinryan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: JellyfinRepository,
    savedStateHandle: SavedStateHandle,
    application: Application // Inject Application for AndroidViewModel
) : AndroidViewModel(application) {

    private val _mediaUrl = MutableStateFlow<String?>(null)
    val mediaUrl: StateFlow<String?> = _mediaUrl.asStateFlow()

    private val episodeId: String = savedStateHandle.get<String>("itemId") ?: ""

    init {
        fetchMediaUrl(episodeId)
    }

    private fun fetchMediaUrl(episodeId: String) {
        viewModelScope.launch {
            // Assuming repository has a function like getEpisodeStreamUrl
            // You might need to implement this in your JellyfinRepository
            val url = repository.getEpisodeStreamUrl(episodeId)
            _mediaUrl.value = url
        }
    }
}