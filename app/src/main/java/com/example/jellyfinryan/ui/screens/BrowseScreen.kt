package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import com.example.jellyfinryan.viewmodel.BrowseViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BrowseScreen(
    libraryId: String,
    onItemClick: (String) -> Unit,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val allItems by viewModel.items.collectAsState() // <- FIXED HERE
    var scrollHorizontally by remember { mutableStateOf(false) }

    // Load the full library items for the given library
    LaunchedEffect(libraryId) {
        viewModel.loadItems(libraryId) // <- FIXED HERE
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Browsing Library",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Toggle between vertical and horizontal layout
        TextButton(onClick = { scrollHorizontally = !scrollHorizontally }) {
            Text("Switch to ${if (scrollHorizontally) "Vertical" else "Horizontal"} View")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (scrollHorizontally) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(allItems.sortedBy { it.Name }) { item ->
                    Column(
                        modifier = Modifier
                            .width(160.dp)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    // You can update background here if desired
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
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allItems.sortedBy { it.Name }) { item ->
                    Column(
                        modifier = Modifier
                            .onFocusChanged {
                                if (it.isFocused) {
                                    // You can update background here if desired
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
                                    .width(160.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }
                        Text(
                            text = item.Name,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(160.dp),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
