package com.cascadestreamer.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.cascadestreamer.app.data.Video
import com.cascadestreamer.app.states.AppState
import com.cascadestreamer.app.ui.HomeScreen
import com.cascadestreamer.app.ui.SettingsScreen
import com.cascadestreamer.app.ui.InfoScreen
import com.cascadestreamer.app.ui.VideoPlayerScreen
import com.cascadestreamer.app.ui.QualitySelectionScreen
import com.cascadestreamer.app.ui.SeriesDetailScreen
import com.cascadestreamer.app.ui.SeriesData
import com.cascadestreamer.app.ui.FileBrowserScreen

enum class Screen {
    HOME, SETTINGS, INFO, PLAYER, QUALITY, SERIES, FILE_BROWSER
}

@Composable
fun CascadeStreamerApp(
    appState: AppState = AppState(),
    onExitApp: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentScreen = remember { mutableStateOf(Screen.HOME) }
    val selectedVideo = remember { mutableStateOf<Video?>(null) }
    val selectedQuality = remember { mutableStateOf(appState.loadSelectedQuality()) }
    val selectedSeries = remember { mutableStateOf<SeriesData?>(null) }
    val backPressCount = remember { mutableStateOf(0) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentScreen.value = Screen.FILE_BROWSER
        }
    }
    
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
            },
            onOpenFileClick = {
                if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                ) {
                    currentScreen.value = Screen.FILE_BROWSER
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
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
        
        Screen.FILE_BROWSER -> {
            FileBrowserScreen(
                onFileSelected = { filePath ->
                    val videoUrl = if (filePath.startsWith("content://") || filePath.startsWith("http")) {
                        filePath
                    } else {
                        "file://$filePath"
                    }
                    selectedVideo.value = Video(
                        id = filePath,
                        title = filePath.substringAfterLast("/"),
                        url = videoUrl
                    )
                    currentScreen.value = Screen.PLAYER
                },
                onBack = {
                    currentScreen.value = Screen.HOME
                    backPressCount.value = 0
                }
            )
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
