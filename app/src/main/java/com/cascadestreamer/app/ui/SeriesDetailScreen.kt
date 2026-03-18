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

// ... (CastMember and SeriesData data classes stay at top)

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

    if (selectedCast.value != null) {
        ActorWikiProfile(member = selectedCast.value!!, onBack = { selectedCast.value = null })
        return
    }

    if (selectedEpisode.value != null) {
        // ... (Episode details template overlay remains unchanged)
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // BACKGROUND IMAGE
        AsyncImage(
            model = series.backdropUrl ?: series.show.image?.original,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            // 1. INVISIBLE FOCUSABLE SPACER (For Scroll-to-top)
            Spacer(modifier = Modifier.size(1.dp).focusable())

            // 2. THE GAP (Moves your text/icons from Red Arrow to Green Arrow position)
            Spacer(modifier = Modifier.fillMaxWidth().height(450.dp))

            // HERO CONTENT (Play, Rating, Summary)
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

            // SEASONS
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("SEASONS")
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                allSeasons.value.forEach { season ->
                    TVSeasonSelectButton(
                        season = season, 
                        isSelected = selectedSeason.intValue == season, 
                        onClick = { selectedSeason.intValue = season } // Season logic restored
                    )
                }
            }

            // EPISODES
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("EPISODES")
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                episodesInSeason.value.forEach { episode ->
                    TVEpisodeCard(episode = episode, fallback = series.posterUrl, onClick = { selectedEpisode.value = episode })
                }
            }

            // CAST
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
    
    // ... (Dialog logic for summary with scroll arrows remains unchanged)
}

@Composable
fun TVFocusButton(text: String, onClick: () -> Unit, width: androidx.compose.ui.unit.Dp = 60.dp, isIcon: Boolean = false, focusColor: Color = Color.White) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Button(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.height(if (isIcon) 48.dp else 52.dp).then(if (isIcon) Modifier.width(48.dp) else Modifier.width(width)),
        colors = ButtonDefaults.buttonColors(
            // UPDATED: 15% Darker transparency
            containerColor = if (isFocused) focusColor else Color.Black.copy(alpha = 0.15f),
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        border = if (!isFocused) BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = if (isIcon) 20.sp else 16.sp)
    }
}

@Composable
fun TVSeasonSelectButton(season: Int, isSelected: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Button(
        onClick = onClick,
        interactionSource = source,
        colors = ButtonDefaults.buttonColors(
            // UPDATED: 15% Darker transparency
            containerColor = when { 
                isFocused -> Color.White 
                isSelected -> Color(0xFF388E3C) 
                else -> Color.Black.copy(alpha = 0.15f) 
            }, 
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isFocused && !isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Text("Season $season", fontWeight = FontWeight.Bold)
    }
}
