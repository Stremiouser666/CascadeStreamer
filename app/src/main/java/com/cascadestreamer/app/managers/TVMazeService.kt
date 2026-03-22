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

    @GET("shows/{id}/images")
    suspend fun getShowImages(@Path("id") showId: Int): List<TVMazeImageData>

    @GET("shows/{id}/cast")
    suspend fun getShowCast(@Path("id") showId: Int): List<TVMazeCastMember>

    @GET("people/{id}")
    suspend fun getPerson(@Path("id") personId: Int): TVMazePerson

    @GET("people/{id}/castcredits")
    suspend fun getPersonCastCredits(@Path("id") personId: Int): List<TVMazeCastCredit>
}

data class TVMazeShowSearchResult(
    val score: Double,
    val show: TVMazeShow
)

data class TVMazeCastCredit(
    val _links: TVMazeCastCreditLinks
)

data class TVMazeCastCreditLinks(
    val show: TVMazeLink? = null
)

data class TVMazeLink(
    val name: String? = null
)
