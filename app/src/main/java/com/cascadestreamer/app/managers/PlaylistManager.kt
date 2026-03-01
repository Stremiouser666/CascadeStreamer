package com.cascadestreamer.app.managers

import com.cascadestreamer.app.data.Video
import com.cascadestreamer.app.data.Playlist
import com.cascadestreamer.app.data.VideoRepository

class PlaylistManager(
    private val repository: VideoRepository = VideoRepository()
) {
    
    fun createPlaylist(name: String): Playlist {
        val playlist = Playlist(
            id = System.currentTimeMillis().toString(),
            name = name,
            videos = emptyList()
        )
        return playlist
    }
    
    fun deletePlaylist(playlistId: String) {
    }
    
    fun renamePlaylist(playlistId: String, newName: String): Playlist? {
        return null
    }
    
    fun addVideoToPlaylist(playlistId: String, video: Video): Playlist? {
        val playlist = repository.getPlaylists().find { it.id == playlistId }
        playlist?.let {
            val updated = it.copy(videos = it.videos + video)
            repository.addVideoToPlaylist(playlistId, video)
            return updated
        }
        return null
    }
    
    fun removeVideoFromPlaylist(playlistId: String, videoId: String): Playlist? {
        val playlist = repository.getPlaylists().find { it.id == playlistId }
        playlist?.let {
            val updated = it.copy(videos = it.videos.filter { v -> v.id != videoId })
            return updated
        }
        return null
    }
}
