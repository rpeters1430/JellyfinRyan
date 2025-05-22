package com.example.jellyfinryan.ui.screens

import android.app.Activity
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility // Import AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Import clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width // Import width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView // Import LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewwind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.material3.Slider // Import Slider
import androidx.compose.material3.SliderDefaults // Import SliderDefaults
import androidx.compose.material3.MaterialTheme // Import MaterialTheme
import androidx.compose.material.icons.filled.VolumeUp // Import volume icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.Fullscreen // Import fullscreen icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert // Import morevert icon
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.example.jellyfinryan.viewmodel.PlayerViewModel
import androidx.media3.common.Player
import java.util.concurrent.TimeUnit
import android.view.WindowManager // Import WindowManager
import kotlinx.coroutines.delay // Import delay

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
    var controlsVisible by remember { mutableStateOf(true) } // State for control visibility
    var volumeSliderVisible by remember { mutableStateOf(false) } // State for volume slider visibility
    var isFullScreen by remember { mutableStateOf(false) } // State for fullscreen mode


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

        // Auto-hide controls
        LaunchedEffect(controlsVisible) {
            if (controlsVisible) {
                delay(3000L) // Hide after 3 seconds
                controlsVisible = false
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { controlsVisible = !controlsVisible }, // Toggle controls visibility on click
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

        // Play/Pause button
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.Center)
        ) {
            FloatingActionButton(
                onClick = { player?.playWhenReady = !(player?.playWhenReady ?: true) }
            ) {
                Icon(imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause")
            }
        }


        // Control Row
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f)) // Add a semi-transparent black background
                .padding(vertical = 8.dp, horizontal = 16.dp) // Add vertical and horizontal padding
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Rewind button
                IconButton(
                    onClick = {
                        player?.seekBack() // Use seekBack for rewind
                    },
                    modifier = Modifier.size(48.dp) // Make the button larger
                ) {
                    Icon(imageVector = Icons.Filled.FastRewind, contentDescription = "Rewind")
                }

                // Current position text
                Text(text = formatTime(currentPosition), color = Color.White) // Set text color to white

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
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp), // Slider takes available space
                    colors = SliderDefaults.colors( // Optional: Customize slider colors
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                // Duration text
                Text(text = formatTime(duration), color = Color.White) // Set text color to white

                // Fast Forward button
                IconButton(
                    onClick = {
                        player?.seekForward() // Use seekForward for fast forward
                    },
                    modifier = Modifier.size(48.dp) // Make the button larger
                ) {
                    Icon(imageVector = Icons.Filled.FastForward, contentDescription = "Fast Forward")
                }

                // Volume button
                IconButton(
                    onClick = {
                        volumeSliderVisible = !volumeSliderVisible // Toggle volume slider visibility
                    },
                    modifier = Modifier.size(48.dp) // Make the button larger
                ) {
                    val volumeIcon = when {
                        player?.volume == 0f -> Icons.Filled.VolumeMute
                        player?.volume ?: 0f < 0.5f -> Icons.Filled.VolumeDown
                        else -> Icons.Filled.VolumeUp
                    }
                    Icon(imageVector = volumeIcon, contentDescription = "Volume")
                }

                // Full-screen toggle button
                IconButton(
                    onClick = {
                        isFullScreen = !isFullScreen
                        val activity = LocalView.current.context as Activity
                        if (isFullScreen) {
                            activity.window.setFlags(
                                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN
                            )
                        } else {
                            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        }
                    },
                    modifier = Modifier.size(48.dp) // Make the button larger
                ) {
                    val fullscreenIcon = if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen
                    Icon(imageVector = fullscreenIcon, contentDescription = if (isFullScreen) "Exit Fullscreen" else "Enter Fullscreen")
                }

                // Options button (placeholder)
                IconButton(
                    onClick = {
                        // TODO: Implement options menu
                    },
                    modifier = Modifier.size(48.dp) // Make the button larger
                ) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Options")
                }
            }
        }

        // Volume Slider
        AnimatedVisibility(
            visible = volumeSliderVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp) // Position above the control row
                .width(48.dp) // Fixed width for the vertical slider
        ) {
             Slider(
                value = player?.volume ?: 0f,
                valueRange = 0f..1f,
                onValueChange = { newValue ->
                    player?.volume = newValue
                },
                colors = SliderDefaults.colors( // Customize slider colors
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
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