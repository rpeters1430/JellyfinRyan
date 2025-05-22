package com.example.jellyfinryan.ui.screens

import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.example.jellyfinryan.viewmodel.PlayerViewModel
import androidx.media3.common.Player


@Composable
fun PlayerScreen(
    episodeId: String,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val mediaUrl by viewModel.mediaUrl.collectAsState()
 val errorMessage by viewModel.error.collectAsState()
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(player?.isPlaying == true) }
    var isBuffering by remember { mutableStateOf(false) }
    val player = viewModel.player

    player?.let {
 LaunchedEffect(it) {
            isPlaying = it.isPlaying

            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    this@PlayerScreen.isPlaying = isPlaying
                }
            }
                it.removeListener(listener)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                FrameLayout(context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    val playerView = PlayerView(context).apply {
                        player = this@PlayerScreen.player
                        useController = false // Disable default controls
                        layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    }
                    addView(playerView)
                }
            },
            update = { frameLayout ->
                // Update logic if needed based on Compose state changes
            }
        )

        if (isBuffering) {
            LinearProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (errorMessage != null) {
            Text(text = errorMessage.orEmpty(), modifier = Modifier.align(Alignment.Center))
        }

        FloatingActionButton(
            onClick = { player?.playWhenReady = !(player?.playWhenReady ?: true) },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause")
        }
    }
}