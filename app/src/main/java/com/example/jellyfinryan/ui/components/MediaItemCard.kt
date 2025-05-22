package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem

@Composable
fun MediaItemCard(
    item: JellyfinItem,
    serverUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(160.dp),
        scale = CardDefaults.scale(focusedScale = 1.1f)
    ) {
        Column {
            item.getImageUrl(serverUrl)?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = item.Name,
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth()
                )
            }

            Text(
                text = item.Name,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
