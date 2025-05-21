package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onFocusChanged
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.focusable
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.example.jellyfinryan.data.model.MediaItem

@Composable
fun HomeScreen(onItemClick: (String) -> Unit) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<HomeViewModel>()
    val mediaItems by viewModel.recentItems.collectAsState()
    var focusedBackground by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Recently Added",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(mediaItems) { item ->
                var isFocused by remember { mutableStateOf(false) }
                Card(
                    onClick = { onItemClick(item.Id) },
                    modifier = Modifier
                        .width(160.dp)
                        .onFocusChanged {
                            isFocused = it.isFocused
                            if (it.isFocused) {
                                focusedBackground = item.getImageUrl(viewModel.getServerUrl())
                            }
                        }
                        .focusable()
                        .clip(MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        AsyncImage(
                            model = item.getImageUrl(viewModel.getServerUrl()),
                            contentDescription = item.Name,
                            modifier = Modifier
                                .height(240.dp)
                                .fillMaxWidth()
                        )
                        Text(
                            text = item.Name ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}







