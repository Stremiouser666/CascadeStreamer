package com.cascadestreamer.app.managers

import retrofit2.http.*

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
