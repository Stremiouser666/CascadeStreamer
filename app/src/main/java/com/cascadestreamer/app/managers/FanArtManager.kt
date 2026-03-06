package com.cascadestreamer.app.managers

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FanArtManager(private val apiKey: String = "") {
    private val baseUrl = "https://webservice.fanart.tv/v3/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val service = retrofit.create(FanArtService::class.java)
    
    suspend fun getArtwork(tvdbId: Int): FanArtMetadata? {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getArtwork(tvdbId, apiKey)
                FanArtMetadata(
                    showId = tvdbId,
                    name = "",
                    posters = response.tvposter?.map { FanArtImage(it.id, it.url, it.likes) } ?: emptyList(),
                    backdrops = response.tvbackground?.map { FanArtImage(it.id, it.url, it.likes) } ?: emptyList(),
                    thumbnails = response.tvthumb?.map { FanArtImage(it.id, it.url, it.likes) } ?: emptyList(),
                    logos = response.logo?.map { FanArtImage(it.id, it.url, it.likes) } ?: emptyList()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun getShowBackdrop(tvdbId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                val artwork = getArtwork(tvdbId)
                artwork?.backdrops?.maxByOrNull { it.likes }?.url
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun getShowPoster(tvdbId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                val artwork = getArtwork(tvdbId)
                artwork?.posters?.maxByOrNull { it.likes }?.url
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

data class FanArtMetadata(
    val showId: Int,
    val name: String,
    val posters: List<FanArtImage> = emptyList(),
    val backdrops: List<FanArtImage> = emptyList(),
    val thumbnails: List<FanArtImage> = emptyList(),
    val logos: List<FanArtImage> = emptyList()
)

data class FanArtImage(
    val id: String,
    val url: String,
    val likes: Int = 0
)
