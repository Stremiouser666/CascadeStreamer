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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val imageUrl = series.backdropUrl ?: series.show.image?.original
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            // RESTORED: Hidden focusable anchor for scroll-to-top logic
            Spacer(modifier = Modifier.height(1.dp).focusable())

            Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
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

    if (showFullDescription.value) {
        var fontSize by remember { mutableStateOf(18.sp) }
        val dialogScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        Dialog(onDismissRequest = { showFullDescription.value = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).padding(60.dp)) {
                Column(modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Summary", style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // RESTORED: Scroll Arrows
                        TVFocusButton(text = "↑", onClick = { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value - 500) } }, isIcon = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        TVFocusButton(text = "↓", onClick = { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value + 500) } }, isIcon = true)
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        Text("Size: ", color = Color.Gray, fontSize = 14.sp)
                        TVFocusButton(text = "—", onClick = { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }, isIcon = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        TVFocusButton(text = "+", onClick = { if (fontSize.value < 40) fontSize = (fontSize.value + 2).sp }, isIcon = true)
                        Spacer(modifier = Modifier.width(20.dp))
                        TVFocusButton(text = "✕", onClick = { showFullDescription.value = false }, isIcon = true)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Column(modifier = Modifier.verticalScroll(dialogScrollState)) {
                        Text(text = series.show.summary?.replace("<[^>]*>".toRegex(), "") ?: "", style = TVShadowStyle.copy(fontSize = fontSize, lineHeight = (fontSize.value * 1.5).sp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text = text, modifier = Modifier.padding(horizontal = 32.dp, vertical = 10.dp), style = TVShadowStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Gray))
}

@Composable
fun TVBackButton(onBack: () -> Unit, label: String) {
    Row(modifier = Modifier.padding(24.dp).clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        Spacer(modifier = Modifier.width(12.dp))
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
        modifier = Modifier.height(if (isIcon) 48.dp else 52.dp).then(if (isIcon) Modifier.width(48.dp) else Modifier.width(width)),
        colors = ButtonDefaults.buttonColors(
            // FIXED: 15% Dark/Grey transparent background when not focused
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
            // FIXED: 15% Dark/Grey transparent background when not focused/selected
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

@Composable
fun TVEpisodeCard(episode: TVMazeEpisode, fallback: String?, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Column(modifier = Modifier.width(280.dp).clickable(source, null) { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(12.dp)).background(Color(0xFF151515)).then(if (isFocused) Modifier.border(4.dp, Color.White, RoundedCornerShape(12.dp)).padding(4.dp) else Modifier)) {
            AsyncImage(model = episode.image?.medium ?: fallback, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("E${episode.number}: ${episode.name ?: "Untitled"}", style = TVShadowStyle.copy(fontSize = 15.sp, fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Medium), maxLines = 1)
    }
}

@Composable
fun TVCastCircleCard(member: CastMember, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isFocused by source.collectIsFocusedAsState()
    Column(modifier = Modifier.width(130.dp).clickable(source, null) { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(if (isFocused) Color.White else Color(0xFF151515)).then(if (isFocused) Modifier.padding(5.dp).clip(CircleShape) else Modifier)) {
            AsyncImage(model = member.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(member.name, style = TVShadowStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        Text(member.character, style = TVShadowStyle.copy(fontSize = 12.sp, color = Color.Gray), maxLines = 1)
    }
}

@Composable
fun ActorWikiProfile(member: CastMember, onBack: () -> Unit) {
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
