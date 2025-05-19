package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadUserViews()
        viewModel.loadRecentItems()
        viewModel.loadContinueWatching()
        isLoading.value = false
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isLoading.value) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    Text(
                        text = "Welcome to Jellyfin",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                if (continueWatching.isNotEmpty()) {
                    item {
                        LibraryRow(
                            title = "Continue Watching",
                            items = continueWatching,
                            onItemClick = onItemClick,
                            serverUrl = viewModel.getServerUrl()
                        )
                    }
                }

                if (recentItems.isNotEmpty()) {
                    item {
                        LibraryRow(
                            title = "Recently Added",
                            items = recentItems,
                            onItemClick = onItemClick,
                            serverUrl = viewModel.getServerUrl()
                        )
                    }
                }

                if (libraries.isNotEmpty()) {
                    item {
                        Text(
                            text = "Your Libraries",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(libraries) { library ->
                                Card(
                                    onClick = { onBrowseLibrary(library.Id) },
                                    modifier = Modifier
                                        .width(260.dp)
                                        .height(140.dp)
                                        .focusable()
                                ) {
                                    Box(
                                        Modifier.fillMaxSize(),
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