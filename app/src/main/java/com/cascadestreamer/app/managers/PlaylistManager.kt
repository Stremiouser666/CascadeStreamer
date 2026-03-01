package com.cascadestreamer.app.managers

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
        // TODO: Implement deletion
    }
    
    fun renamePlaylist(playlistId: String, newName: String): Playlist? {
        // TODO: Implement rename
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
