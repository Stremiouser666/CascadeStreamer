package com.cascadestreamer.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.cascadestreamer.app.data.Video
import com.cascadestreamer.app.states.AppState
import com.cascadestreamer.app.ui.HomeScreen
import com.cascadestreamer.app.ui.SettingsScreen
import com.cascadestreamer.app.ui.InfoScreen
import com.cascadestreamer.app.ui.VideoPlayerScreen
import com.cascadestreamer.app.ui.QualitySelectionScreen

enum class Screen {
    HOME, SETTINGS, INFO, PLAYER, QUALITY
}

@Composable
fun CascadeStreamerApp() {
    val appState = remember { AppState() }
    val currentScreen = remember { mutableStateOf(Screen.HOME) }
    val selectedVideo = remember { mutableStateOf<Video?>(null) }
    val selectedQuality = remember { mutableStateOf("720p") }
    
    BackHandler(enabled = currentScreen.value != Screen.HOME) {
        currentScreen.value = Screen.HOME
    }
    
    when (currentScreen.value) {
        Screen.HOME -> HomeScreen(
            appState = appState,
            onVideoSelected = { video ->
                selectedVideo.value = video
                appState.playVideo(video)  // Populate qualities
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
