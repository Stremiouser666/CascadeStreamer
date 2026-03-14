package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.managers.TVMazeManager
import com.cascadestreamer.app.managers.TVMazeShow
import com.cascadestreamer.app.ui.templates.EpisodeDetailsTemplate
import kotlinx.coroutines.launch

data class SeriesData(
    val show: TVMazeShow,
    val episodes: List<TVMazeEpisode> = emptyList(),
    val backdropUrl: String? = null,
    val posterUrl: String? = null
)

@Composable
fun SeriesDetailScreen(
    series: SeriesData,
    onPlay: () -> Unit,
    onBack: () -> Unit
) {
    val selectedSeason = remember { mutableStateOf(1) }
    val selectedEpisode = remember { mutableStateOf<TVMazeEpisode?>(null) }
    val episodes = remember { mutableStateOf<List<TVMazeEpisode>>(emptyList()) }
    val allSeasons = remember { mutableStateOf<List<Int>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val tvMazeManager = remember { TVMazeManager() }
    val scope = rememberCoroutineScope()
    val showDescriptionPopup = remember { mutableStateOf(false) }

    // Load all seasons on composition
    LaunchedEffect(series.show.id) {
        isLoading.value = true
        scope.launch {
            val allEpisodes = tvMazeManager.getShowEpisodes(series.show.id)
            val seasons = allEpisodes.mapNotNull { it.season }.distinct().sorted()
            allSeasons.value = seasons

            // Load Season 1 by default
            if (seasons.isNotEmpty()) {
                selectedSeason.value = seasons.first()
                episodes.value = allEpisodes.filter { it.season == selectedSeason.value }
            }
            isLoading.value = false
        }
    }

    // Load episodes when season changes
    LaunchedEffect(selectedSeason.value) {
        scope.launch {
            val allEpisodes = tvMazeManager.getShowEpisodes(series.show.id)
            episodes.value = allEpisodes.filter { it.season == selectedSeason.value }
        }
    }

    // If episode selected, show EpisodeDetailsTemplate
    if (selectedEpisode.value != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Back button header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedEpisode.value = null }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back to series",
                        tint = Color.White
                    )
                }
                
                Text(
                    "Back to Series",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Episode details
            EpisodeDetailsTemplate(
                episode = selectedEpisode.value!!,
                allEpisodesInSeason = episodes.value,
                onPlay = onPlay,
                onWatchedToggle = { /* TODO: Implement watched tracking */ },
                onFavoritesToggle = { /* TODO: Implement favorites */ },
                onRestart = { /* TODO: Implement restart */ },
                onRemoveFromWatchlist = { /* TODO: Implement remove */ },
                onNextEpisode = {
                    val currentEp = selectedEpisode.value
                    val nextEp = episodes.value.firstOrNull { it.number != null && currentEp != null && it.number!! > currentEp.number!! }
                    if (nextEp != null) {
                        selectedEpisode.value = nextEp
                    }
                },
                onEpisodeSelected = { selectedEpisode.value = it },
                isWatched = false,
                isFavorite = false,
                watchedPercentage = 0
            )
        }
        return
    }

    // Default: Show series details + episode list
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.DarkGray)
        ) {
            // Use backdropUrl, fallback to show image original, then poster
            val imageUrl = series.backdropUrl ?: series.show.image?.original ?: series.show.image?.medium
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = series.show.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Title overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    series.show.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Play button + Heart button with strong background colors on focus
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Play button - Green on focus
            val playSource = remember { MutableInteractionSource() }
            val playFocused by playSource.collectIsFocusedAsState()
            
            Button(
                onClick = onPlay,
                interactionSource = playSource,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (playFocused) Color(0xFF4CAF50) else Color.DarkGray
                )
            ) {
                Text("▶ Play", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Heart button - Red on focus
            val heartSource = remember { MutableInteractionSource() }
            val heartFocused by heartSource.collectIsFocusedAsState()
            
            Button(
                onClick = {},
                interactionSource = heartSource,
                modifier = Modifier.size(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (heartFocused) Color.Red else Color.DarkGray
                )
            ) {
                Text("♡", color = Color.White, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metadata
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                "★${series.show.rating?.average ?: "N/A"} • ${series.show.premiered?.take(4) ?: "N/A"} • ${series.show.genres.take(3).joinToString(", ")}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            series.show.summary?.let {
                Text(
                    it.replace("<[^>]*>".toRegex(), ""),
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    maxLines = 3,
                    modifier = Modifier.clickable { showDescriptionPopup.value = true }
                )
                Text(
                    "Tap to expand ↕️",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Season selector - Blue on focus
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                "Select Season",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading.value) {
                Text(
                    "Loading seasons...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allSeasons.value.forEach { season ->
                        val seasonSource = remember { MutableInteractionSource() }
                        val seasonFocused by seasonSource.collectIsFocusedAsState()
                        
                        Button(
                            onClick = { selectedSeason.value = season },
                            interactionSource = seasonSource,
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    selectedSeason.value == season -> Color(0xFF4CAF50)
                                    seasonFocused -> Color(0xFF2196F3)
                                    else -> Color.DarkGray
                                }
                            )
                        ) {
                            Text("Season $season", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Episodes horizontal scroll (just pictures with fallback)
        if (episodes.value.isEmpty()) {
            Text(
                "No episodes found",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                episodes.value.forEach { episode ->
                    EpisodeImageCard(
                        episode = episode,
                        showPosterFallback = series.posterUrl ?: series.show.image?.medium,
                        onClick = { selectedEpisode.value = episode }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cast & Crew (placeholder)
        Text(
            "Cast & Crew",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Cast info coming soon",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Description Popup
    if (showDescriptionPopup.value) {
        DescriptionPopup(
            description = series.show.summary ?: "",
            title = series.show.name,
            onDismiss = { showDescriptionPopup.value = false }
        )
    }
}

@Composable
fun EpisodeImageCard(
    episode: TVMazeEpisode,
    showPosterFallback: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .background(Color.DarkGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Priority 1: Episode image
        if (episode.image?.medium != null) {
            AsyncImage(
                model = episode.image.medium,
                contentDescription = "E${episode.number}: ${episode.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } 
        // Priority 2: Show poster fallback
        else if (showPosterFallback != null) {
            AsyncImage(
                model = showPosterFallback,
                contentDescription = "Show poster",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // Priority 3: Text fallback
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "E${episode.number}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    episode.name ?: "Unknown",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Episode number overlay (only show if we have image)
        if (episode.image?.medium != null) {
            Text(
                "E${episode.number}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.7f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )
        }
    }
}
