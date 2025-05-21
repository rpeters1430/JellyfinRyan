package com.example.jellyfinryan.ui.screens

import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import androidx.compose.material3.Slider // Import Slider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.example.jellyfinryan.viewmodel.PlayerViewModel
import androidx.media3.common.Player
import java.util.concurrent.TimeUnit


@Composable
fun PlayerScreen(
    episodeId: String,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val mediaUrl by viewModel.mediaUrl.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val context = LocalContext.current
    val player = viewModel.player

    // State for playback position and duration
    var currentPosition by remember { mutableStateOf(0L) }
    var bufferedPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(player?.isPlaying == true) }
    var isBuffering by remember { mutableStateOf(false) }


    // Listen to player state changes
    player?.let {
        LaunchedEffect(it) {
            isPlaying = it.isPlaying
            isBuffering = it.playbackState == Player.STATE_BUFFERING
            currentPosition = it.currentPosition
            bufferedPosition = it.bufferedPosition
            duration = it.duration


            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    this@PlayerScreen.isPlaying = isPlaying
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) || events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED) || events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                        currentPosition = player.currentPosition
                    }
                    if (events.contains(Player.EVENT_BUFFERED_POSITION_CHANGED)) {
                         bufferedPosition = player.bufferedPosition
                    }
                     if (events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                         duration = player.duration
                     }
                }

                override fun onBufferedPositionChanged(bufferedPosition: Long) {
                    this@PlayerScreen.bufferedPosition = bufferedPosition
                }

                override fun onDurationChanged(duration: Long) {
                    this@PlayerScreen.duration = duration
                }
            }

            it.addListener(listener)
            onDispose {
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rewind button
            IconButton(onClick = {
                player?.seekBack() // Use seekBack for rewind
            }) {
                Icon(imageVector = Icons.Filled.FastRewind, contentDescription = "Rewind")
            }

            // Current position text
            Text(text = formatTime(currentPosition))

            // Seek bar
            Slider(
                value = currentPosition.toFloat(),
                valueRange = 0f..duration.toFloat(),
                onValueChange = { newValue ->
                    currentPosition = newValue.toLong() // Update position as slider moves
                },
                onValueChangeFinished = { newValue ->
                    player?.seekTo(newValue.toLong()) // Seek to the final position
                },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp) // Slider takes available space
            )

            // Duration text
            Text(text = formatTime(duration))


            // Fast Forward button
            IconButton(onClick = {
                player?.seekForward() // Use seekForward for fast forward
            }) {
                Icon(imageVector = Icons.Filled.FastForward, contentDescription = "Fast Forward")
            }
        }
    }
}

// Helper function to format time
fun formatTime(millis: Long): String {
    if (millis < 0) return "00:00"

    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
