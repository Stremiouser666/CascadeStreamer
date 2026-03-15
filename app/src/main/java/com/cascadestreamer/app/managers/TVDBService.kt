package com.cascadestreamer.app.managers

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TVDBService {

    @GET("search")
    suspend fun searchShows(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String
    ): TVDBSearchResponse

    @GET("series/{id}/images")
    suspend fun getShowImages(
        @Header("Authorization") apiKey: String,
        @Path("id") showId: Int
    ): TVDBImagesResponse
}
