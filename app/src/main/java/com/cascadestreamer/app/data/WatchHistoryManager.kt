package com.cascadestreamer.app.data

import androidx.compose.runtime.mutableStateOf

data class WatchHistoryItem(
    val videoId: String,
    val videoTitle: String,
    val lastWatchedPosition: Long,
    val watchedAt: Long = System.currentTimeMillis()
)

class WatchHistoryManager {
    private val history = mutableStateOf<List<WatchHistoryItem>>(emptyList())
    
    fun addToHistory(video: Video, position: Long) {
        val item = WatchHistoryItem(
            videoId = video.id,
            videoTitle = video.title,
            lastWatchedPosition = position,
            watchedAt = System.currentTimeMillis()
        )
        
        val filtered = history.value.filter { it.videoId != video.id }
        history.value = listOf(item) + filtered
    }
    
    fun getHistory(): List<WatchHistoryItem> = history.value
    
    fun getWatchPosition(videoId: String): Long {
        return history.value.find { it.videoId == videoId }?.lastWatchedPosition ?: 0L
    }
    
    fun clearHistory() {
        history.value = emptyList()
    }
}
