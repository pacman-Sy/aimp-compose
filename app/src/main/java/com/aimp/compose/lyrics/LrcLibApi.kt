package com.aimp.compose.lyrics

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface LrcLibApi {
    @Headers("User-Agent: LRCGET v0.2.0 (https://github.com/pacman-Sy/aimp-lrclib-player)")
    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") duration: Int? = null
    ): Response<LrcResponse>

    @Headers("User-Agent: LRCGET v0.2.0 (https://github.com/pacman-Sy/aimp-lrclib-player)")
    @GET("api/search")
    suspend fun search(@Query("q") query: String): Response<List<LrcResponse>>
}
