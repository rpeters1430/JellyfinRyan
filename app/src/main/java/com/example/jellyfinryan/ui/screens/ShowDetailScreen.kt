package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import com.example.jellyfinryan.viewmodel.ShowDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    showId: String,
    onSeasonClick: (String) -> Unit,
    onBackClick: () -> Unit, // <-- Include the onBackClick parameter
    viewModel: ShowDetailViewModel = hiltViewModel()
) {
    val seasons by viewModel.seasons.collectAsState()

    LaunchedEffect(showId) {
        viewModel.loadSeasons(showId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = { onBackClick() }) { // <-- Use the onBackClick callback
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            title = {
                Text(text = "Seasons", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(seasons) { season ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        onClick = { onSeasonClick(season.Id) },
                        modifier = Modifier
                            .width(160.dp)
                            .height(240.dp)
                            .clip(MaterialTheme.shapes.large)
                    ) {
                        AsyncImage(
                            model = season.getImageUrl(viewModel.getServerUrl()),
                            contentDescription = season.Name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = season.Name,
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


