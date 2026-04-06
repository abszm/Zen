package com.zen.embytv.data.network.emby

import com.squareup.moshi.Json

data class EmbyLoginRequest(
    @Json(name = "Username") val username: String,
    @Json(name = "Pw") val password: String,
)

data class EmbyLoginResponse(
    @Json(name = "AccessToken") val accessToken: String,
    @Json(name = "User") val user: EmbyUser,
)

data class EmbyUser(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String,
)

data class EmbyItemsResponse(
    @Json(name = "Items") val items: List<EmbyItem>,
    @Json(name = "TotalRecordCount") val totalRecordCount: Int? = null,
)

data class EmbyItem(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String,
    @Json(name = "Type") val type: String,
)

data class PlaybackProgressRequest(
    @Json(name = "ItemId") val itemId: String,
    @Json(name = "PositionTicks") val positionTicks: Long,
)
