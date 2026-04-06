package com.zen.embytv.domain.emby

data class EmbySession(
    val serverUrl: String,
    val userId: String,
    val userName: String,
    val token: String,
)
