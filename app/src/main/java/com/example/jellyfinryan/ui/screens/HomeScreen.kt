package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.tv.material3.*
import com.example.jellyfinryan.viewmodel.HomeViewModel
import com.example.jellyfinryan.models.Episode
import com.example.jellyfinryan.ui.components.LibraryRow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val episodes by viewModel.episodes.collectAsState()
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
                                onItemClick = { itemId ->
                                    navController.navigate("seasons/$itemId")
                                },
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
                                onItemClick = { itemId ->
                                    navController.navigate("seasons/$itemId")
                                },
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
                                Text(text = "No libraries found.")
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
                                        onClick = { onBrowseLibrary(library.id) },
                                        modifier = Modifier
                                            .height(120.dp)
                                            .weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = library.name,
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







