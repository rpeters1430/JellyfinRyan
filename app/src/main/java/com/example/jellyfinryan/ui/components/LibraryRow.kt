package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import com.example.jellyfinryan.api.model.JellyfinItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryRow(
    title: String,
    items: List<JellyfinItem>,
    onItemClick: (String) -> Unit,
    serverUrl: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                MediaCard(
                    item = item,
                    serverUrl = serverUrl,
                    onClick = { onItemClick(item.Id) },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}