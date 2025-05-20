package com.example.jellyfinryan.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.api.model.JellyfinItemDetails
import com.example.jellyfinryan.api.model.ShowSeason
import com.example.jellyfinryan.ui.navigation.Screen // Your Screen class
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShowDetailsScreenUiState(
    val isLoading: Boolean = true,
    val itemDetails: JellyfinItemDetails? = null,
    val seasons: List<UiShowSeason> = emptyList(), // Seasons with prepared image URLs
    val error: String? = null,
    val itemPosterUrl: String? = null,
    val itemBackdropUrl: String? = null
)

// Helper data class to hold season info along with its pre-constructed poster URL
data class UiShowSeason(
    val season: ShowSeason,
    val posterUrl: String?
)

@HiltViewModel
class ShowDetailsViewModel @Inject constructor(
    private val repository: JellyfinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>(Screen.ShowDetails.ARG_ITEM_ID)!!

    private val _uiState = MutableStateFlow(ShowDetailsScreenUiState())
    val uiState: StateFlow<ShowDetailsScreenUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Fetch item details
                val detailsResult = repository.getItemDetails(itemId)

                detailsResult.onSuccess { details ->
                    val posterUrl = repository.getImageUrl(
                        itemId = details.id,
                        imageTag = details.imageTags?.get("Primary"),
                        imageType = "Primary",
                        maxWidth = 400, // Adjust as needed for your UI
                        maxHeight = 600
                    )
                    val backdropUrl = repository.getImageUrl(
                        itemId = details.id,
                        imageTag = details.imageTags?.get("Backdrop") ?: details.backdropImageTags?.firstOrNull(),
                        imageType = "Backdrop",
                        maxWidth = 1280 // Adjust for backdrop
                    )

                    _uiState.update {
                        it.copy(
                            itemDetails = details,
                            itemPosterUrl = posterUrl,
                            itemBackdropUrl = backdropUrl,
                            // Keep isLoading true if we need to fetch seasons, otherwise false
                            isLoading = details.type == "Series"
                        )
                    }

                    // If it's a series, fetch seasons
                    if (details.type == "Series") {
                        val seasonsResult = repository.getShowSeasons(details.id)
                        seasonsResult.onSuccess { seasonsList ->
                            val uiSeasons = seasonsList.mapNotNull { season ->
                                // Skip seasons with no ID or indexNumber if they are not useful
                                if (season.id.isBlank() || season.indexNumber == null) return@mapNotNull null
                                val seasonPosterUrl = repository.getImageUrl(
                                    itemId = season.id, // Season's own ID
                                    imageTag = season.imageTags?.get("Primary"),
                                    imageType = "Primary",
                                    maxWidth = 300, // Adjust as needed
                                    maxHeight = 450
                                )
                                UiShowSeason(season, seasonPosterUrl)
                            }.sortedBy { it.season.indexNumber } // Sort by season number

                            _uiState.update { it.copy(isLoading = false, seasons = uiSeasons, error = null) }
                        }.onFailure { seasonError ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Failed to load seasons: ${seasonError.localizedMessage}"
                                )
                            }
                        }
                    } else {
                        // Not a series, no seasons to load
                        _uiState.update { it.copy(isLoading = false) }
                    }

                }.onFailure { detailsError ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load item details: ${detailsError.localizedMessage}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "An unexpected error occurred: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}