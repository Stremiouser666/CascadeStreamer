package com.cascadestreamer.app.ui.templates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.ui.* import kotlinx.coroutines.launch

@Composable
fun EpisodeDetailsTemplate(
    episode: TVMazeEpisode,
    allEpisodesInSeason: List<TVMazeEpisode> = emptyList(),
    onPlay: () -> Unit = {},
    onBack: () -> Unit = {},
    onEpisodeSelected: (TVMazeEpisode) -> Unit = {}
) {
    val showFullDescription = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    BackHandler(enabled = true) { onBack() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            TVBackButton(onBack = onBack, label = "Back to Series")
            Spacer(modifier = Modifier.fillMaxWidth().height(415.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column {
                        TVFocusButton("▶ Play", onPlay, width = 160.dp, focusColor = Color(0xFF00A36C))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TVFocusButton("♡", {}, isIcon = true, focusColor = Color.Red)
                            TVFocusButton("↺", {}, isIcon = true)
                        }
                    }
                    Spacer(modifier = Modifier.width(28.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("E${episode.number}: ${episode.name}", style = TVShadowStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold))
                        Text("Season ${episode.season}  •  ${episode.runtime} min", style = TVShadowStyle.copy(fontSize = 15.sp, color = Color.Gray))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = episode.summary?.replace("<[^>]*>".toRegex(), "") ?: "No summary.",
                            style = TVShadowStyle.copy(fontSize = 15.sp),
                            maxLines = 3,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(12.dp).clickable { showFullDescription.value = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("MORE FROM THIS SEASON")
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                allEpisodesInSeason.forEach { ep ->
                    TVEpisodeCard(episode = ep, fallback = null, onClick = { onEpisodeSelected(ep) })
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showFullDescription.value) {
            EpisodeDescriptionDialog(title = "E${episode.number}: ${episode.name}", summary = episode.summary ?: "", onDismiss = { showFullDescription.value = false })
        }
    }
}

@Composable
fun EpisodeDescriptionDialog(title: String, summary: String, onDismiss: () -> Unit) {
    var fontSize by remember { mutableStateOf(18.sp) }
    val dialogScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).padding(60.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = TVShadowStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.weight(1f))
                    TVFocusButton("↑", { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value - 500) } }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("↓", { coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.value + 500) } }, isIcon = true)
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("Size: ", color = Color.Gray, fontSize = 14.sp)
                    TVFocusButton("—", { if (fontSize.value > 12) fontSize = (fontSize.value - 2).sp }, isIcon = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    TVFocusButton("+", { if (fontSize.value < 40) fontSize = (fontSize.value + 2).sp }, isIcon = true)
                    Spacer(modifier = Modifier.width(20.dp))
                    TVFocusButton("✕", onDismiss, isIcon = true)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(modifier = Modifier.verticalScroll(dialogScrollState)) {
                    Text(summary.replace("<[^>]*>".toRegex(), ""), style = TVShadowStyle.copy(fontSize = fontSize, lineHeight = (fontSize.value * 1.5).sp))
                }
            }
        }
    }
}
