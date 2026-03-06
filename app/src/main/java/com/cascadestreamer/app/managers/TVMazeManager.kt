package com.cascadestreamer.app.managers

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TVMazeManager {
    private val baseUrl = "https://api.tvmaze.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val service = retrofit.create(TVMazeService::class.java)
    
    suspend fun searchShows(query: String): List<TVMazeShow> {
        return withContext(Dispatchers.IO) {
            try {
                val results = service.searchShows(query)
                results.map { it.show }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    suspend fun getShowDetails(showId: Int): TVMazeShow? {
        return withContext(Dispatchers.IO) {
            try {
                service.getShowDetails(showId)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun getShowEpisodes(showId: Int): List<TVMazeEpisode> {
        return withContext(Dispatchers.IO) {
            try {
                service.getShowEpisodes(showId)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}

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
