package com.cascadestreamer.app.managers

import com.google.gson.Gson

data class TVMazeShow(
    val id: Int,
    val name: String,
    val premiered: String? = null,
    val rating: TVMazeRating? = null,
    val summary: String? = null,
    val genres: List<String> = emptyList(),
    val image: TVMazeImage? = null
)

data class TVMazeRating(
    val average: Double? = null
)

data class TVMazeImage(
    val medium: String? = null,
    val original: String? = null
)

data class TVMazeEpisode(
    val id: Int,
    val name: String,
    val number: Int? = null,
    val season: Int? = null,
    val summary: String? = null,
    val runtime: Int? = null,
    val image: TVMazeImage? = null
)

class TVMazeManager {
    private val gson = Gson()
    private val baseUrl = "https://api.tvmaze.com"
    
    fun searchShows(query: String): List<TVMazeShow> {
        // TODO: GET /search/shows?q=QUERY
        return emptyList()
    }
    
    fun getShowDetails(showId: Int): TVMazeShow? {
        // TODO: GET /shows/{id}
        return null
    }
    
    fun getShowEpisodes(showId: Int): List<TVMazeEpisode> {
        // TODO: GET /shows/{id}/episodes
        return emptyList()
    }
}
