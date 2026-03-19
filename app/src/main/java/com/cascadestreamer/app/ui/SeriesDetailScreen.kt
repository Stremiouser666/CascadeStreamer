package com.cascadestreamer.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

        // LAYER 1: MAIN CONTENT
        if (selectedEpisode.value == null && selectedCast.value == null) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                TVBackButton(onBack = onBack, label = "Back to Home")

                // Cinematic Vertical Spacing
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
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .clickable { showFullDescription.value = true }
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
                        TVEpisodeCard(
                            episode = episode, 
                            fallback = series.posterUrl,
                            isWatched = false, 
                            watchedPercentage = 0, 
                            onClick = { selectedEpisode.value = episode }
                        )
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

        // LAYER 2: EPISODE OVERLAY
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

        // LAYER 4: SUMMARY POPUP (FIXED CONTROLS & FADE)
        if (showFullDescription.value) {
            SeriesDescriptionDialog(
                title = "Summary", 
                summary = series.show.summary ?: "", 
                onDismiss = { showFullDescription.value = false }
            )
        }
    }
}

// --- HELPER COMPONENTS ---

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
    val containerColor = if (isFocused) focusColor else Color.Black.copy(alpha = 0.4f)
    val textColor = if (isFocused) Color.Black else Color.White

    Button(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.height(52.dp).then(if (isIcon) Modifier.width(52.dp) else Modifier.width(width)),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black, color = textColor)
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
    val containerColor = when {
        isFocused -> Color.White
        isSelected -> Color(0xFF388E3C)
        else -> Color.DarkGray
    }
    val textColor = if (isFocused) Color.Black else Color.White

    Button(
        onClick = onClick,
        interactionSource = source,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Season $season", color = textColor)
    }
}

@Composable
fun TVEpisodeCard(
    episode: TVMazeEpisode, 
    fallback: String?, 
    isWatched: Boolean = false, 
    watchedPercentage: Int = 0, 
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()

    Column(modifier = Modifier.width(280.dp).clickable(source, null) { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(12.dp))
                .border(if (isFocused) 4.dp else 0.dp, Color.White, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(model = episode.image?.medium ?: fallback, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            if (watchedPercentage > 0) {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.5f))) {
                    Box(modifier = Modifier.fillMaxWidth(watchedPercentage / 100f).fillMaxHeight().background(Color(0xFF4CAF50)))
                }
            }

            if (isWatched) {
                Box(modifier = Modifier.padding(8.dp).size(28.dp).background(Color.Black.copy(alpha = 0.7f), CircleShape).align(Alignment.TopEnd), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                }
            }
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
        Text(member.name, style = TVShadowStyle.copy(fontSize = 12.sp), maxLines = 1, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActorWikiProfile(member: CastMember, onBack: () -> Unit) {
    BackHandler { onBack() }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f))) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            TVBackButton(onBack = onBack, label = "Actor Profile")
            Row(modifier = Modifier.padding(40.dp), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                AsyncImage(model = member.imageUrl, contentDescription = null, modifier = Modifier.size(300.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Column {
                    Text(member.name, style = TVShadowStyle.copy(fontSize = 42.sp, fontWeight = FontWeight.Black))
                    Text("as ${member.character}", style = TVShadowStyle.copy(fontSize = 22.sp, color = Color.Gray))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(member.biography ?: "Biography details coming soon.", style = TVShadowStyle.copy(fontSize = 18.sp, lineHeight = 28.sp))
                }
            }
        }
    }
}

// --- POPUP DIALOG WITH YOUR PREFERRED CONTROLS & FADE EFFECT ---
@Composable
fun SeriesDescriptionDialog(title: String, summary: String, onDismiss: () -> Unit) {
    var fontSize by remember { mutableStateOf(18.sp) }
    val dialogScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss, 
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .padding(60.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)) {
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    TVFocusButton("↑", { 
    coroutineScope.launch { 
        dialogScrollState.animateScrollBy(-80f, tween(200, easing = FastOutSlowInEasing)) 
    } 
}, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("↓", { 
    coroutineScope.launch { 
        // 80f is roughly 1-2 lines of text. 
        // 200ms makes the "tick" feel instant but smooth.
        dialogScrollState.animateScrollBy(80f, tween(200, easing = FastOutSlowInEasing)) 
    } 
}, isIcon = true)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text("Size: ", color = Color.Gray, fontSize = 14.sp)
                    TVFocusButton("—", { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("+", { if (fontSize.value < 40) fontSize = (fontSize.value + 2).sp }, isIcon = true)
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    TVFocusButton("✕", onDismiss, isIcon = true, focusColor = Color.Red)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(dialogScrollState)) {
                        Text(
                            text = summary.replace("<[^>]*>".toRegex(), ""), 
                            style = TVShadowStyle.copy(
                                fontSize = fontSize, 
                                lineHeight = (fontSize.value * 1.5).sp
                            )
                        )
                    }

                    // --- SOFTER FADE EDGES ---
                    val fadeColor = Color.Black.copy(alpha = 0.7f) // Semi-transparent instead of solid

                    // Top Fade
                    Box(modifier = Modifier.fillMaxWidth().height(25.dp).align(Alignment.TopCenter)
                        .background(Brush.verticalGradient(listOf(fadeColor, Color.Transparent))))

                    // Bottom Fade
                    Box(modifier = Modifier.fillMaxWidth().height(25.dp).align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, fadeColor))))
                }
            }
        }
    }
}