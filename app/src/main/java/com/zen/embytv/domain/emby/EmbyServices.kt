package com.zen.embytv.domain.emby

import com.zen.embytv.data.repository.LibraryPageResult

interface EmbyAuthService {
    suspend fun login(serverUrl: String, username: String, password: String): Result<EmbySession>
    suspend fun logout(): Result<Unit>
}

interface EmbyLibraryService {
    suspend fun syncLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult>
}

interface EmbyPlaybackService {
    suspend fun reportProgress(itemId: String, positionMs: Long): Result<Unit>
}
