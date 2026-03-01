package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    appState: AppState,
    onBack: () -> Unit,
    onQuality: () -> Unit
) {
    val showAddUrlDialog = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Text(
            "Settings",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        SettingItem(
            title = "Add Video URL",
            description = "Add a new video to play",
            onClick = { showAddUrlDialog.value = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingItem(
            title = "Quality Settings",
            description = "Select playback quality",
            onClick = onQuality
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingItem(
            title = "Back",
            description = "Return to home",
            onClick = onBack
        )
    }
    
    if (showAddUrlDialog.value) {
        AddUrlDialog(
            onDismiss = { showAddUrlDialog.value = false },
            onAddUrl = { url, title ->
                val video = Video(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    url = url
                )
                appState.addVideo(video)
            }
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            title,
            fontSize = 18.sp,
            color = Color(0xFF64B5F6)
        )
        Text(
            description,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
