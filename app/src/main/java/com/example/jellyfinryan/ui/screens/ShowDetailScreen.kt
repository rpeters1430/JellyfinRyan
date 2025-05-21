// ShowDetailScreen.kt
package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import coil.compose.AsyncImage
import com.example.jellyfinryan.data.model.Season
import com.example.jellyfinryan.data.model.MediaItem

@Composable
fun ShowDetailScreen(
    showId: String,
    seasons: List<Season>,
    episodesBySeason: Map<String, List<MediaItem>>,
    onBackClick: () -> Unit,
    onSeasonClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Seasons",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        LazyColumn {
            items(seasons) { season ->
                Text(
                    text = season.Name ?: "Season ${'$'}{season.IndexNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(episodesBySeason[season.Id] ?: emptyList()) { ep ->
                        Card(
                            onClick = { /* handle episode click */ },
                            modifier = Modifier.size(width = 200.dp, height = 120.dp)
                        ) {
                            AsyncImage(
                                model = ep.getImageUrl(),
                                contentDescription = ep.Name,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = ep.Name ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}



