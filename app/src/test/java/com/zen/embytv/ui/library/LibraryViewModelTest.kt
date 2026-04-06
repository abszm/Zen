package com.zen.embytv.ui.library

import com.zen.embytv.data.repository.EmbyRepository
import com.zen.embytv.data.repository.LibraryPageResult
import com.zen.embytv.domain.emby.EmbyAuthService
import com.zen.embytv.domain.emby.EmbyLibraryService
import com.zen.embytv.domain.emby.EmbySession
import com.zen.embytv.domain.media.MediaItem
import com.zen.embytv.testutil.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_withBlankFields_setsValidationError() = runTest {
        val repository = FakeEmbyRepository()
        val authService = FakeAuthService(repository)
        val libraryService = FakeLibraryService()
        val viewModel = LibraryViewModel(authService, libraryService, repository)

        viewModel.login()
        advanceUntilIdle()

        assertEquals("Please fill server, username and password.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun login_success_fetchesFirstPage() = runTest {
        val repository = FakeEmbyRepository()
        val authService = FakeAuthService(repository)
        val libraryService = FakeLibraryService().apply {
            pageResultProvider = { page, pageSize, query ->
                LibraryPageResult(
                    items = listOf(
                        MediaItem("1", "Movie A", "Movie"),
                        MediaItem("2", "Movie B", "Movie"),
                    ),
                    totalCount = 42,
                    page = page,
                    pageSize = pageSize,
                    query = query,
                )
            }
        }
        val viewModel = LibraryViewModel(authService, libraryService, repository)

        viewModel.updateUsername("demo")
        viewModel.updatePassword("pass")
        viewModel.login()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.page)
        assertEquals(42, viewModel.uiState.value.totalCount)
        assertEquals("Fetched 2 items", viewModel.uiState.value.successMessage)
        assertEquals(listOf(1), libraryService.requestedPages)
    }

    @Test
    fun pagination_loadNextPage_requestsNextPage() = runTest {
        val repository = FakeEmbyRepository()
        val authService = FakeAuthService(repository)
        val libraryService = FakeLibraryService().apply {
            pageResultProvider = { page, pageSize, query ->
                LibraryPageResult(
                    items = listOf(MediaItem(page.toString(), "Movie-$page", "Movie")),
                    totalCount = 90,
                    page = page,
                    pageSize = pageSize,
                    query = query,
                )
            }
        }
        val viewModel = LibraryViewModel(authService, libraryService, repository)

        viewModel.updateUsername("demo")
        viewModel.updatePassword("pass")
        viewModel.login()
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.page)
        assertEquals(listOf(1, 2), libraryService.requestedPages)
    }

    @Test
    fun syncLibrary_networkError_showsReadableMessage() = runTest {
        val repository = FakeEmbyRepository()
        val authService = FakeAuthService(repository)
        val libraryService = FakeLibraryService().apply {
            error = IOException("connect failed")
        }
        val viewModel = LibraryViewModel(authService, libraryService, repository)

        viewModel.updateUsername("demo")
        viewModel.updatePassword("pass")
        viewModel.login()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage?.contains("Cannot reach Emby server") == true)
    }
}

private class FakeAuthService(
    private val repository: FakeEmbyRepository,
) : EmbyAuthService {
    override suspend fun login(serverUrl: String, username: String, password: String): Result<EmbySession> {
        val session = EmbySession(
            serverUrl = serverUrl,
            userId = "user-id",
            userName = username,
            token = "token",
        )
        repository.sessionFlow.value = session
        return Result.success(session)
    }

    override suspend fun logout(): Result<Unit> {
        repository.sessionFlow.value = null
        return Result.success(Unit)
    }
}

private class FakeLibraryService : EmbyLibraryService {
    var error: Throwable? = null
    var pageResultProvider: (page: Int, pageSize: Int, query: String) -> LibraryPageResult = { page, pageSize, query ->
        LibraryPageResult(emptyList(), 0, page, pageSize, query)
    }
    val requestedPages = mutableListOf<Int>()

    override suspend fun syncLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult> {
        requestedPages += page
        val currentError = error
        return if (currentError != null) {
            Result.failure(currentError)
        } else {
            Result.success(pageResultProvider(page, pageSize, query))
        }
    }
}

private class FakeEmbyRepository : EmbyRepository {
    val sessionFlow = MutableStateFlow<EmbySession?>(null)
    val mediaItemsFlow = MutableStateFlow<List<MediaItem>>(emptyList())

    override val session: StateFlow<EmbySession?> = sessionFlow
    override val mediaItems: StateFlow<List<MediaItem>> = mediaItemsFlow

    override suspend fun authenticate(serverUrl: String, username: String, password: String): Result<EmbySession> {
        return Result.failure(UnsupportedOperationException("unused in this test"))
    }

    override suspend fun refreshLibrary(page: Int, pageSize: Int, query: String): Result<LibraryPageResult> {
        return Result.failure(UnsupportedOperationException("unused in this test"))
    }

    override suspend fun sendPlaybackHeartbeat(itemId: String, positionMs: Long): Result<Unit> {
        return Result.success(Unit)
    }

    override fun buildPlaybackUrl(itemId: String): String? = "http://test/$itemId"

    override fun clearSession() {
        sessionFlow.value = null
        mediaItemsFlow.value = emptyList()
    }
}
