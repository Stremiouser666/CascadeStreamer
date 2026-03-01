package com.cascadestreamer.app.ui

import com.cascadestreamer.app.managers.AudioPreferences
import com.cascadestreamer.app.managers.CompressionLevel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
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
fun AudioSettingsScreen(
    audioPreferences: AudioPreferences,
    onBack: () -> Unit
) {
    val prefs = audioPreferences.preferences.value
    val drcEnabled = remember { mutableStateOf(prefs.drcEnabled) }
    val compressionLevel = remember { mutableStateOf(prefs.compressionLevel) }
    val volumeNormEnabled = remember { mutableStateOf(prefs.volumeNormalization) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Text(
            "Audio Settings",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // DRC Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Dynamic Range Compression",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    "Enhance dialogue clarity",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = drcEnabled.value,
                onCheckedChange = {
                    drcEnabled.value = it
                    audioPreferences.setDrcEnabled(it)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compression Level
        if (drcEnabled.value) {
            Text(
                "Compression Level",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            listOf(
                CompressionLevel.OFF to "Off",
                CompressionLevel.LOW to "Low",
                CompressionLevel.MEDIUM to "Medium",
                CompressionLevel.HIGH to "High"
            ).forEach { (level, label) ->
                CompressionLevelItem(
                    label = label,
                    isSelected = compressionLevel.value == level,
                    onClick = {
                        compressionLevel.value = level
                        audioPreferences.setCompressionLevel(level)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Volume Normalization Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Volume Normalization",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    "Consistent volume levels",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = volumeNormEnabled.value,
                onCheckedChange = {
                    volumeNormEnabled.value = it
                    audioPreferences.setVolumeNormalization(it)
                }
            )
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
fun CompressionLevelItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color(0xFF64B5F6) else Color.DarkGray
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 16.sp,
            color = if (isSelected) Color.Black else Color.White
        )
        if (isSelected) {
            Text(
                "✓",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
            )
        }
    }
}
