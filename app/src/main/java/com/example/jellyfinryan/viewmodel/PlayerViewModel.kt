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
import kotlinx.coroutines.flow.StateFlow
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
            _isLoading.value = true
            _error.value = null
            try {
                val item = repository.getItemDetails(itemId) // Assuming this function exists
                val url = when (item?.type) {
                    "Episode" -> repository.getEpisodeStreamUrl(itemId) // Assuming this function exists
                    "Movie" -> repository.getMovieStreamUrl(itemId) // Assuming you'll add this function
                    else -> null // Handle other types or null item
                }
                _mediaUrl.value = url
                if (url != null) {
                    val mediaItem = MediaItem.fromUri(url)
                    _player?.setMediaItem(mediaItem)
                    _player?.prepare()
                    _player?.play()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _player?.release()
        _player = null
    }
}