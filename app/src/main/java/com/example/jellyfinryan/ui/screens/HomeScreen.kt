package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onBrowseLibrary: (String) -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val libraries by viewModel.libraries.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState(initial = emptyMap())
    var focusedBackground by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image layer
        focusedBackground?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.2f
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Your Libraries",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(libraries) { library ->
                        var isFocused by remember { mutableStateOf(false) }
                        val imageUrl = library.getImageUrl(viewModel.getServerUrl())

                        Card(
                            onClick = { onBrowseLibrary(library.Id) },
                            modifier = Modifier
                                .width(320.dp)
                                .height(180.dp)
                                .onFocusChanged {
                                    isFocused = it.isFocused
                                    if (it.isFocused) {
                                        focusedBackground = imageUrl
                                    }
                                }
                                .focusable()
                                .clip(MaterialTheme.shapes.extraLarge),
                            shape = MaterialTheme.shapes.extraLarge,
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = 8.dp,
                                focusedElevation = 12.dp
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (imageUrl != null) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = library.Name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Black.copy(alpha = 0.6f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                Text(
                                    text = library.Name,
                                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            items(libraries) { library ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Recently Added in ${library.Name}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val items = libraryItems[library.Id] ?: emptyList()

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items) { item ->
                            var isFocused by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .width(160.dp)
                                    .onFocusChanged {
                                        isFocused = it.isFocused
                                        if (it.isFocused) {
                                            focusedBackground = item.getImageUrl(viewModel.getServerUrl())
                                        }
                                    }
                                    .focusable(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item.getImageUrl(viewModel.getServerUrl())?.let { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = item.Name,
                                        modifier = Modifier
                                            .height(240.dp)
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.large)
                                    )
                                }
                                Text(
                                    text = item.Name,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}






