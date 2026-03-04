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
fun CascadeStreamerApp(onExitApp: () -> Unit = {}) {
    val appState = remember { AppState() }
    val currentScreen = remember { mutableStateOf(Screen.HOME) }
    val selectedVideo = remember { mutableStateOf<Video?>(null) }
    val selectedQuality = remember { mutableStateOf("720p") }
    val backPressCount = remember { mutableStateOf(0) }
    
    // Back button on HOME screen = exit app
    BackHandler(enabled = currentScreen.value == Screen.HOME) {
        backPressCount.value++
        if (backPressCount.value >= 2) {
            onExitApp()
        }
    }
    
    // Back button on other screens = go HOME
    BackHandler(enabled = currentScreen.value != Screen.HOME) {
        currentScreen.value = Screen.HOME
        backPressCount.value = 0
    }
    
    when (currentScreen.value) {
        Screen.HOME -> HomeScreen(
            appState = appState,
            onVideoSelected = { video ->
                selectedVideo.value = video
                appState.playVideo(video)
                currentScreen.value = Screen.QUALITY
                backPressCount.value = 0
            },
            onPlaylistSelected = { playlist ->
                appState.selectPlaylist(playlist)
            },
            onSettingsClick = {
                currentScreen.value = Screen.SETTINGS
                backPressCount.value = 0
            },
            onInfoClick = {
                currentScreen.value = Screen.INFO
                backPressCount.value = 0
            }
        )
        
        Screen.SETTINGS -> SettingsScreen(
            appState = appState,
            onBack = { 
                currentScreen.value = Screen.HOME
                backPressCount.value = 0
            },
            onQuality = { currentScreen.value = Screen.QUALITY }
        )
        
        Screen.INFO -> InfoScreen(
            onBack = { 
                currentScreen.value = Screen.HOME
                backPressCount.value = 0
            }
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
                    onBack = { 
                        currentScreen.value = Screen.HOME
                        backPressCount.value = 0
                    }
                )
            }
        }
        
        Screen.PLAYER -> {
            selectedVideo.value?.let { video ->
                VideoPlayerScreen(
                    video = video,
                    quality = selectedQuality.value,
                    onBack = { 
                        currentScreen.value = Screen.HOME
                        backPressCount.value = 0
                    },
                    appState = appState
                )
            }
        }
    }
}
