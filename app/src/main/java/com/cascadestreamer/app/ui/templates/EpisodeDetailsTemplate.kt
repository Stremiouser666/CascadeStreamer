package com.cascadestreamer.app.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.ui.DescriptionPopup
import com.cascadestreamer.app.ui.components.ImageFullscreenViewer

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
    isWatched: Boolean = false,
    isFavorite: Boolean = false,
    watchedPercentage: Int = 0
) {
    val showDescriptionPopup = remember { mutableStateOf(false) }
    val showImageFullscreen = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        // Full width episode image
        episode.image?.medium?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.DarkGray)
                    .clickable { showImageFullscreen.value = true }
            ) {
                AsyncImage(
                    model = it,
                    contentDescription = episode.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    "Tap for fullscreen",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Controls + Description row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Controls column (1/3)
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play + Watched button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OvalProgressPlayButton(
                        watchedPercentage = watchedPercentage,
                        onPlay = onPlay,
                        modifier = Modifier.weight(1f)
                    )
                    
                    val watchedSource = remember { MutableInteractionSource() }
                    val watchedFocused by watchedSource.collectIsFocusedAsState()
                    
                    IconButton(
                        onClick = { onWatchedToggle(!isWatched) },
                        interactionSource = watchedSource,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isWatched) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (isWatched) "Mark unwatched" else "Mark watched",
                            tint = if (watchedFocused) Color(0xFF4CAF50) else if (isWatched) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Favorites + Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val favSource = remember { MutableInteractionSource() }
                    val favFocused by favSource.collectIsFocusedAsState()
                    
                    IconButton(
                        onClick = { onFavoritesToggle(!isFavorite) },
                        interactionSource = favSource,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                            tint = if (favFocused) Color.Red else if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                    
                    val nextSource = remember { MutableInteractionSource() }
                    val nextFocused by nextSource.collectIsFocusedAsState()
                    
                    IconButton(
                        onClick = onNextEpisode,
                        interactionSource = nextSource,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next episode",
                            tint = if (nextFocused) Color(0xFF2196F3) else Color.Gray
                        )
                    }
                }
                
                // Restart + Remove
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val restartSource = remember { MutableInteractionSource() }
                    val restartFocused by restartSource.collectIsFocusedAsState()
                    
                    IconButton(
                        onClick = onRestart,
                        interactionSource = restartSource,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Restart episode",
                            tint = if (restartFocused) Color(0xFF2196F3) else Color.Gray
                        )
                    }
                    
                    val removeSource = remember { MutableInteractionSource() }
                    val removeFocused by removeSource.collectIsFocusedAsState()
                    
                    IconButton(
                        onClick = onRemoveFromWatchlist,
                        interactionSource = removeSource,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove from watchlist",
                            tint = if (removeFocused) Color(0xFFFF6B6B) else Color.Gray
                        )
                    }
                }
            }
            
            // Description (2/3)
            Column(
                modifier = Modifier
                    .weight(2f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "E${episode.number}: ${episode.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    "Season ${episode.season} • ${episode.runtime ?: 0} min",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                episode.summary?.let {
                    Text(
                        it.replace("<[^>]*>".toRegex(), ""),
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        maxLines = 5,
                        modifier = Modifier.clickable { showDescriptionPopup.value = true }
                    )
                    Text(
                        "Tap to expand ↕️",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Episode list
        if (allEpisodesInSeason.isNotEmpty()) {
            Text(
                "Episodes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allEpisodesInSeason.forEach { ep ->
                    EpisodeListItem(episode = ep)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    if (showDescriptionPopup.value) {
        DescriptionPopup(
            description = episode.summary ?: "",
            title = "E${episode.number}: ${episode.name}",
            onDismiss = { showDescriptionPopup.value = false }
        )
    }
    
    if (showImageFullscreen.value) {
        ImageFullscreenViewer(
            imageUrl = episode.image?.medium,
            onDismiss = { showImageFullscreen.value = false }
        )
    }
}

@Composable
fun OvalProgressPlayButton(
    watchedPercentage: Int = 0,
    onPlay: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    
    Box(
        modifier = modifier
            .height(48.dp)
            .background(Color.DarkGray, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .clickable(interactionSource = source, indication = null) { onPlay() }
            .drawBehind {
                val progressWidth = (this.size.width * watchedPercentage / 100f).coerceIn(0f, this.size.width)
                drawRect(
                    color = Color(0xFF4CAF50).copy(alpha = if (isFocused) 0.8f else 0.5f),
                    size = androidx.compose.ui.geometry.Size(progressWidth, this.size.height)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        if (watchedPercentage > 0) {
            Text(
                "$watchedPercentage%",
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun EpisodeListItem(episode: TVMazeEpisode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(8.dp)
    ) {
        Text(
            "E${episode.number}: ${episode.name}",
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "${episode.runtime ?: 0} min",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
