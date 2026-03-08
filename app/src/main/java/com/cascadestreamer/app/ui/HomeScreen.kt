package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cascadestreamer.app.states.AppState
import com.cascadestreamer.app.data.Video
import com.cascadestreamer.app.data.Playlist

@Composable
fun HomeScreen(
    appState: AppState,
    onVideoSelected: (Video) -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onOpenFileClick: () -> Unit = {},
    onSearchSeriesClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.DarkGray)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⚙️",
                fontSize = 24.sp,
                modifier = Modifier.clickable { onSettingsClick() }
            )
            Text(
                "ℹ️",
                fontSize = 24.sp,
                modifier = Modifier.clickable { onInfoClick() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onOpenFileClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("📁 Open File", color = Color.White, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onSearchSeriesClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("🔍 Search Series", color = Color.White, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "RECENT VIDEOS",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (appState.videos.value.isEmpty()) {
            Text(
                "No videos added yet",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            appState.videos.value.forEach { video ->
                VideoItem(
                    video = video,
                    onClick = { onVideoSelected(video) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "PLAYLISTS",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "Placeholder for playlist grid",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun VideoItem(
    video: Video,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            video.title,
            fontSize = 16.sp,
            color = Color.White
        )
        Text(
            video.url,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
