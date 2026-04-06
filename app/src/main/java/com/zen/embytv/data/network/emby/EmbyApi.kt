package com.zen.embytv.data.network.emby

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyApi {
    @POST("Users/AuthenticateByName")
    suspend fun login(@Body request: EmbyLoginRequest): EmbyLoginResponse

    @GET("Users/{userId}/Items")
    suspend fun getUserItems(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") token: String,
        @Query("Recursive") recursive: Boolean = true,
        @Query("IncludeItemTypes") includeItemTypes: String = "Movie,Episode",
        @Query("Fields") fields: String = "MediaSources",
        @Query("SortBy") sortBy: String = "SortName",
        @Query("StartIndex") startIndex: Int = 0,
        @Query("Limit") limit: Int = 30,
        @Query("SearchTerm") searchTerm: String? = null,
    ): EmbyItemsResponse

    @POST("Sessions/Playing/Progress")
    suspend fun reportProgress(
        @Header("X-Emby-Token") token: String,
        @Body request: PlaybackProgressRequest,
    )
}
