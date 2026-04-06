package com.zen.embytv.data.repository

import com.zen.embytv.domain.emby.EmbySession
import com.zen.embytv.domain.media.MediaItem
import kotlinx.coroutines.flow.StateFlow

data class LibraryPageResult(
    val items: List<MediaItem>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val query: String,
)

interface EmbyRepository {
    val session: StateFlow<EmbySession?>
    val mediaItems: StateFlow<List<MediaItem>>

    suspend fun authenticate(serverUrl: String, username: String, password: String): Result<EmbySession>
    suspend fun refreshLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult>
    suspend fun sendPlaybackHeartbeat(itemId: String, positionMs: Long): Result<Unit>
    fun buildPlaybackUrl(itemId: String): String?
    fun clearSession()
}
