package com.zen.embytv.ui.library

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zen.embytv.data.repository.EmbyRepository
import com.zen.embytv.data.repository.LibraryPageResult
import com.zen.embytv.domain.emby.EmbyAuthService
import com.zen.embytv.domain.emby.EmbyLibraryService
import com.zen.embytv.domain.emby.EmbySession
import com.zen.embytv.domain.media.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun libraryScreen_showsBasicSections() {
        val repository = FakeUiRepository()
        val viewModel = LibraryViewModel(
            authService = FakeUiAuthService(repository),
            libraryService = FakeUiLibraryService(),
            repository = repository,
        )

        composeRule.setContent {
            LibraryScreen(viewModel = viewModel, onPlayRequest = { _, _ -> })
        }

        composeRule.onNodeWithText("Emby Login").assertExists()
        composeRule.onNodeWithText("Connect").assertExists()
        composeRule.onNodeWithText("Media Library (0)").assertExists()
        composeRule.onNodeWithText("No media loaded. Connect and Sync first.").assertExists()
    }
}

private class FakeUiRepository : EmbyRepository {
    private val sessionFlow = MutableStateFlow<EmbySession?>(null)
    private val mediaItemsFlow = MutableStateFlow<List<MediaItem>>(emptyList())

    override val session: StateFlow<EmbySession?> = sessionFlow
    override val mediaItems: StateFlow<List<MediaItem>> = mediaItemsFlow

    override suspend fun authenticate(serverUrl: String, username: String, password: String): Result<EmbySession> {
        return Result.failure(UnsupportedOperationException("unused in this test"))
    }

    override suspend fun refreshLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult> {
        return Result.success(
            LibraryPageResult(
                items = emptyList(),
                totalCount = 0,
                page = page,
                pageSize = pageSize,
                query = query,
            ),
        )
    }

    override suspend fun sendPlaybackHeartbeat(itemId: String, positionMs: Long): Result<Unit> {
        return Result.success(Unit)
    }

    override fun buildPlaybackUrl(itemId: String): String? = null

    override fun clearSession() {
        sessionFlow.value = null
        mediaItemsFlow.value = emptyList()
    }
}

private class FakeUiAuthService(
    private val repository: FakeUiRepository,
) : EmbyAuthService {
    override suspend fun login(serverUrl: String, username: String, password: String): Result<EmbySession> {
        val session = EmbySession(serverUrl, "user-id", username, "token")
        return Result.success(session)
    }

    override suspend fun logout(): Result<Unit> {
        repository.clearSession()
        return Result.success(Unit)
    }
}

private class FakeUiLibraryService : EmbyLibraryService {
    override suspend fun syncLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult> {
        return Result.success(
            LibraryPageResult(
                items = emptyList(),
                totalCount = 0,
                page = page,
                pageSize = pageSize,
                query = query,
            ),
        )
    }
}
