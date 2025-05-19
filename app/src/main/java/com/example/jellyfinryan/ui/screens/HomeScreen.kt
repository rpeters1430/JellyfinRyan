package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*
import com.example.jellyfinryan.ui.components.LibraryRow
import com.example.jellyfinryan.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onBrowseLibrary: (String) -> Unit,
    onItemClick: (String) -> Unit
) {
    val libraries by viewModel.libraries.collectAsState()
    val recentItems by viewModel.recentItems.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserViews()
        viewModel.loadRecentItems()
        viewModel.loadContinueWatching()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        TvLazyColumn(
            modifier = Modifier.padding(top = 48.dp, start = 48.dp, end = 48.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Jellyfin",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            if (continueWatching.isNotEmpty()) {
                item {
                    LibraryRow(
                        title = "Continue Watching",
                        items = continueWatching,
                        onItemClick = onItemClick,
                        serverUrl = viewModel.getServerUrl(),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }

            if (recentItems.isNotEmpty()) {
                item {
                    LibraryRow(
                        title = "Recently Added",
                        items = recentItems,
                        onItemClick = onItemClick,
                        serverUrl = viewModel.getServerUrl(),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }

            item {
                Text(
                    text = "Libraries",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            if (libraries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        libraries.forEach { library ->
                            Card(
                                onClick = { onBrowseLibrary(library.Id) },
                                modifier = Modifier
                                    .height(120.dp)
                                    .weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = library.Name,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}