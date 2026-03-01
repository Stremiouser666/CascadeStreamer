package com.cascadestreamer.app.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AddUrlDialog(
    onDismiss: () -> Unit,
    onAddUrl: (String, String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .background(Color.DarkGray)
                .padding(24.dp)
                .width(400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Add Video URL",
                fontSize = 20.sp,
                color = Color.White
            )
            
            OutlinedTextField(
                value = urlInput,
                onValueChange = { 
                    urlInput = it
                    errorMessage = ""
                },
                label = { Text("Video URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = titleInput,
                onValueChange = { 
                    titleInput = it
                    errorMessage = ""
                },
                label = { Text("Title (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    fontSize = 12.sp,
                    color = Color.Red
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        when {
                            urlInput.isBlank() -> {
                                errorMessage = "URL cannot be empty"
                            }
                            !isValidUrl(urlInput) -> {
                                errorMessage = "Invalid URL format"
                            }
                            else -> {
                                val title = titleInput.ifBlank { urlInput }
                                onAddUrl(urlInput, title)
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }
}

private fun isValidUrl(url: String): Boolean {
    return url.startsWith("http://") || url.startsWith("https://")
}
