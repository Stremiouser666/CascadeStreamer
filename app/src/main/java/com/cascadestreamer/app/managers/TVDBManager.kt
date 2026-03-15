package com.cascadestreamer.app.managers

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TVDBManager {
    private val apiKey = "598cad7b-56c2-47ab-b7d7-a95a4a807872"
    private val baseUrl = "https://api4.thetvdb.com/v4/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(TVDBService::class.java)

    suspend fun searchShows(query: String): List<TVDBShow> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.searchShows(apiKey, query)
                response.data ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getShowBackgroundImage(showId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getShowImages(apiKey, showId)
                response.data?.backgrounds?.firstOrNull()?.image
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

data class TVDBSearchResponse(
    val data: List<TVDBShow>?
)

data class TVDBShow(
    val id: Int,
    val name: String,
    val image: String? = null
)

data class TVDBImagesResponse(
    val data: TVDBImages?
)

data class TVDBImages(
    val backgrounds: List<TVDBImage>? = null
)

data class TVDBImage(
    val image: String
)
