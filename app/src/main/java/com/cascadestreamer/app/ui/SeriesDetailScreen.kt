package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.cascadestreamer.app.managers.TVMazeShow

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
            series.backdropUrl?.let {
                AsyncImage(
                    model = it,
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
        
        // Play button + action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("▶ Play", color = Color.White, fontSize = 16.sp)
            }
            
            Button(
                onClick = {},
                modifier = Modifier.size(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
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
                    maxLines = 3
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Season selector
        Text(
            "Season ${selectedSeason.value}",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Episodes list
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            series.episodes.forEachIndexed { index, episode ->
                EpisodeCard(
                    episode = episode,
                    episodeNumber = index + 1,
                    onClick = { /* Play episode */ }
                )
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
}

@Composable
fun EpisodeCard(
    episode: TVMazeEpisode,
    episodeNumber: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            "$episodeNumber. ${episode.name}",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "S${episode.season}E${episode.number} • ${episode.runtime ?: 0} min",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        episode.summary?.let {
            Text(
                it.replace("<[^>]*>".toRegex(), ""),
                fontSize = 12.sp,
                color = Color.LightGray,
                maxLines = 2,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
