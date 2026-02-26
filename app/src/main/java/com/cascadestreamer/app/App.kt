package com.cascadestreamer.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

enum class Screen {
    HOME, SETTINGS, INFO, PLAYER, QUALITY
}

@Composable
fun CascadeStreamerApp() {
    val appState = remember { AppState() }
    val currentScreen = remember { mutableStateOf(Screen.HOME) }
    val selectedVideo = remember { mutableStateOf<Video?>(null) }
    val selectedQuality = remember { mutableStateOf("720p") }
    
    when (currentScreen.value) {
        Screen.HOME -> HomeScreen(
            appState = appState,
            onVideoSelected = { video ->
                selectedVideo.value = video
                currentScreen.value = Screen.QUALITY
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
            onQuality = { currentScreen.value = Screen.QUALITY }
        )
        
        Screen.INFO -> InfoScreen(
            onBack = { currentScreen.value = Screen.HOME }
        )
        
        Screen.QUALITY -> {
            selectedVideo.value?.let { video ->
                QualitySelectionScreen(
                    availableQualities = appState.availableQualities.value,
                    selectedQuality = selectedQuality.value,
                    onQualitySelected = { quality ->
                        selectedQuality.value = quality
                        currentScreen.value = Screen.PLAYER
                    },
                    onBack = { currentScreen.value = Screen.HOME }
                )
            }
        }
        
        Screen.PLAYER -> {
            selectedVideo.value?.let { video ->
                VideoPlayerScreen(
                    video = video,
                    quality = selectedQuality.value,
                    onBack = { currentScreen.value = Screen.HOME },
                    appState = appState
                )
            }
        }
    }
}
