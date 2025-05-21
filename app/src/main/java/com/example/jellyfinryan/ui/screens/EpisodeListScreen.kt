package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.runtime.*
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.isFocused
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import com.example.jellyfinryan.ui.components.MediaItemCard
import androidx.tv.foundation.lazy.list.rememberLazyListState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.viewmodel.EpisodeListViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
    seasonId: String,
    onEpisodeClick: (String) -> Unit = {}, // Added default empty lambda
    viewModel: EpisodeListViewModel = hiltViewModel()
) {
    val episodes by viewModel.episodes.collectAsState()

    LaunchedEffect(seasonId) {
        viewModel.loadEpisodes(seasonId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Episodes",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(episodes) { episode ->
                MediaItemCard(
                    item = episode,
                    serverUrl = viewModel.getServerUrl(),
                    onClick = { /* TODO: Handle episode click */ }
                )
            }
        }
    }
}
