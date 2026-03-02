package com.cascadestreamer.app.ui

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
import com.cascadestreamer.app.DebugLogger
import com.cascadestreamer.app.data.Video
import com.cascadestreamer.app.states.AppState

@Composable
fun VideoPlayerScreen(
    video: Video,
    quality: String,
    onBack: () -> Unit,
    appState: AppState
) {
    val context = LocalContext.current
    val debugLogger = remember { DebugLogger(context) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        debugLogger.log("VideoPlayer", "Initializing player for: ${video.title}")
        debugLogger.log("VideoPlayer", "URL: ${video.url}")
        debugLogger.log("VideoPlayer", "Quality: $quality")
        
        try {
            val player = ExoPlayer.Builder(context).build()
            exoPlayer = player
            debugLogger.log("VideoPlayer", "ExoPlayer created successfully")
            
            val mediaItem = MediaItem.fromUri(video.url)
            debugLogger.log("VideoPlayer", "MediaItem created from URI")
            
            player.setMediaItem(mediaItem)
            debugLogger.log("VideoPlayer", "MediaItem set to player")
            
            player.prepare()
            debugLogger.log("VideoPlayer", "Player prepared")
            
            player.playWhenReady = true
            isPlaying = true
            debugLogger.log("VideoPlayer", "Playback started")
            
            if (video.currentPosition > 0) {
                player.seekTo(video.currentPosition)
                debugLogger.log("VideoPlayer", "Seeked to position: ${video.currentPosition}")
            }
        } catch (e: Exception) {
            debugLogger.log("VideoPlayer", "ERROR initializing player", e)
            errorMessage = "Error: ${e.message}"
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
                    debugLogger.log("VideoPlayer", "Creating PlayerView")
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
                
                // Show debug info if error
                if (errorMessage.isNotEmpty()) {
                    Text(
                        errorMessage,
                        fontSize = 12.sp,
                        color = Color.Red,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(12.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        exoPlayer?.let {
                            if (it.isPlaying) {
                                it.pause()
                                isPlaying = false
                                debugLogger.log("VideoPlayer", "Playback paused")
                            } else {
                                it.play()
                                isPlaying = true
                                debugLogger.log("VideoPlayer", "Playback resumed")
                            }
                        }
                    }
                ) {
                    Text(if (isPlaying) "Pause" else "Play")
                }
                
                Button(onClick = {
                    exoPlayer?.let {
                        appState.updateVideoProgress(video.id, it.currentPosition)
                        debugLogger.log("VideoPlayer", "Position saved: ${it.currentPosition}")
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
                debugLogger.log("VideoPlayer", "Player disposed, position saved: ${player.currentPosition}")
                player.release()
            }
        }
    }
}
