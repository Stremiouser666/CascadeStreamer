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
fun QualitySelectionScreen(
    availableQualities: List<String>,
    selectedQuality: String,
    onQualitySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Text(
            "Select Quality",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (availableQualities.isEmpty()) {
            Text(
                "No qualities available",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            availableQualities.forEach { quality ->
                QualityItem(
                    quality = quality,
                    isSelected = quality == selectedQuality,
                    onClick = {
                        onQualitySelected(quality)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Back",
            fontSize = 16.sp,
            color = Color(0xFF64B5F6),
            modifier = Modifier
                .background(Color.DarkGray)
                .clickable { onBack() }
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun QualityItem(
    quality: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color(0xFF64B5F6) else Color.DarkGray
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            quality,
            fontSize = 18.sp,
            color = if (isSelected) Color.Black else Color.White
        )
        Text(
            if (isSelected) "Selected" else "Available",
            fontSize = 12.sp,
            color = if (isSelected) Color.DarkGray else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
