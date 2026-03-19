package com.cascadestreamer.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState // THE MISSING IMPORT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.managers.TVMazeManager
import com.cascadestreamer.app.managers.TVMazeShow
import com.cascadestreamer.app.ui.templates.EpisodeDetailsTemplate
import kotlinx.coroutines.launch

// ... (Rest of your data models and SeriesDetailScreen stay the same)

@Composable
fun SeriesDescriptionDialog(title: String, summary: String, onDismiss: () -> Unit) {
    var fontSize by remember { mutableStateOf(18.sp) }
    val dialogScrollState = rememberScrollState()
    
    // Hold-to-Scroll Interactions
    val upInteraction = remember { MutableInteractionSource() }
    val downInteraction = remember { MutableInteractionSource() }
    val isUpPressed by upInteraction.collectIsPressedAsState()
    val isDownPressed by downInteraction.collectIsPressedAsState()

    // Scroll Engines
    LaunchedEffect(isUpPressed) {
        while (isUpPressed) {
            dialogScrollState.animateScrollBy(80f, tween(150, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(isDownPressed) {
        while (isDownPressed) {
            dialogScrollState.animateScrollBy(80f, tween(150, easing = FastOutSlowInEasing))
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.96f)).padding(60.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // Buttons using the new Holdable helper
                    TVFocusButton_Holdable("↑", upInteraction, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton_Holdable("↓", downInteraction, isIcon = true)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text("Size: ", color = Color.Gray, fontSize = 14.sp)
                    TVFocusButton("—", { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("+", { if (fontSize.value < 40) fontSize = (fontSize.value + 2).sp }, isIcon = true)
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    TVFocusButton("✕", onDismiss, isIcon = true, focusColor = Color.Red)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(dialogScrollState)) {
                        Text(
                            text = summary.replace("<[^>]*>".toRegex(), ""), 
                            style = TVShadowStyle.copy(fontSize = fontSize, lineHeight = (fontSize.value * 1.5).sp)
                        )
                    }
                    val fadeColor = Color.Black.copy(alpha = 0.6f)
                    Box(modifier = Modifier.fillMaxWidth().height(25.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(fadeColor, Color.Transparent))))
                    Box(modifier = Modifier.fillMaxWidth().height(25.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, fadeColor))))
                }
            }
        }
    }
}

@Composable
fun TVFocusButton_Holdable(text: String, interactionSource: MutableInteractionSource, isIcon: Boolean) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    Button(
        onClick = {}, // Handled by the LaunchedEffect loop
        interactionSource = interactionSource,
        modifier = Modifier.height(52.dp).width(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) Color.White else Color.White.copy(alpha = 0.1f),
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = 18.sp)
    }
}
