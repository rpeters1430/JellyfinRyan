package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.example.jellyfinryan.ui.components.LibraryRow
import com.example.jellyfinryan.viewmodel.HomeViewModel
import com.example.jellyfinryan.models.Episode

/**
 * Composable function that displays the home screen of the Jellyfin application.
 *
 * This screen shows:
 * - A "Jellyfin" title.
 * - A "Continue Watching" row if there are items to continue watching.
 * - A "Recently Added" row if there are recently added items.
 * - A "Libraries" section that displays available libraries as clickable cards.
 *
 * It uses a [HomeViewModel] to fetch data and manages a loading state.
 *
 * @param viewModel The [HomeViewModel] instance used to manage the data and logic for this screen. Defaults to an instance provided by Hilt.
 * @param onBrowseLibrary A lambda function that is invoked when a library card is clicked. It receives the ID of the clicked library as a [String].
 * @param onItemClick A lambda function that is invoked when an item within a library row (e.g., "Continue Watching", "Recently Added") is clicked. It receives the ID of the clicked item as a [String].
 */
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
    val episodes = remember { mutableStateListOf<Episode>() }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadUserViews()
        viewModel.loadRecentItems()
        viewModel.loadContinueWatching()
        isLoading.value = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Surface(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
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
    }
}