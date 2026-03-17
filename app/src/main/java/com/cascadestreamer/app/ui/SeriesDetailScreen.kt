package com.cascadestreamer.app.ui

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
import androidx.compose.ui.graphics.Brush
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

// ... (Data classes and TVShadowStyle remain the same)

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

    val episodes = remember { mutableStateOf<List<TVMazeEpisode>>(emptyList()) }
    val allSeasons = remember { mutableStateOf<List<Int>>(emptyList()) }
    val tvMazeManager = remember { TVMazeManager() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(series.show.id) {
        scope.launch {
            val allEpisodes = tvMazeManager.getShowEpisodes(series.show.id)
            val seasons = allEpisodes.mapNotNull { it.season }.distinct().sorted()
            allSeasons.value = seasons
            if (seasons.isNotEmpty()) {
                selectedSeason.intValue = seasons.first()
                episodes.value = allEpisodes.filter { it.season == selectedSeason.intValue }
            }
        }
    }

    if (selectedCast.value != null) {
        ActorWikiProfile(member = selectedCast.value!!, onBack = { selectedCast.value = null })
        return
    }

    if (selectedEpisode.value != null) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            TVBackButton(onBack = { selectedEpisode.value = null }, label = "Back to Series")
            EpisodeDetailsTemplate(
                episode = selectedEpisode.value!!,
                allEpisodesInSeason = episodes.value,
                onPlay = onPlay,
                onWatchedToggle = {}, onFavoritesToggle = {}, onRestart = {},
                onRemoveFromWatchlist = {},
                onNextEpisode = {
                    val current = selectedEpisode.value
                    val next = episodes.value.firstOrNull { it.number != null && current != null && it.number!! > current.number!! }
                    if (next != null) selectedEpisode.value = next
                },
                onEpisodeSelected = { selectedEpisode.value = it },
                isWatched = false, isFavorite = false, watchedPercentage = 0
            )
        }
        return
    }

    // MAIN UI ROOT
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // BACKGROUND IMAGE (Now fills entire screen)
        val imageUrl = series.backdropUrl ?: series.show.image?.original
        AsyncImage(
            model = imageUrl, 
            contentDescription = null, 
            modifier = Modifier.fillMaxSize(), 
            contentScale = ContentScale.Crop
        )

        // GRADIENT OVERLAY (Makes lists readable and fades the photo into the black bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.9f),
                            Color.Black
                        ),
                        startY = 0f,
                        endY = 1000f // Adjust this value to control where the fade finishes
                    )
                )
        )

        // CONTENT LAYER
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            
            // HERO SECTION
            Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                // We keep this Box to maintain your relative positioning for text/buttons
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(horizontalAlignment = Alignment.Start) {
                            TVFocusButton(text = "▶ Play", onClick = onPlay, width = 160.dp, focusColor = Color(0xFF00A36C))
                            Spacer(modifier = Modifier.height(12.dp))
                            TVFocusButton(text = "♡", onClick = {}, isIcon = true, focusColor = Color.Red)
                        }
                        Spacer(modifier = Modifier.width(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "★ ${series.show.rating?.average ?: "N/A"}  •  ${series.show.premiered?.take(4) ?: "N/A"}  •  ${series.show.genres.joinToString(", ")}", style = TVShadowStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            val summaryText = series.show.summary?.replace("<[^>]*>".toRegex(), "") ?: ""
                            Text(text = summaryText, style = TVShadowStyle.copy(fontSize = 15.sp, lineHeight = 22.sp, color = Color.White), maxLines = 3, modifier = Modifier.clickable { showFullDescription.value = true }.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(12.dp))
                        }
                    }
                }
            }

            // LIST SECTIONS (These now scroll over the background)
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
                episodes.value.forEach { episode ->
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

    // DIALOG (Remains at the bottom of the root function)
    if (showFullDescription.value) {
        var fontSize by remember { mutableStateOf(18.sp) }
        Dialog(onDismissRequest = { showFullDescription.value = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).padding(60.dp)) {
                Column(modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Summary", style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Size: ", color = Color.Gray, fontSize = 14.sp)
                        TVFocusButton(text = "—", onClick = { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }, isIcon = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        TVFocusButton(text = "+", onClick = { if (fontSize.value < 40) fontSize = (fontSize.value + 2).sp }, isIcon = true)
                        Spacer(modifier = Modifier.width(20.dp))
                        TVFocusButton(text = "✕", onClick = { showFullDescription.value = false }, isIcon = true)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(text = series.show.summary?.replace("<[^>]*>".toRegex(), "") ?: "", style = TVShadowStyle.copy(fontSize = fontSize, lineHeight = (fontSize.value * 1.5).sp))
                    }
                }
            }
        }
    }
}

// ... (Rest of your helper Composables like SectionTitle, TVFocusButton, etc. remain unchanged)
