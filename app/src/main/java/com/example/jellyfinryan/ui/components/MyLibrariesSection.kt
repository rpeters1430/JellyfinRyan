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
fun MyLibrariesSection(
    libraries: List<JellyfinItem>,
    serverUrl: String,
    onLibraryClick: (String) -> Unit,
    onLibraryFocus: (JellyfinItem) -> Unit,
    modifier: Modifier = Modifier,
    sdkRepository: JellyfinSdkRepository? = null
) {
    if (libraries.isEmpty()) return

    Column(
        modifier = modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "My Libraries",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            items(libraries) { library ->
                // FIXED: Use LibraryCard instead of HorizontalMediaCard to remove text overlay
                LibraryCard(
                    item = library,
                    serverUrl = serverUrl,
                    onClick = { onLibraryClick(library.Id) },
                    onFocus = onLibraryFocus,
                    sdkRepository = sdkRepository
                )
            }
        }
    }
}
