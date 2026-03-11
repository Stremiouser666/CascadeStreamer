package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    var fontSize by remember { mutableStateOf(14f) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val cleanDescription = description.replace("<[^>]*>".toRegex(), "")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp)
                .background(Color(0xFF121212))
                .border(2.dp, Color.DarkGray)
                .padding(16.dp)
        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Description",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable description with fade
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            cleanDescription,
                            fontSize = fontSize.sp,
                            color = Color.LightGray,
                            lineHeight = (fontSize + 4).sp
                        )
                    }

                    // Top fade for scroll - clickable (more prominent)
                    if (scrollState.value > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        0f to Color(0xFF121212),
                                        0.7f to Color(0xFF121212).copy(alpha = 0.3f),
                                        1f to Color.Transparent
                                    )
                                )
                                .align(Alignment.TopCenter)
                                .clickable {
                                    scope.launch {
                                        scrollState.animateScrollTo(maxOf(0, scrollState.value - 100))
                                    }
                                }
                        )
                    }

                    // Bottom fade for scroll - clickable (more prominent)
                    if (scrollState.value < scrollState.maxValue) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        0.3f to Color(0xFF121212).copy(alpha = 0.3f),
                                        1f to Color(0xFF121212)
                                    )
                                )
                                .align(Alignment.BottomCenter)
                                .clickable {
                                    scope.launch {
                                        scrollState.animateScrollTo(minOf(scrollState.maxValue, scrollState.value + 100))
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Floating zoom controls with opacity feedback
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Smaller button
                    val smallerInteractionSource = remember { MutableInteractionSource() }
                    val smallerPressed by smallerInteractionSource.collectIsPressedAsState()
                    val smallerFocused by smallerInteractionSource.collectIsFocusedAsState()
                    
                    FloatingActionButton(
                        onClick = { if (fontSize > 10f) fontSize -= 2f },
                        containerColor = if (smallerPressed || smallerFocused) Color(0xFF2196F3) else Color(0xFF2196F3).copy(alpha = 0.6f),
                        interactionSource = smallerInteractionSource
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Smaller", tint = Color.White)
                    }

                    Text(
                        "${fontSize.toInt()} pt",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    // Larger button
                    val largerInteractionSource = remember { MutableInteractionSource() }
                    val largerPressed by largerInteractionSource.collectIsPressedAsState()
                    val largerFocused by largerInteractionSource.collectIsFocusedAsState()
                    
                    FloatingActionButton(
                        onClick = { if (fontSize < 24f) fontSize += 2f },
                        containerColor = if (largerPressed || largerFocused) Color(0xFF2196F3) else Color(0xFF2196F3).copy(alpha = 0.6f),
                        interactionSource = largerInteractionSource
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Larger", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close button with opacity feedback
                val closeInteractionSource = remember { MutableInteractionSource() }
                val closePressed by closeInteractionSource.collectIsPressedAsState()
                val closeFocused by closeInteractionSource.collectIsFocusedAsState()
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (closePressed || closeFocused) Color(0xFF2196F3) else Color(0xFF2196F3).copy(alpha = 0.6f)
                    ),
                    interactionSource = closeInteractionSource
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
