package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.viewmodel.EpisodeListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTvMaterial3Api::class)
@Composable
fun EpisodeListScreen(
    seasonId: String,
    onBackClick: () -> Unit,
    onEpisodeClick: (String) -> Unit = {},
    viewModel: EpisodeListViewModel = hiltViewModel()
) {
    val episodes by viewModel.episodes.collectAsState()

    LaunchedEffect(seasonId) {
        viewModel.loadEpisodes(seasonId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                navigationIconContentColor = Color.White,
                titleContentColor = Color.White
            ),
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            title = {
                Text(
                    text = "Episodes",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(episodes) { episode ->
                EpisodeCard(
                    episode = episode,
                    serverUrl = viewModel.getServerUrl(),
                    onClick = { onEpisodeClick(episode.Id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpisodeCard(
    episode: JellyfinItem,
    serverUrl: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Try to get horizontal images for episodes
    val imageUrl = episode.getImageUrl(serverUrl, type = "Banner")
        ?: episode.getImageUrl(serverUrl, type = "Thumb")
        ?: episode.getBackdropImageUrl(serverUrl)
        ?: episode.getPrimaryImageUrl(serverUrl)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp) // 16:9 aspect ratio
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        colors = CardDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.3f),
            focusedContainerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Episode thumbnail
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(16f / 9f)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = episode.Name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.Gray.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Episode info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = episode.Name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                episode.Overview?.let { overview ->
                    Text(
                        text = overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = if (isFocused) 4 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Episode metadata
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    episode.getRunTimeMinutes()?.let { runtime ->
                        Text(
                            text = "${runtime}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    episode.CommunityRating?.let { rating ->
                        Text(
                            text = "★ ${String.format("%.1f", rating)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Yellow
                        )
                    }
                }
            }
        }
    }
}
