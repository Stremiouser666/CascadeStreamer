package com.cascadestreamer.app.data

class VideoRepository {
    private val videos = mutableListOf<Video>()
    private val playlists = mutableListOf<Playlist>()
    
    fun addVideo(video: Video) {
        videos.add(video)
    }
    
    fun getVideos(): List<Video> = videos.toList()
    
    fun createPlaylist(name: String): Playlist {
        val playlist = Playlist(
            id = System.currentTimeMillis().toString(),
            name = name
        )
        playlists.add(playlist)
        return playlist
    }
    
    fun getPlaylists(): List<Playlist> = playlists.toList()
    
    fun addVideoToPlaylist(playlistId: String, video: Video) {
        val playlist = playlists.find { it.id == playlistId }
        playlist?.let {
            val updated = it.copy(videos = it.videos + video)
            playlists[playlists.indexOf(it)] = updated
        }
    }
}
