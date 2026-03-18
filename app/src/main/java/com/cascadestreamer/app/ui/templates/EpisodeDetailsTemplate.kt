package com.cascadestreamer.app.ui.templates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.ui.* // Imports TVFocusButton, TVShadowStyle, etc.
import kotlinx.coroutines.launch

@Composable
fun EpisodeDetailsTemplate(
    episode: TVMazeEpisode,
    allEpisodesInSeason: List<TVMazeEpisode> = emptyList(),
    onPlay: () -> Unit = {},
    onBack: () -> Unit = {},
    onEpisodeSelected: (TVMazeEpisode) -> Unit = {}
) {
    val showFullDescription = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // 1. HARD BACK BUTTON: Returns to Series Detail Screen
    BackHandler(enabled = true) { onBack() }

    // No background color here so the Dynamic Backdrop from SeriesDetailScreen shows through
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header with Back Button
            TVBackButton(onBack = onBack, label = "Back to Series")

            // THE 415DP GAP: Ensures the episode title lines up exactly with the series title
            Spacer(modifier = Modifier.fillMaxWidth().height(415.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    // Left Column: Main Actions
                    Column(horizontalAlignment = Alignment.Start) {
                        TVFocusButton(
                            text = "▶ Play", 
                            onClick = onPlay, 
                            width = 160.dp, 
                            focusColor = Color(0xFF00A36C)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TVFocusButton(text = "♡", onClick = {}, isIcon = true, focusColor = Color.Red)
                            TVFocusButton(text = "↺", onClick = {}, isIcon = true)
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Right Column: Episode Info & Summary
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "E${episode.number}: ${episode.name ?: "Untitled"}",
                            style = TVShadowStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Season ${episode.season}  •  ${episode.runtime ?: "N/A"} min",
                            style = TVShadowStyle.copy(fontSize = 15.sp, color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val summaryText = episode.summary?.replace("<[^>]*>".toRegex(), "") ?: "No summary."
                        Text(
                            text = summaryText,
                            style = TVShadowStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
                            maxLines = 3,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { showFullDescription.value = true }
                                .padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("MORE FROM THIS SEASON")

            // Carousel of other episodes in the season
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                allEpisodesInSeason.forEach { ep ->
                    TVEpisodeCard(
                        episode = ep, 
                        fallback = null, 
                        onClick = { onEpisodeSelected(ep) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // 2. THE POPUP: Exactly matches the Series Screen popup with Size controls
        if (showFullDescription.value) {
            EpisodeDescriptionDialog(
                title = "E${episode.number}: ${episode.name}",
                summary = episode.summary ?: "",
                onDismiss = { showFullDescription.value = false }
            )
        }
    }
}

@Composable
fun EpisodeDescriptionDialog(title: String, summary: String, onDismiss: () -> Unit) {
    var fontSize by remember { mutableStateOf(18.sp) }
    val dialogScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss, 
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).padding(60.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Nav and Text Controls
                    TVFocusButton("↑", { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value - 500) } }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("↓", { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value + 500) } }, isIcon = true)
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Text("Size: ", color = Color.Gray, fontSize = 14.sp)
                    TVFocusButton("—", { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("+", { if (fontSize.value < 40) fontSize = (fontSize.value + 2).sp }, isIcon = true)
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    TVFocusButton("✕", { onDismiss() }, isIcon = true)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(modifier = Modifier.verticalScroll(dialogScrollState)) {
                    Text(
                        text = summary.replace("<[^>]*>".toRegex(), ""), 
                        style = TVShadowStyle.copy(fontSize = fontSize, lineHeight = (fontSize.value * 1.5).sp)
                    )
                }
            }
        }
    }
}
