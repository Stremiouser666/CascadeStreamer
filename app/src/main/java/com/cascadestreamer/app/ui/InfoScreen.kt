package com.cascadestreamer.app.ui

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
fun InfoScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "CascadeStreamer",
            fontSize = 48.sp,
            color = Color(0xFF64B5F6),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            "Version 1.0.0",
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            "Android TV Video Player",
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            "Stream videos with yt-dlp integration",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        Text(
            "Back",
            fontSize = 16.sp,
            color = Color(0xFF64B5F6),
            modifier = Modifier
                .background(Color.DarkGray)
                .clickable { onBack() }
                .padding(16.dp)
        )
    }
}
