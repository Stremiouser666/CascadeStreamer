package com.cascadestreamer.app.managers

data class FanArtImage(
    val id: String,
    val url: String,
    val likes: Int = 0
)

data class FanArtMetadata(
    val showId: Int,
    val name: String,
    val posters: List<FanArtImage> = emptyList(),
    val backdrops: List<FanArtImage> = emptyList(),
    val thumbnails: List<FanArtImage> = emptyList(),
    val logos: List<FanArtImage> = emptyList()
)

class FanArtManager(private val apiKey: String = "") {
    private val baseUrl = "https://webservice.fanart.tv/v3"
    
    fun getArtwork(tvdbId: Int): FanArtMetadata? {
        // TODO: GET /tv/{tvdbId}?api_key=KEY
        // Returns posters, backdrops, episode thumbs, logos
        return null
    }
    
    fun getShowPoster(tvdbId: Int): FanArtImage? {
        // TODO: Get highest-rated poster
        return null
    }
    
    fun getShowBackdrop(tvdbId: Int): FanArtImage? {
        // TODO: Get highest-rated backdrop
        return null
    }
    
    fun getEpisodeThumbnail(tvdbId: Int, season: Int, episode: Int): FanArtImage? {
        // TODO: Get episode-specific thumbnail
        return null
    }
}
