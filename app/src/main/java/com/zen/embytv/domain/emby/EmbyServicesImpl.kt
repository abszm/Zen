package com.zen.embytv.domain.emby

import com.zen.embytv.data.repository.EmbyRepository
import com.zen.embytv.data.repository.LibraryPageResult

class EmbyAuthServiceImpl(
    private val repository: EmbyRepository,
) : EmbyAuthService {
    override suspend fun login(
        serverUrl: String,
        username: String,
        password: String,
    ): Result<EmbySession> = repository.authenticate(
        serverUrl = serverUrl,
        username = username,
        password = password,
    )

    override suspend fun logout(): Result<Unit> = runCatching {
        repository.clearSession()
    }
}

class EmbyLibraryServiceImpl(
    private val repository: EmbyRepository,
) : EmbyLibraryService {
    override suspend fun syncLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult> {
        return repository.refreshLibrary(page = page, pageSize = pageSize, query = query)
    }
}

class EmbyPlaybackServiceImpl(
    private val repository: EmbyRepository,
) : EmbyPlaybackService {
    override suspend fun reportProgress(itemId: String, positionMs: Long): Result<Unit> {
        return repository.sendPlaybackHeartbeat(itemId = itemId, positionMs = positionMs)
    }
}
