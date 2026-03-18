package com.cascadestreamer.app.ui.templates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.ui.*
import kotlinx.coroutines.launch

@Composable
fun EpisodeDetailsTemplate(
    episode: TVMazeEpisode,
    allEpisodesInSeason: List<TVMazeEpisode> = emptyList(),
    onPlay: () -> Unit = {},
    onBack: () -> Unit = {},
    onEpisodeSelected: (TVMazeEpisode) -> Unit = {},
    // Re-integrated parameters from your original code
    onWatchedToggle: (Boolean) -> Unit = {},
    onFavoritesToggle: (Boolean) -> Unit = {},
    onRestart: () -> Unit = {},
    onRemoveFromWatchlist: () -> Unit = {},
    isWatched: Boolean = false,
    isFavorite: Boolean = false,
    watchedPercentage: Int = 0 
) {
    val showFullDescription = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    BackHandler(enabled = true) { onBack() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            
            TVBackButton(onBack = onBack, label = "Back to Series")

            // Matching the 415dp Gap for UI continuity
            Spacer(modifier = Modifier.fillMaxWidth().height(415.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    
                    // LEFT COLUMN: GRID OF ACTIONS
                    Column(modifier = Modifier.width(200.dp), horizontalAlignment = Alignment.Start) {
                        
                        // 1. PLAY BUTTON WITH PROGRESS
                        TVFocusButton(
                            text = if (watchedPercentage > 0) "▶ Resume ($watchedPercentage%)" else "▶ Play",
                            onClick = onPlay,
                            width = 200.dp,
                            focusColor = Color(0xFF00A36C)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 2. ICON ACTIONS (ROW 1: Watched & Favorite)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TVFocusButton(
                                text = if (isWatched) "✓" else "○", 
                                onClick = { onWatchedToggle(!isWatched) }, 
                                isIcon = true,
                                focusColor = Color(0xFF4CAF50)
                            )
                            TVFocusButton(
                                text = if (isFavorite) "❤" else "♡", 
                                onClick = { onFavoritesToggle(!isFavorite) }, 
                                isIcon = true, 
                                focusColor = Color.Red
                            )
                            // Next Episode Button
                            TVFocusButton(text = "⏭", onClick = {
                                val nextIndex = allEpisodesInSeason.indexOf(episode) + 1
                                if (nextIndex < allEpisodesInSeason.size) {
                                    onEpisodeSelected(allEpisodesInSeason[nextIndex])
                                }
                            }, isIcon = true)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. ICON ACTIONS (ROW 2: Restart & Remove)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TVFocusButton(text = "↺", onClick = onRestart, isIcon = true)
                            TVFocusButton(text = "🗑", onClick = onRemoveFromWatchlist, isIcon = true, focusColor = Color(0xFFFF6B6B))
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // RIGHT COLUMN: EPISODE INFO
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "E${episode.number}: ${episode.name ?: "Untitled"}",
                            style = TVShadowStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Season ${episode.season}  •  ${episode.runtime ?: "N/A"} min",
                            style = TVShadowStyle.copy(fontSize = 15.sp, color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val summaryText = episode.summary?.replace("<[^>]*>".toRegex(), "") ?: "No summary."
                        Text(
                            text = summaryText,
                            style = TVShadowStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
                            maxLines = 4,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .clickable { showFullDescription.value = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            SectionTitle("MORE FROM THIS SEASON")
            
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                allEpisodesInSeason.forEach { ep ->
                    // Note: You may want to pass specific watched data here if available
                    TVEpisodeCard(
                        episode = ep, 
                        fallback = null, 
                        onClick = { onEpisodeSelected(ep) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showFullDescription.value) {
            EpisodeDescriptionDialog(
                title = "E${episode.number}: ${episode.name}",
                summary = episode.summary ?: "",
                onDismiss = { showFullDescription.value = false }
            )
        }
    }
}

// --- FULL POPUP DIALOG (With Scrolling & Sizing) ---
@Composable
fun EpisodeDescriptionDialog(title: String, summary: String, onDismiss: () -> Unit) {
    var fontSize by remember { mutableStateOf(18.sp) }
    val dialogScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.96f)).padding(60.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.Center)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Black))
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Controls
                    TVFocusButton("↑", { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value - 600) } }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("↓", { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value + 600) } }, isIcon = true)
                    Spacer(modifier = Modifier.width(24.dp))
                    Text("Size: ", color = Color.Gray, fontSize = 14.sp)
                    TVFocusButton("—", { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("+", { if (fontSize.value < 44) fontSize = (fontSize.value + 2).sp }, isIcon = true)
                    Spacer(modifier = Modifier.width(24.dp))
                    TVFocusButton("✕", onDismiss, isIcon = true, focusColor = Color.Red)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Box(modifier = Modifier.weight(1f).verticalScroll(dialogScrollState)) {
                    Text(
                        text = summary.replace("<[^>]*>".toRegex(), ""), 
                        style = TVShadowStyle.copy(fontSize = fontSize, lineHeight = (fontSize.value * 1.6).sp)
                    )
                }
            }
        }
    }
}
