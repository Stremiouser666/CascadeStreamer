package com.cascadestreamer.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .background(Color(0xFF121212))
                .border(2.dp, Color.DarkGray)
                .padding(24.dp)
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

                    // Top fade for scroll - clickable
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

                    // Bottom fade for scroll - clickable
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

                // Floating zoom controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZoomButton(
                        icon = Icons.Filled.Remove,
                        onClick = { if (fontSize > 10f) fontSize -= 2f }
                    )

                    Text(
                        "${fontSize.toInt()} pt",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    ZoomButton(
                        icon = Icons.Filled.Add,
                        onClick = { if (fontSize < 24f) fontSize += 2f }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close button
                val closeSource = remember { MutableInteractionSource() }
                val closeFocused by closeSource.collectIsFocusedAsState()
                
                Button(
                    onClick = onDismiss,
                    interactionSource = closeSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (closeFocused) {
                                Modifier
                                    .border(3.dp, Color.White)
                                    .padding(2.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = Color(0xFF2196F3).copy(alpha = 0.4f),
                                            radius = this.size.maxDimension / 3.5f
                                        )
                                    }
                            } else Modifier
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (closeFocused) Color(0xFF2196F3) else Color(0xFF2196F3).copy(alpha = 0.6f)
                    )
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ZoomButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()

    FloatingActionButton(
        onClick = onClick,
        interactionSource = source,
        containerColor = if (isFocused) Color(0xFF2196F3) else Color(0xFF2196F3).copy(alpha = 0.6f),
        modifier = Modifier
            .size(56.dp)
            .then(
                if (isFocused) {
                    Modifier
                        .border(3.dp, Color.White, FloatingActionButtonDefaults.shape)
                        .padding(2.dp)
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF2196F3).copy(alpha = 0.4f),
                                radius = this.size.maxDimension / 1.2f
                            )
                        }
                } else Modifier
            )
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}
