package com.cascadestreamer.app.managers

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "apikey") val apiKey: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val data: TokenData
)

@JsonClass(generateAdapter = true)
data class TokenData(
    val token: String
)

@JsonClass(generateAdapter = true)
data class ArtworksResponse(
    val data: ArtworksData
)

@JsonClass(generateAdapter = true)
data class ArtworksData(
    val artworks: List<Artwork>?
)

@JsonClass(generateAdapter = true)
data class Artwork(
    val id: Int,
    val type: Int,
    val image: String,
    val thumbnail: String? = null,
    val rating: Double? = null
)

interface TVDBService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("series/{seriesId}/artworks")
    suspend fun getArtworks(
        @Header("Authorization") bearer: String,
        @Path("seriesId") seriesId: Int,
        @Query("lang") lang: String? = "eng",
        @Query("type") type: Int? = null
    ): ArtworksResponse
}

class TVDBManager {
    private val apiKey = "598cad7b-56c2-47ab-b7d7-a95a4a807872"
    private val baseUrl = "https://api4.thetvdb.com/v4/"
    private var token: String? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service = retrofit.create(TVDBService::class.java)

    private suspend fun getToken(): String {
        if (token != null) return token!!

        return withContext(Dispatchers.IO) {
            try {
                val response = service.login(LoginRequest(apiKey))
                token = response.data.token
                token!!
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    suspend fun getShowBackgroundImage(seriesId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                val bearer = getToken()
                if (bearer.isEmpty()) return@withContext null

                val response = service.getArtworks(
                    bearer = "Bearer $bearer",
                    seriesId = seriesId,
                    type = 3  // 3 = fanart/background
                )

                response.data.artworks?.firstOrNull()?.image
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
