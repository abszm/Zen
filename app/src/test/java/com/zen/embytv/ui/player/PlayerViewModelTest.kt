package com.zen.embytv.ui.player

import com.zen.embytv.domain.emby.EmbyPlaybackService
import com.zen.embytv.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun open_setsCurrentMediaAndClearsError() {
        val playbackService = FakePlaybackService()
        val viewModel = PlayerViewModel(playbackService)

        viewModel.onPlaybackError("temporary error")
        viewModel.open(mediaItemId = "item-1", title = "Demo", streamUrl = "http://stream")

        val state = viewModel.uiState.value
        assertEquals("item-1", state.mediaItemId)
        assertEquals("Demo", state.mediaTitle)
        assertEquals("http://stream", state.streamUrl)
        assertNull(state.playbackErrorMessage)
    }

    @Test
    fun progressReporting_reportsHeartbeatOnInterval() = runTest {
        val playbackService = FakePlaybackService()
        val viewModel = PlayerViewModel(playbackService)

        viewModel.open(mediaItemId = "item-2", title = "Movie", streamUrl = "http://stream")
        viewModel.startProgressReporting(positionProvider = { 12_345L })

        advanceTimeBy(10_000)
        advanceUntilIdle()

        assertEquals(1, playbackService.reports.size)
        assertEquals("item-2", playbackService.reports.first().first)
        assertEquals(12_345L, playbackService.reports.first().second)

        viewModel.stopProgressReporting()
        advanceTimeBy(20_000)
        advanceUntilIdle()

        assertEquals(1, playbackService.reports.size)
    }

    @Test
    fun progressReporting_disabledForSmb_doesNotReport() = runTest {
        val playbackService = FakePlaybackService()
        val viewModel = PlayerViewModel(playbackService)

        viewModel.open(
            mediaItemId = "smb://nas/media/movie.mkv",
            title = "SMB Movie",
            streamUrl = "smb://nas/media/movie.mkv",
            reportPlaybackProgress = false,
        )
        viewModel.startProgressReporting(positionProvider = { 1_000L })

        advanceTimeBy(20_000)
        advanceUntilIdle()

        assertEquals(0, playbackService.reports.size)
    }
}

private class FakePlaybackService : EmbyPlaybackService {
    val reports = mutableListOf<Pair<String, Long>>()

    override suspend fun reportProgress(itemId: String, positionMs: Long): Result<Unit> {
        reports += itemId to positionMs
        return Result.success(Unit)
    }
}
