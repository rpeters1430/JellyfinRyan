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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.example.jellyfinryan.ui.common.UiState // Import UiState
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: JellyfinRepository,
    savedStateHandle: SavedStateHandle,
    application: Application // Inject Application for AndroidViewModel
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState<String?>>(UiState.Loading)
    val uiState = _uiState.asStateFlow() // Expose UiState as StateFlow

    private val itemId: String = savedStateHandle.get<String>("itemId") ?: ""

    private var _player: ExoPlayer? = null
    val player: ExoPlayer?
        get() = _player

    init {
        _player = ExoPlayer.Builder(getApplication()).build()
        fetchMediaUrl(itemId)
    }

    private fun fetchMediaUrl(itemId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading // Emit Loading state
            try {
                val item = repository.getItemDetails(itemId) // Assuming this function exists
                val url = when (item?.type) {
                    "Episode" -> repository.getEpisodeStreamUrl(itemId) // Assuming this function exists
                    "Movie" -> repository.getMovieStreamUrl(itemId) // Assuming you'll add this function
                    else -> null // Handle other types or null item
                }
                _uiState.value = UiState.Success(url) // Emit Success state with the URL
                if (url != null) {
                    val mediaItem = MediaItem.fromUri(url)
                    _player?.setMediaItem(mediaItem)
                    _player?.prepare()
                    _player?.play()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unknown error occurred") // Emit Error state
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _player?.release()
        _player = null
    }
}