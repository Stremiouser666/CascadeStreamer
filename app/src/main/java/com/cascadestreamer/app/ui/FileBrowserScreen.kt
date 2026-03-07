package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun FileBrowserScreen(
    onFileSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentPath = remember { mutableStateOf("/storage/emulated/0") }
    val files = remember { mutableStateOf<List<File>>(emptyList()) }
    
    // Load files whenever path changes
    LaunchedEffect(currentPath.value) {
        files.value = File(currentPath.value)
            .listFiles()
            ?.sortedBy { !it.isDirectory }
            ?.filter { it.isDirectory || it.extension in listOf("mp4", "mkv", "webm", "avi") }
            ?: emptyList()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Current path
        Text(
            "Browsing: ${currentPath.value}",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // File list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (files.value.isEmpty()) {
                Text(
                    "No files or folders found",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                files.value.forEach { file ->
                    FileItem(
                        file = file,
                        onClick = {
                            if (file.isDirectory) {
                                currentPath.value = file.absolutePath
                            } else {
                                onFileSelected(file.absolutePath)
                            }
                        }
                    )
                }
            }
        }
        
        // Back button
        Text(
            "← Back",
            fontSize = 16.sp,
            color = Color(0xFF64B5F6),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { onBack() }
                .padding(top = 16.dp)
        )
    }
}

@Composable
fun FileItem(
    file: File,
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
            if (file.isDirectory) "📁 ${file.name}" else "🎬 ${file.name}",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            file.absolutePath,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
