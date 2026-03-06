package com.cascadestreamer.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.cascadestreamer.app.data.Video
import com.cascadestreamer.app.managers.TVMazeShow
import com.cascadestreamer.app.managers.TVMazeRating
import com.cascadestreamer.app.managers.TVMazeImage
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.states.AppState
import com.cascadestreamer.app.ui.HomeScreen
import com.cascadestreamer.app.ui.SettingsScreen
import com.cascadestreamer.app.ui.InfoScreen
import com.cascadestreamer.app.ui.VideoPlayerScreen
import com.cascadestreamer.app.ui.QualitySelectionScreen
import com.cascadestreamer.app.ui.SeriesDetailScreen
import com.cascadestreamer.app.ui.SeriesData

enum class Screen {
    HOME, SETTINGS, INFO, PLAYER, QUALITY, SERIES
}

@Composable
fun CascadeStreamerApp(
    appState: AppState = AppState(),
    onExitApp: () -> Unit = {}
) {
    val currentScreen = remember { mutableStateOf(Screen.HOME) }
    val selectedVideo = remember { mutableStateOf<Video?>(null) }
    val selectedQuality = remember { mutableStateOf(appState.loadSelectedQuality()) }
    val selectedSeries = remember { mutableStateOf<SeriesData?>(null) }
    val backPressCount = remember { mutableStateOf(0) }
    
    BackHandler(enabled = currentScreen.value == Screen.HOME) {
        backPressCount.value++
        if (backPressCount.value >= 2) {
            onExitApp()
        }
    }
    
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
                selectedQuality.value = appState.loadSelectedQuality()
                currentScreen.value = Screen.PLAYER
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
                        appState.saveSelectedQuality(quality)
                        currentScreen.value = Screen.PLAYER
                    },
                    onBack = { 
                        currentScreen.value = Screen.HOME
                        backPressCount.value = 0
                    }
                )
            }
        }
        
        Screen.SERIES -> {
            selectedSeries.value?.let { series ->
                SeriesDetailScreen(
                    series = series,
                    onPlay = {
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
