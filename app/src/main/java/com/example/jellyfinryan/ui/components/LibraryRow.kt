package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.JellyfinSdkRepository

@Composable
fun LibraryRow(
    title: String,
    items: List<JellyfinItem>,
    onItemClick: (String) -> Unit,
    serverUrl: String,
    onItemFocus: (JellyfinItem) -> Unit = {},
    modifier: Modifier = Modifier,
    sdkRepository: JellyfinSdkRepository? = null
) {
    Column(modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {            items(items) { item ->
                MediaCard(
                    item = item,
                    serverUrl = serverUrl,
                    onClick = { onItemClick(item.Id) },
                    onFocus = { onItemFocus(item) },
                    sdkRepository = sdkRepository
                )
            }
        }
    }
}
