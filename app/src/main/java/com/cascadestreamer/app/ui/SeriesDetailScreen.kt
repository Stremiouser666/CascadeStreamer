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

// DATA MODELS
data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val imageUrl: String?,
    val biography: String? = null
)

data class SeriesData(
    val show: TVMazeShow,
    val episodes: List<TVMazeEpisode> = emptyList(),
    val cast: List<CastMember> = emptyList(),
    val backdropUrl: String? = null,
    val posterUrl: String? = null
)

val TVShadowStyle = TextStyle(
    color = Color.White,
    shadow = Shadow(
        color = Color.Black.copy(alpha = 0.95f),
        offset = Offset(3f, 5f),
        blurRadius = 14f
    )
)

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

    // THE ROOT BOX
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // DYNAMIC BACKDROP: Swaps between Series and Episode image
        AsyncImage(
            model = if (selectedEpisode.value != null) {
                // If an episode is selected, show its image
                selectedEpisode.value?.image?.original ?: selectedEpisode.value?.image?.medium
            } else {
                // Otherwise show the Series backdrop
                series.backdropUrl ?: series.show.image?.original
            },
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // LAYER 1: Main Series Scrollable Content
        // We only show this if NO episode and NO actor is selected
        if (selectedEpisode.value == null && selectedCast.value == null) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                Spacer(modifier = Modifier.size(1.dp).focusable())
                
                // BACK BUTTON FOR SERIES
                TVBackButton(onBack = onBack, label = "Back to Home")

                Spacer(modifier = Modifier.fillMaxWidth().height(360.dp)) // Adjusted for Back Button

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

                Spacer(modifier = Modifier.height(32.dp))
                SectionTitle("SEASONS")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    allSeasons.value.forEach { season ->
                        TVSeasonSelectButton(season = season, isSelected = selectedSeason.intValue == season, onClick = { selectedSeason.intValue = season })
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                SectionTitle("EPISODES")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    episodesInSeason.value.forEach { episode ->
                        TVEpisodeCard(episode = episode, fallback = series.posterUrl, onClick = { selectedEpisode.value = episode })
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                SectionTitle("CAST & CREW")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    series.cast.forEach { member ->
                        TVCastCircleCard(member = member, onClick = { selectedCast.value = member })
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // LAYER 2: EPISODE DETAILS OVERLAY
        if (selectedEpisode.value != null) {
            EpisodeDetailsTemplate(
                episode = selectedEpisode.value!!,
                allEpisodesInSeason = episodesInSeason.value,
                onPlay = onPlay,
                onBack = { selectedEpisode.value = null },
                onEpisodeSelected = { selectedEpisode.value = it }
            )
        }

        // LAYER 3: ACTOR PROFILE
        if (selectedCast.value != null) {
            ActorWikiProfile(member = selectedCast.value!!, onBack = { selectedCast.value = null })
        }

        // LAYER 4: DESCRIPTION DIALOG
        if (showFullDescription.value) {
            SeriesDescriptionDialog(
                title = "Summary",
                summary = series.show.summary ?: "",
                onDismiss = { showFullDescription.value = false }
            )
        }
    }
}

// ... (Rest of helpers remain exactly the same)
