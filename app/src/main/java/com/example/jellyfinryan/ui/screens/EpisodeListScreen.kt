package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import com.example.jellyfinryan.viewmodel.EpisodeListViewModel

@Composable
fun EpisodeListScreen(
    seasonId: String,
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
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    episode.getImageUrl(viewModel.getServerUrl())?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = episode.Name,
                            modifier = Modifier
                                .height(240.dp)
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                        )
                    }
                    Text(
                        text = episode.Name,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(160.dp)
                    )
                }
            }
        }
    }
}
