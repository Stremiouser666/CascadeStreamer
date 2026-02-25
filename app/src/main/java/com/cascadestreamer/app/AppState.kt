package com.cascadestreamer.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class AppState(
    private val repository: VideoRepository = VideoRepository()
) {
    private val _videos = mutableStateOf<List<Video>>(emptyList())
    val videos: State<List<Video>> = _videos
    
    private val _playlists = mutableStateOf<List<Playlist>>(emptyList())
    val playlists: State<List<Playlist>> = _playlists
    
    private val _currentVideo = mutableStateOf<Video?>(null)
    val currentVideo: State<Video?> = _currentVideo
    
    private val _currentPlaylist = mutableStateOf<Playlist?>(null)
    val currentPlaylist: State<Playlist?> = _currentPlaylist
    
    fun addVideo(video: Video) {
        repository.addVideo(video)
        _videos.value = repository.getVideos()
    }
    
    fun createPlaylist(name: String) {
        repository.createPlaylist(name)
        _playlists.value = repository.getPlaylists()
    }
    
    fun playVideo(video: Video) {
        _currentVideo.value = video
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
        }
    }
}
