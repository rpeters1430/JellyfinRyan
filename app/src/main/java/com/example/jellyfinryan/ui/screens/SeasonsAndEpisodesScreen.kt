package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.example.jellyfinryan.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeasonsAndEpisodesScreen(
    itemId: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val seasons by viewModel.getSeasons(itemId).collectAsState()

    if (seasons.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No seasons found.")
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(seasons) { season ->
                Card(onClick = { /* Navigate to episodes within this season */ }) {
                    Text(text = "Season ${season.seasonNumber}")
                }
            }
        }
    }
}