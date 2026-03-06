package com.cascadestreamer.app.managers

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TVMazeService {
    
    @GET("search/shows")
    suspend fun searchShows(@Query("q") query: String): List<TVMazeShowSearchResult>
    
    @GET("shows/{id}")
    suspend fun getShowDetails(@Path("id") showId: Int): TVMazeShow
    
    @GET("shows/{id}/episodes")
    suspend fun getShowEpisodes(@Path("id") showId: Int): List<TVMazeEpisode>
}

data class TVMazeShowSearchResult(
    val score: Double,
    val show: TVMazeShow
)
