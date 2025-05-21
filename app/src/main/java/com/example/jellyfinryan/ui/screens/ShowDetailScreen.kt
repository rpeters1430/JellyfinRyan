package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val showDetails by viewModel.showDetails.collectAsStateWithLifecycle()
    val seasons by viewModel.seasons.collectAsStateWithLifecycle() ?: emptyList() // Ensure seasons is not null
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var focusedBackground by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        focusedBackground?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f // Adjust opacity as needed
            )
        }

        // Add a semi-transparent overlay
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))) // Adjust overlay color and opacity

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: $error", color = Color.Red)
                }
            }
            else -> {
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

        showDetails?.let { show ->
            AsyncImage(
                model = show.getImageUrl(viewModel.getServerUrl()),
                contentDescription = show.Name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // Adjust height as needed
                    .clip(MaterialTheme.shapes.medium)
            )

            Text(
                text = show.Name,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            show.Overview?.let { overview ->
                Text(
                    text = overview,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(seasons) { season ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        onClick = { onSeasonClick(season.Id) },
                        modifier = Modifier
                            .width(200.dp)
                            .height(300.dp)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedBackground = season.getImageUrl(viewModel.getServerUrl())
                                } else {
                                    focusedBackground = null // Clear background when focus leaves
                                }
                            }
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
                            .width(200.dp) // Match card width
                    )
                }
            }
        }
            }
        }
    }
}
