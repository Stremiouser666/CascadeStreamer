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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.cascadestreamer.app.ui.TVBackButton
import com.cascadestreamer.app.ui.TVShadowStyle
import com.cascadestreamer.app.ui.SectionTitle
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

    // 1. CAPTURE HARD BACK BUTTON: Returns to Series Detail
    BackHandler(enabled = true) { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Clean Header
            TVBackButton(onBack = onBack, label = "Back to Series")

            Spacer(modifier = Modifier.height(60.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    // Left Column: Actions
                    Column(horizontalAlignment = Alignment.Start) {
                        TVEpisodePlayButton(
                            watchedPercentage = watchedPercentage,
                            onPlay = onPlay
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TVSmallIconButton(
                                icon = if (isWatched) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                onClick = { onWatchedToggle(!isWatched) },
                                focusColor = Color(0xFF4CAF50)
                            )
                            TVSmallIconButton(
                                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                onClick = { onFavoritesToggle(!isFavorite) },
                                focusColor = Color.Red
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TVSmallIconButton(icon = Icons.Filled.SkipNext, onClick = onNextEpisode)
                            TVSmallIconButton(icon = Icons.Filled.Refresh, onClick = onRestart)
                            TVSmallIconButton(
                                icon = Icons.Filled.Delete, 
                                onClick = onRemoveFromWatchlist,
                                focusColor = Color(0xFFFF6B6B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Right Column: Info
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

                        val summaryText = episode.summary?.replace("<[^>]*>".toRegex(), "") ?: "No summary."
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
            SectionTitle("MORE EPISODES")
            
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                allEpisodesInSeason.forEach { ep ->
                    TVEpisodeCardSmall(
                        episode = ep, 
                        isSelected = ep.id == episode.id,
                        onClick = { onEpisodeSelected(ep) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }

        // 2. FULL POPUP: With + and - Text Controls
        if (showFullDescription.value) {
            EpisodeDescriptionDialog(
                title = "E${episode.number}: ${episode.name}",
                summary = episode.summary ?: "",
                onDismiss = { showFullDescription.value = false }
            )
        }
    }
}

// --- HELPER COMPONENTS (Missing in previous version) ---

@Composable
fun TVSmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    focusColor: Color = Color.White
) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()

    Button(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) focusColor else Color.Black.copy(alpha = 0.4f),
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        border = if (!isFocused) BorderStroke(2.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun TVEpisodePlayButton(watchedPercentage: Int, onPlay: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()

    Button(
        onClick = onPlay,
        interactionSource = source,
        modifier = Modifier.width(160.dp).height(52.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) Color(0xFF00A36C) else Color.Black.copy(alpha = 0.4f),
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        border = if (!isFocused) BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayArrow, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (watchedPercentage > 0) "$watchedPercentage%" else "Play", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TVEpisodeCardSmall(episode: TVMazeEpisode, isSelected: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Column(modifier = Modifier.width(220.dp).clickable(source, null) { onClick() }) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f/9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(
                width = if (isFocused) 4.dp else if (isSelected) 2.dp else 0.dp,
                color = if (isFocused) Color.White else if (isSelected) Color(0xFF00A36C) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
        ) {
            AsyncImage(model = episode.image?.medium, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("E${episode.number}: ${episode.name ?: "Untitled"}", style = TVShadowStyle.copy(fontSize = 14.sp), maxLines = 1)
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
                    TVDialogButton("↑") { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value - 500) } }
                    Spacer(modifier = Modifier.width(8.dp))
                    TVDialogButton("↓") { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value + 500) } }
                    Spacer(modifier = Modifier.width(20.dp))
                    TVDialogButton("—") { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }
                    Spacer(modifier = Modifier.width(8.dp))
                    TVDialogButton("+") { if (fontSize.value < 40) fontSize = (fontSize.value + 2).sp }
                    Spacer(modifier = Modifier.width(20.dp))
                    TVDialogButton("✕") { onDismiss() }
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

@Composable
fun TVDialogButton(text: String, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Button(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.size(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) Color.White else Color.Black.copy(alpha = 0.4f),
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}
