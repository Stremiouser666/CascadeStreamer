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

// --- DATA MODELS ---
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // DYNAMIC BACKDROP
        AsyncImage(
            model = if (selectedEpisode.value != null) {
                selectedEpisode.value?.image?.original ?: selectedEpisode.value?.image?.medium
            } else {
                series.backdropUrl ?: series.show.image?.original
            },
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // MAIN CONTENT
        if (selectedEpisode.value == null && selectedCast.value == null) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                TVBackButton(onBack = onBack, label = "Back to Home")
                Spacer(modifier = Modifier.fillMaxWidth().height(360.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column {
                            TVFocusButton(text = "▶ Play", onClick = onPlay, width = 160.dp, focusColor = Color(0xFF00A36C))
                            Spacer(modifier = Modifier.height(12.dp))
                            TVFocusButton(text = "♡", onClick = {}, isIcon = true, focusColor = Color.Red)
                        }
                        Spacer(modifier = Modifier.width(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("★ ${series.show.rating?.average ?: "N/A"}  •  ${series.show.premiered?.take(4)}", style = TVShadowStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = series.show.summary?.replace("<[^>]*>".toRegex(), "") ?: "",
                                style = TVShadowStyle.copy(fontSize = 15.sp),
                                maxLines = 3,
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).padding(12.dp).clickable { showFullDescription.value = true }
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

                Spacer(modifier = Modifier.height(32.dp))
                SectionTitle("CAST")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    series.cast.forEach { member ->
                        TVCastCircleCard(member = member, onClick = { selectedCast.value = member })
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // OVERLAYS
        if (selectedEpisode.value != null) {
            EpisodeDetailsTemplate(
                episode = selectedEpisode.value!!,
                allEpisodesInSeason = episodesInSeason.value,
                onPlay = onPlay,
                onBack = { selectedEpisode.value = null },
                onEpisodeSelected = { selectedEpisode.value = it }
            )
        }

        if (selectedCast.value != null) {
            ActorWikiProfile(member = selectedCast.value!!, onBack = { selectedCast.value = null })
        }

        if (showFullDescription.value) {
            SeriesDescriptionDialog(title = "Summary", summary = series.show.summary ?: "", onDismiss = { showFullDescription.value = false })
        }
    }
}

// --- HELPER COMPONENTS (The stuff that was missing!) ---

@Composable
fun TVBackButton(onBack: () -> Unit, label: String) {
    Row(modifier = Modifier.padding(24.dp).clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = TVShadowStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun TVFocusButton(text: String, onClick: () -> Unit, width: androidx.compose.ui.unit.Dp = 60.dp, isIcon: Boolean = false, focusColor: Color = Color.White) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Button(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.height(52.dp).then(if (isIcon) Modifier.width(52.dp) else Modifier.width(width)),
        colors = ButtonDefaults.buttonColors(containerColor = if (isFocused) focusColor else Color.Black.copy(alpha = 0.4f), contentColor = if (isFocused) Color.Black else Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp), style = TVShadowStyle.copy(fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold))
}

@Composable
fun TVSeasonSelectButton(season: Int, isSelected: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Button(
        onClick = onClick,
        interactionSource = source,
        colors = ButtonDefaults.buttonColors(containerColor = if (isFocused) Color.White else if (isSelected) Color(0xFF388E3C) else Color.DarkGray),
        contentColor = if (isFocused) Color.Black else Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Season $season")
    }
}

@Composable
fun TVEpisodeCard(episode: TVMazeEpisode, fallback: String?, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Column(modifier = Modifier.width(280.dp).clickable(source, null) { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(12.dp)).border(if (isFocused) 4.dp else 0.dp, Color.White, RoundedCornerShape(12.dp))) {
            AsyncImage(model = episode.image?.medium ?: fallback, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Text("E${episode.number}: ${episode.name}", style = TVShadowStyle.copy(fontSize = 14.sp), maxLines = 1)
    }
}

@Composable
fun TVCastCircleCard(member: CastMember, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Column(modifier = Modifier.width(120.dp).clickable(source, null) { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(model = member.imageUrl, contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape).border(if (isFocused) 3.dp else 0.dp, Color.White, CircleShape), contentScale = ContentScale.Crop)
        Text(member.name, style = TVShadowStyle.copy(fontSize = 12.sp), maxLines = 1)
    }
}

@Composable
fun ActorWikiProfile(member: CastMember, onBack: () -> Unit) {
    BackHandler { onBack() }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f))) {
        Column {
            TVBackButton(onBack = onBack, label = "Actor Profile")
            Text(member.name, style = TVShadowStyle.copy(fontSize = 40.sp), modifier = Modifier.padding(32.dp))
        }
    }
}

@Composable
fun SeriesDescriptionDialog(title: String, summary: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(32.dp)) {
            Column {
                Text(title, style = TVShadowStyle.copy(fontSize = 24.sp))
                Text(summary.replace("<[^>]*>".toRegex(), ""), color = Color.White)
                TVFocusButton("Close", onDismiss)
            }
        }
    }
}
