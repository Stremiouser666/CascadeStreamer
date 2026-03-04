package com.cascadestreamer.app.states

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.cascadestreamer.app.data.Video
import com.cascadestreamer.app.data.VideoRepository
import com.cascadestreamer.app.data.Playlist
import com.cascadestreamer.app.managers.YtDlpManager
import com.cascadestreamer.app.storage.LocalStorageManager

class AppState(
    private val repository: VideoRepository = VideoRepository(),
    private val ytDlpManager: YtDlpManager = YtDlpManager(),
    private val storageManager: LocalStorageManager? = null
) {
    private val _videos = mutableStateOf<List<Video>>(emptyList())
    val videos: State<List<Video>> = _videos
    
    private val _playlists = mutableStateOf<List<Playlist>>(emptyList())
    val playlists: State<List<Playlist>> = _playlists
    
    private val _currentVideo = mutableStateOf<Video?>(null)
    val currentVideo: State<Video?> = _currentVideo
    
    private val _currentPlaylist = mutableStateOf<Playlist?>(null)
    val currentPlaylist: State<Playlist?> = _currentPlaylist
    
    private val _availableQualities = mutableStateOf<List<String>>(emptyList())
    val availableQualities: State<List<String>> = _availableQualities
    
    init {
        // Load saved videos on startup
        loadFromStorage()
    }
    
    private fun loadFromStorage() {
        storageManager?.let {
            try {
                val savedVideos = it.loadVideos()
                if (savedVideos.isNotEmpty()) {
                    _videos.value = savedVideos
                }
                val savedPlaylists = it.loadPlaylists()
                if (savedPlaylists.isNotEmpty()) {
                    _playlists.value = savedPlaylists
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun addVideo(video: Video) {
        repository.addVideo(video)
        _videos.value = repository.getVideos()
        // Save to persistent storage
        storageManager?.saveVideos(_videos.value)
    }
    
    fun createPlaylist(name: String) {
        repository.createPlaylist(name)
        _playlists.value = repository.getPlaylists()
        // Save to persistent storage
        storageManager?.savePlaylists(_playlists.value)
    }
    
    fun playVideo(video: Video) {
        _currentVideo.value = video
        _availableQualities.value = ytDlpManager.getAvailableQualities(video.url)
    }
    
    fun selectPlaylist(playlist: Playlist) {
        _currentPlaylist.value = playlist
    }
    
    fun updateVideoProgress(videoId: String, position: Long) {
        val video = _videos.value.find { it.id == videoId }
        video?.let {
            val updated = it.copy(currentPosition = position)
            _videos.value = _videos.value.map { v ->
                if (v.id == videoId) updated else v
            }
            // Save updated progress
            storageManager?.saveVideos(_videos.value)
        }
    }
    
    fun getStreamUrl(videoUrl: String, quality: String): String {
        return ytDlpManager.getStreamUrlForQuality(videoUrl, quality)
    }
}
