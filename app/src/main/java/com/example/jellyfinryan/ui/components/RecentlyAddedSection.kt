package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.JellyfinSdkRepository

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun RecentlyAddedSection(
    title: String,
    items: List<JellyfinItem>,
    serverUrl: String,
    onItemClick: (String) -> Unit,
    onItemFocus: (JellyfinItem) -> Unit,
    modifier: Modifier = Modifier,
    sdkRepository: JellyfinSdkRepository? = null
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {            items(items) { item ->
                HorizontalMediaCard(
                    item = item,
                    serverUrl = serverUrl,
                    onClick = { onItemClick(item.Id) },
                    onFocus = onItemFocus,
                    sdkRepository = sdkRepository
                )
            }
        }
    }
}
