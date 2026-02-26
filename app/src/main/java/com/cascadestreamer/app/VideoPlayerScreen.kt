package com.cascadestreamer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayerScreen(
    video: Video,
    quality: String,
    onBack: () -> Unit,
    appState: AppState
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val player = ExoPlayer.Builder(context).build()
        exoPlayer = player
        
        val streamUrl = appState.getStreamUrl(video.url, quality)
        val mediaItem = MediaItem.fromUri(streamUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        
        if (video.currentPosition > 0) {
            player.seekTo(video.currentPosition)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        exoPlayer?.let { player ->
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                    }
                }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    video.title,
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(12.dp)
                )
                Text(
                    "Quality: $quality",
                    fontSize = 14.sp,
                    color = Color(0xFF64B5F6),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(12.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    exoPlayer?.let {
                        if (it.isPlaying) {
                            it.pause()
                            isPlaying = false
                        } else {
                            it.play()
                            isPlaying = true
                        }
                    }
                }) {
                    Text(if (isPlaying) "Pause" else "Play")
                }
                
                Button(onClick = {
                    exoPlayer?.let {
                        appState.updateVideoProgress(video.id, it.currentPosition)
                    }
                    onBack()
                }) {
                    Text("Back")
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.let { player ->
                appState.updateVideoProgress(video.id, player.currentPosition)
                player.release()
            }
        }
    }
}
