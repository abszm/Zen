package com.zen.embytv.data.repository

import com.zen.embytv.data.network.emby.EmbyClientFactory
import com.zen.embytv.data.network.emby.EmbyLoginRequest
import com.zen.embytv.data.network.emby.PlaybackProgressRequest
import com.zen.embytv.domain.emby.EmbySession
import com.zen.embytv.domain.media.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmbyRepositoryImpl : EmbyRepository {
    private val _session = MutableStateFlow<EmbySession?>(null)
    override val session: StateFlow<EmbySession?> = _session.asStateFlow()

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    override val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String,
    ): Result<EmbySession> = runCatching {
        val api = EmbyClientFactory.create(serverUrl)
        val response = api.login(
            request = EmbyLoginRequest(
                username = username,
                password = password,
            ),
        )
        EmbySession(
            serverUrl = normalizeServerUrl(serverUrl),
            userId = response.user.id,
            userName = response.user.name,
            token = response.accessToken,
        ).also {
            _session.value = it
        }
    }

    override suspend fun refreshLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult> = runCatching {
        val current = requireNotNull(_session.value) { "Please login to Emby first." }
        val api = EmbyClientFactory.create(current.serverUrl)
        val response = api.getUserItems(
            userId = current.userId,
            token = current.token,
            startIndex = (page.coerceAtLeast(1) - 1) * pageSize.coerceAtLeast(1),
            limit = pageSize.coerceAtLeast(1),
            searchTerm = query.ifBlank { null },
        )
        val mapped = response.items.map { item ->
            MediaItem(
                id = item.id,
                name = item.name,
                type = item.type,
            )
        }
        _mediaItems.value = mapped
        LibraryPageResult(
            items = mapped,
            totalCount = response.totalRecordCount ?: mapped.size,
            page = page,
            pageSize = pageSize,
            query = query,
        )
    }

    override suspend fun sendPlaybackHeartbeat(itemId: String, positionMs: Long): Result<Unit> = runCatching {
        val current = requireNotNull(_session.value) { "Please login to Emby first." }
        val api = EmbyClientFactory.create(current.serverUrl)
        api.reportProgress(
            token = current.token,
            request = PlaybackProgressRequest(
                itemId = itemId,
                positionTicks = positionMs * 10_000,
            ),
        )
    }

    override fun buildPlaybackUrl(itemId: String): String? {
        val current = _session.value ?: return null
        return "${current.serverUrl}Videos/$itemId/stream?static=true&api_key=${current.token}"
    }

    override fun clearSession() {
        _session.value = null
        _mediaItems.value = emptyList()
    }

    private fun normalizeServerUrl(rawUrl: String): String {
        val withScheme = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            "http://$rawUrl"
        }
        return if (withScheme.endsWith('/')) withScheme else "$withScheme/"
    }
}
