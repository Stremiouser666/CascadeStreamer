package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cascadestreamer.app.managers.TVMazeManager
import com.cascadestreamer.app.managers.TVMazeShow
import kotlinx.coroutines.launch

@Composable
fun SearchSeriesScreen(
    onSeriesSelected: (TVMazeShow) -> Unit,
    onBack: () -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateOf<List<TVMazeShow>>(emptyList()) }
    val isSearching = remember { mutableStateOf(false) }
    val tvMazeManager = remember { TVMazeManager() }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            "Search Series",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Search input area - TV friendly
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                "Enter show name:",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            TextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("e.g., Breaking Bad") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }
        
        // Search button - Always accessible
        Button(
            onClick = {
                if (searchQuery.value.isNotBlank()) {
                    isSearching.value = true
                    scope.launch {
                        searchResults.value = tvMazeManager.searchShows(searchQuery.value)
                        isSearching.value = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("🔍 Search", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Results
        if (isSearching.value) {
            Text(
                "Searching...",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else if (searchResults.value.isEmpty() && searchQuery.value.isNotBlank()) {
            Text(
                "No results found",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else if (searchResults.value.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                searchResults.value.forEach { show ->
                    SeriesSearchResult(
                        show = show,
                        onClick = { onSeriesSelected(show) }
                    )
                }
            }
        } else {
            Text(
                "Enter a show name and tap Search",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("← Back", color = Color.White)
        }
    }
}

@Composable
fun SeriesSearchResult(
    show: TVMazeShow,
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
            show.name,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            "ID: ${show.id} | ${show.premiered?.take(4) ?: "N/A"}",
            fontSize = 12.sp,
            color = Color.Gray
        )
        show.summary?.let {
            Text(
                it.replace("<[^>]*>".toRegex(), ""),
                fontSize = 11.sp,
                color = Color.LightGray,
                maxLines = 2,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
