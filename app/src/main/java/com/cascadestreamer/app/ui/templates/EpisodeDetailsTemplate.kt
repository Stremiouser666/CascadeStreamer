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
import coil.compose.AsyncImage
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.ui.TVBackButton
import com.cascadestreamer.app.ui.TVShadowStyle
import com.cascadestreamer.app.ui.TVFocusButton // Using the one defined in SeriesDetail

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
    val showDescriptionPopup = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Match hardware back behavior
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
    ) {
        // 1. Back Button at the top
        TVBackButton(onBack = onBack, label = "Back to Series")

        // 2. Main Episode Image (Hero Section)
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
                contentDescription = episode.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Info & Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left Side: Stacked Controls
            Column(
                modifier = Modifier.width(160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play with Progress
                OvalProgressPlayButton(
                    watchedPercentage = watchedPercentage,
                    onPlay = onPlay
                )

                // Secondary Actions Grid
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
                    TVSmallIconButton(
                        icon = Icons.Filled.SkipNext,
                        onClick = onNextEpisode
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TVSmallIconButton(
                        icon = Icons.Filled.Refresh,
                        onClick = onRestart
                    )
                    TVSmallIconButton(
                        icon = Icons.Filled.Delete,
                        onClick = onRemoveFromWatchlist,
                        focusColor = Color(0xFFFF6B6B)
                    )
                }
            }

            // Right Side: Title & Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "E${episode.number}: ${episode.name}",
                    style = TVShadowStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold)
                )
                
                Text(
                    text = "Season ${episode.season}  •  ${episode.runtime ?: "N/A"} min",
                    style = TVShadowStyle.copy(fontSize = 14.sp, color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val summaryText = episode.summary?.replace("<[^>]*>".toRegex(), "") ?: "No summary available."
                Text(
                    text = summaryText,
                    style = TVShadowStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    maxLines = 5,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { showDescriptionPopup.value = true }
                        .padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 4. Horizontal Episode List (Matches "EPISODES" row in Series Detail)
        if (allEpisodesInSeason.isNotEmpty()) {
            Text(
                text = "MORE FROM SEASON ${episode.season}",
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 10.dp),
                style = TVShadowStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Gray)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                allEpisodesInSeason.forEach { ep ->
                    EpisodeImageCard(
                        episode = ep,
                        isSelected = ep.id == episode.id,
                        onClick = { onEpisodeSelected(ep) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

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
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun OvalProgressPlayButton(
    watchedPercentage: Int,
    onPlay: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()

    Button(
        onClick = onPlay,
        interactionSource = source,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) Color(0xFF00A36C) else Color.Black.copy(alpha = 0.4f),
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        border = if (!isFocused) BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayArrow, null)
            if (watchedPercentage > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("$watchedPercentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EpisodeImageCard(
    episode: TVMazeEpisode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()

    Column(modifier = Modifier.width(200.dp).clickable(source, null) { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .then(
                    if (isFocused || isSelected) 
                        Modifier.border(4.dp, if (isSelected) Color(0xFF00A36C) else Color.White, RoundedCornerShape(12.dp)).padding(4.dp) 
                    else Modifier
                )
        ) {
            AsyncImage(
                model = episode.image?.medium,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "E${episode.number}: ${episode.name ?: "Untitled"}",
            style = TVShadowStyle.copy(
                fontSize = 14.sp, 
                fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}
