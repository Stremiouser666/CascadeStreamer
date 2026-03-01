package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun PlaylistImportScreen(
    playlistExtractor: PlaylistExtractor,
    onBack: () -> Unit,
    onPlaylistSaved: (String) -> Unit
) {
    val playlistUrl = remember { mutableStateOf("") }
    val ytDlpCommand = remember { 
        mutableStateOf("yt-dlp -j --flat-playlist")
    }
    val extractedUrls = remember { mutableStateOf("") }
    val playlistName = remember { mutableStateOf("") }
    val extractionStatus = remember { mutableStateOf("") }
    val isExtracting = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Import Playlist",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // URL Input
        OutlinedTextField(
            value = playlistUrl.value,
            onValueChange = { playlistUrl.value = it },
            label = { Text("Playlist/Video URL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = false,
            maxLines = 3
        )
        
        Text(
            "yt-dlp Command",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // yt-dlp Command Editor
        OutlinedTextField(
            value = ytDlpCommand.value,
            onValueChange = { ytDlpCommand.value = it },
            label = { Text("Edit yt-dlp command") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 16.dp),
            singleLine = false,
            maxLines = 4
        )
        
        Text(
            "Tip: Default works for most sites. Edit for specific formats.",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Extract Button
        Button(
            onClick = {
                isExtracting.value = true
                extractionStatus.value = "Extracting..."
                
                try {
                    val urls = playlistExtractor.extractPlaylistUrls(playlistUrl.value)
                    extractedUrls.value = urls.joinToString("\n")
                    extractionStatus.value = "Found ${urls.size} URLs"
                } catch (e: Exception) {
                    extractionStatus.value = "Error: ${e.message}"
                }
                
                isExtracting.value = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(if (isExtracting.value) "Extracting..." else "Extract URLs")
        }
        
        if (extractionStatus.value.isNotEmpty()) {
            Text(
                extractionStatus.value,
                fontSize = 12.sp,
                color = if (extractionStatus.value.contains("Error")) Color.Red else Color(0xFF64B5F6),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        if (extractedUrls.value.isNotEmpty()) {
            Text(
                "Extracted URLs (Edit as needed)",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Editable URLs
            OutlinedTextField(
                value = extractedUrls.value,
                onValueChange = { extractedUrls.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp),
                singleLine = false,
                maxLines = 15
            )
            
            // Playlist Name
            OutlinedTextField(
                value = playlistName.value,
                onValueChange = { playlistName.value = it },
                label = { Text("Playlist Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            
            // Save Button
            Button(
                onClick = {
                    if (playlistName.value.isNotEmpty() && extractedUrls.value.isNotEmpty()) {
                        val urls = extractedUrls.value.split("\n").filter { it.isNotEmpty() }
                        val documentsDir = File("/sdcard/Documents")
                        if (!documentsDir.exists()) {
                            documentsDir.mkdirs()
                        }
                        
                        playlistExtractor.savePlaylistToFile(
                            playlistName.value,
                            urls,
                            documentsDir
                        )
                        
                        extractionStatus.value = "Playlist saved: ${playlistName.value}.txt"
                        onPlaylistSaved(playlistName.value)
                    } else {
                        extractionStatus.value = "Please enter playlist name and URLs"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Save Playlist")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
