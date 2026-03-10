package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@Composable
fun DescriptionPopup(
    description: String,
    title: String,
    onDismiss: () -> Unit
) {
    val fontSize = remember { mutableStateOf(14f) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val cleanDescription = description.replace("<[^>]*>".toRegex(), "")
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.9f)
                .background(Color.Black)
                .border(2.dp, Color.DarkGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Description",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Scroll up button
            if (scrollState.value > 0) {
                Button(
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollTo(maxOf(0, scrollState.value - 100))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(
                        Icons.Filled.ArrowDropUp,
                        contentDescription = "Scroll up",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scroll up", fontSize = 10.sp, color = Color.White)
                }
            }
            
            // Description text (scrollable)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    cleanDescription,
                    fontSize = fontSize.value.sp,
                    color = Color.LightGray,
                    lineHeight = (fontSize.value + 4).sp
                )
            }
            
            // Scroll down button
            if (scrollState.value < scrollState.maxValue) {
                Button(
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollTo(minOf(scrollState.maxValue, scrollState.value + 100))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Scroll down",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scroll down", fontSize = 10.sp, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Font size controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (fontSize.value > 10f) fontSize.value -= 2f },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Smaller",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    "Size: ${fontSize.value.toInt()}",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { if (fontSize.value < 24f) fontSize.value += 2f },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Larger",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Close button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Close", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}
