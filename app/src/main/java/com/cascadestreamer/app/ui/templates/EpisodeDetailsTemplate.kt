package com.cascadestreamer.app.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cascadestreamer.app.managers.TVMazeEpisode
import com.cascadestreamer.app.ui.DescriptionPopup
import com.cascadestreamer.app.ui.components.ImageFullscreenViewer
import com.cascadestreamer.app.ui.components.WatchedToggle
import com.cascadestreamer.app.ui.components.WatchedToggle

@Composable
fun EpisodeDetailsTemplate(
    episode: TVMazeEpisode,
    onWatchedToggle: (Boolean) -> Unit = {},
    isWatched: Boolean = false
) {
    val showDescriptionPopup = remember { mutableStateOf(false) }
    val showImageFullscreen = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with title + watched toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "E${episode.number}: ${episode.name}",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            WatchedToggle(
                isWatched = isWatched,
                onToggle = onWatchedToggle
            )
        }
        
        // Episode metadata
        Text(
            "Season ${episode.season} Episode ${episode.number} • ${episode.runtime ?: 0} min",
            fontSize = 12.sp,
            color = Color.Gray
        )
        
        // Episode image - clickable for fullscreen
        episode.image?.medium?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black)
                    .clickable { showImageFullscreen.value = true }
            ) {
                AsyncImage(
                    model = it,
                    contentDescription = episode.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    "Tap for fullscreen",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }
        
        // Description - clickable for popup
        episode.summary?.let {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    it.replace("<[^>]*>".toRegex(), ""),
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    maxLines = 3,
                    modifier = Modifier.clickable { showDescriptionPopup.value = true }
                )
                Text(
                    "Tap to expand ↕️",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
    
    // Description popup
    if (showDescriptionPopup.value) {
        DescriptionPopup(
            description = episode.summary ?: "",
            title = "E${episode.number}: ${episode.name}",
            onDismiss = { showDescriptionPopup.value = false }
        )
    }
    
    // Image fullscreen viewer
    if (showImageFullscreen.value) {
        ImageFullscreenViewer(
            imageUrl = episode.image?.medium,
            onDismiss = { showImageFullscreen.value = false }
        )
    }
}
