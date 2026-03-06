package com.cascadestreamer.app.managers

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FanArtService {
    
    @GET("tv/{tvdbId}")
    suspend fun getArtwork(
        @Path("tvdbId") tvdbId: Int,
        @Query("api_key") apiKey: String
    ): FanArtArtworkResponse
}

data class FanArtArtworkResponse(
    val tvposter: List<FanArtImageData>? = null,
    val tvthumb: List<FanArtImageData>? = null,
    val tvbackground: List<FanArtImageData>? = null,
    val logo: List<FanArtImageData>? = null
)

data class FanArtImageData(
    val id: String,
    val url: String,
    val likes: Int = 0
)
