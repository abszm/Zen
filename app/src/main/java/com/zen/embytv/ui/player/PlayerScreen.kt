package com.zen.embytv.ui.player

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zen.embytv.ui.theme.AppColors

@Composable
fun PlayerScreen(viewModel: PlayerViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var retryToken by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopProgressReporting()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    LaunchedEffect(state.streamUrl, retryToken) {
        if (state.streamUrl.isBlank()) {
            return@LaunchedEffect
        }
        exoPlayer?.release()
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    viewModel.onPlaybackError(error.message ?: "Playback failed")
                }
            })
            setMediaItem(MediaItem.fromUri(state.streamUrl))
            prepare()
            playWhenReady = true
        }
        viewModel.clearPlaybackError()
        val playerRef = exoPlayer
        if (playerRef != null) {
            viewModel.startProgressReporting(positionProvider = { playerRef.currentPosition })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface, RoundedCornerShape(24.dp))
            .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (state.mediaTitle.isBlank()) "Player" else state.mediaTitle,
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.TextPrimary,
        )

        if (state.streamUrl.isBlank()) {
            Text(
                text = "Select media from Library to start playback.",
                color = AppColors.TextSecondary,
            )
        } else {
            state.playbackErrorMessage?.let { message ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Playback error: $message",
                        color = Color(0xFFFFA6A6),
                    )
                    Button(onClick = {
                        viewModel.clearPlaybackError()
                        retryToken += 1
                    }) {
                        Text("Retry")
                    }
                }
            }
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(560.dp),
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        useController = true
                        player = exoPlayer
                    }
                },
                update = { it.player = exoPlayer },
            )
        }
    }
}
