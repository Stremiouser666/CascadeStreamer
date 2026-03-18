package com.cascadestreamer.app.ui.templates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.ui.* // Import helpers from main UI
import kotlinx.coroutines.launch

@Composable
fun EpisodeDetailsTemplate(
    episode: TVMazeEpisode,
    allEpisodesInSeason: List<TVMazeEpisode> = emptyList(),
    onPlay: () -> Unit = {},
    onWatchedToggle: (Boolean) -> Unit = {},
    onFavoritesToggle: (Boolean) -> Unit = {},
    onRestart: () -> Unit = {},
    onRemoveFromWatchlist: () -> Unit = {},
    onNextEpisode: () -> Unit = {},
    onEpisodeSelected: (TVMazeEpisode) -> Unit = {},
    onBack: () -> Unit = {},
    isWatched: Boolean = false,
    isFavorite: Boolean = false,
    watchedPercentage: Int = 0
) {
    val showFullDescription = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // CAPTURE HARD BACK BUTTON
    BackHandler(enabled = true) { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            
            TVBackButton(onBack = onBack, label = "Back to Series")

            // EPISODE IMAGE BOX (Matching Series Detail Layout)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                AsyncImage(
                    model = episode.image?.original ?: episode.image?.medium,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    // Buttons Column
                    Column(horizontalAlignment = Alignment.Start) {
                        TVFocusButton(
                            text = if (watchedPercentage > 0) "▶ $watchedPercentage%" else "▶ Play", 
                            onClick = onPlay, 
                            width = 160.dp, 
                            focusColor = Color(0xFF00A36C)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TVFocusButton(text = "⏭", onClick = onNextEpisode, isIcon = true)
                            TVFocusButton(text = "↺", onClick = onRestart, isIcon = true)
                            TVFocusButton(text = "🗑", onClick = onRemoveFromWatchlist, isIcon = true, focusColor = Color(0xFFFF6B6B))
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Text Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "E${episode.number}: ${episode.name}",
                            style = TVShadowStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Season ${episode.season}  •  ${episode.runtime ?: "N/A"} min",
                            style = TVShadowStyle.copy(fontSize = 15.sp, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val summaryText = episode.summary?.replace("<[^>]*>".toRegex(), "") ?: "No summary available."
                        Text(
                            text = summaryText,
                            style = TVShadowStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
                            maxLines = 4,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { showFullDescription.value = true }
                                .padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            SectionTitle("MORE FROM THIS SEASON")
            
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp),
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

        // FULL DESCRIPTION POPUP
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

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).padding(60.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.weight(1f))
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
