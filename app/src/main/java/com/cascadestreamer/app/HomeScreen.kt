package com.cascadestreamer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    appState: AppState,
    onVideoSelected: (Video) -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "RECENT VIDEOS",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
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
