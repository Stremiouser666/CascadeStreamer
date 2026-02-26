package com.cascadestreamer.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

enum class Screen {
    HOME, SETTINGS, INFO, PLAYER
}

@Composable
fun CascadeStreamerApp() {
    val appState = remember { AppState() }
    val currentScreen = remember { mutableStateOf(Screen.HOME) }
    val selectedVideo = remember { mutableStateOf<Video?>(null) }
    
    when (currentScreen.value) {
        Screen.HOME -> HomeScreen(
            appState = appState,
            onVideoSelected = { video ->
                selectedVideo.value = video
                currentScreen.value = Screen.PLAYER
            },
            onPlaylistSelected = { playlist ->
                appState.selectPlaylist(playlist)
            },
            onSettingsClick = {
                currentScreen.value = Screen.SETTINGS
            },
            onInfoClick = {
                currentScreen.value = Screen.INFO
            }
        )
        
        Screen.SETTINGS -> SettingsScreen(
            appState = appState,
            onBack = { currentScreen.value = Screen.HOME },
            onQuality = { /* TODO: Quality settings */ }
        )
        
        Screen.INFO -> InfoScreen(
            onBack = { currentScreen.value = Screen.HOME }
        )
        
        Screen.PLAYER -> {
            selectedVideo.value?.let { video ->
                PlayerScreen(
                    video = video,
                    onBack = { currentScreen.value = Screen.HOME },
                    appState = appState
                )
            }
        }
    }
}

@Composable
fun PlayerScreen(
    video: Video,
    onBack: () -> Unit,
    appState: AppState
) {
    androidx.compose.material3.Text(
        "Playing: ${video.title}",
        modifier = Modifier.fillMaxSize()
    )
}
