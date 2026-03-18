package com.cascadestreamer.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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

// ... (Data classes CastMember and SeriesData remain the same)

@Composable
fun SeriesDetailScreen(
    series: SeriesData,
    onPlay: () -> Unit,
    onBack: () -> Unit
) {
    val selectedSeason = remember { mutableIntStateOf(1) }
    val selectedEpisode = remember { mutableStateOf<TVMazeEpisode?>(null) }
    val selectedCast = remember { mutableStateOf<CastMember?>(null) }
    val showFullDescription = remember { mutableStateOf(false) }

    val allEpisodes = remember { mutableStateOf<List<TVMazeEpisode>>(emptyList()) }
    val episodesInSeason = remember { derivedStateOf { 
        allEpisodes.value.filter { it.season == selectedSeason.intValue } 
    }}
    val allSeasons = remember { mutableStateOf<List<Int>>(emptyList()) }
    
    val tvMazeManager = remember { TVMazeManager() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(series.show.id) {
        scope.launch {
            val fetched = tvMazeManager.getShowEpisodes(series.show.id)
            allEpisodes.value = fetched
            val seasons = fetched.mapNotNull { it.season }.distinct().sorted()
            allSeasons.value = seasons
            if (seasons.isNotEmpty()) selectedSeason.intValue = seasons.first()
        }
    }

    // ACTOR PROFILE OVERLAY
    if (selectedCast.value != null) {
        ActorWikiProfile(member = selectedCast.value!!, onBack = { selectedCast.value = null })
        return
    }

    // EPISODE DETAILS OVERLAY
    if (selectedEpisode.value != null) {
        // Add BackHandler here too if the hard back button fails on episodes
        BackHandler { selectedEpisode.value = null }
        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            TVBackButton(onBack = { selectedEpisode.value = null }, label = "Back to Series")
            EpisodeDetailsTemplate(
                episode = selectedEpisode.value!!,
                allEpisodesInSeason = episodesInSeason.value,
                onPlay = onPlay,
                onWatchedToggle = {}, onFavoritesToggle = {}, onRestart = {},
                onRemoveFromWatchlist = {},
                onNextEpisode = {
                    val current = selectedEpisode.value
                    val next = episodesInSeason.value.firstOrNull { it.number != null && current != null && it.number!! > current.number!! }
                    if (next != null) selectedEpisode.value = next
                },
                onEpisodeSelected = { selectedEpisode.value = it },
                isWatched = false, isFavorite = false, watchedPercentage = 0
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = series.backdropUrl ?: series.show.image?.original,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Spacer(modifier = Modifier.size(1.dp).focusable())

            // THE ADJUSTED GAP (35dp lift from original 450dp)
            Spacer(modifier = Modifier.fillMaxWidth().height(415.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.Start) {
                        TVFocusButton(text = "▶ Play", onClick = onPlay, width = 160.dp, focusColor = Color(0xFF00A36C))
                        Spacer(modifier = Modifier.height(12.dp))
                        TVFocusButton(text = "♡", onClick = {}, isIcon = true, focusColor = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(28.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "★ ${series.show.rating?.average ?: "N/A"}  •  ${series.show.premiered?.take(4) ?: "N/A"}  •  ${series.show.genres.joinToString(", ")}",
                            style = TVShadowStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val summaryText = series.show.summary?.replace("<[^>]*>".toRegex(), "") ?: ""
                        Text(
                            text = summaryText,
                            style = TVShadowStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
                            maxLines = 3,
                            modifier = Modifier
                                .clickable { showFullDescription.value = true }
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }
            // ... (Seasons, Episodes, Cast rows)
        }
    }
    // ... (Summary Dialog)
}

@Composable
fun ActorWikiProfile(member: CastMember, onBack: () -> Unit) {
    // FIX: Intercepts the physical "Back" button on the remote/phone
    BackHandler(enabled = true) {
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TVBackButton(onBack = onBack, label = "Actor Profile")
            Row(modifier = Modifier.padding(60.dp), horizontalArrangement = Arrangement.spacedBy(50.dp)) {
                AsyncImage(model = member.imageUrl, contentDescription = null, modifier = Modifier.size(350.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Column(modifier = Modifier.weight(1f)) {
                    Text(member.name, style = TVShadowStyle.copy(fontSize = 58.sp, fontWeight = FontWeight.Black))
                    Text("Role: ${member.character}", style = TVShadowStyle.copy(fontSize = 24.sp, color = Color.Gray))
                    Spacer(modifier = Modifier.height(35.dp))
                    Text(text = member.biography ?: "No biography available.", style = TVShadowStyle.copy(fontSize = 20.sp, lineHeight = 32.sp))
                }
            }
        }
    }
}

// ... (Rest of helpers: TVFocusButton, TVSeasonSelectButton, etc., all using alpha 0.4f)
