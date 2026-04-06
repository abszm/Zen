package com.zen.embytv.ui.smb

import com.zen.embytv.domain.local.SmbEntry
import com.zen.embytv.domain.local.SmbMountService
import com.zen.embytv.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmbBrowserViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun connect_withBlankHost_setsValidationError() = runTest {
        val service = FakeSmbMountService()
        val viewModel = SmbBrowserViewModel(service)

        viewModel.connect()
        advanceUntilIdle()

        assertEquals("Please fill host and share.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun connect_success_loadsRootEntries() = runTest {
        val service = FakeSmbMountService().apply {
            listResult = listOf(
                SmbEntry(name = "Movies", path = "/Movies", isDirectory = true),
                SmbEntry(name = "Episode-1.mkv", path = "/Episode-1.mkv", isDirectory = false),
            )
        }
        val viewModel = SmbBrowserViewModel(service)
        viewModel.updateHost("nas")
        viewModel.updateShare("media")

        viewModel.connect()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.connected)
        assertEquals("/", viewModel.uiState.value.currentPath)
        assertEquals(2, viewModel.uiState.value.entries.size)
    }

    @Test
    fun openDirectory_browsesThatPath() = runTest {
        val service = FakeSmbMountService().apply {
            listResult = listOf(
                SmbEntry(name = "Seasons", path = "/TV/Seasons", isDirectory = true),
            )
        }
        val viewModel = SmbBrowserViewModel(service)
        viewModel.updateHost("nas")
        viewModel.updateShare("media")
        viewModel.connect()
        advanceUntilIdle()

        viewModel.openDirectory("/TV")
        advanceUntilIdle()

        assertEquals("/TV", viewModel.uiState.value.currentPath)
        assertEquals(listOf("/", "/TV"), service.listCalls)
    }

    @Test
    fun goParent_atRoot_doesNotRequestList() = runTest {
        val service = FakeSmbMountService()
        val viewModel = SmbBrowserViewModel(service)
        viewModel.updateHost("nas")
        viewModel.updateShare("media")
        viewModel.connect()
        advanceUntilIdle()

        service.listCalls.clear()
        viewModel.goParent()
        advanceUntilIdle()

        assertTrue(service.listCalls.isEmpty())
        assertEquals("/", viewModel.uiState.value.currentPath)
    }

    @Test
    fun connect_successThenListFail_keepsConnectedAndShowsError() = runTest {
        val service = FakeSmbMountService().apply {
            listError = IllegalStateException("access denied")
        }
        val viewModel = SmbBrowserViewModel(service)
        viewModel.updateHost("nas")
        viewModel.updateShare("media")

        viewModel.connect()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.connected)
        assertTrue(viewModel.uiState.value.errorMessage?.contains("SMB list failed") == true)
        assertEquals(false, viewModel.uiState.value.loading)
    }

    @Test
    fun openDirectory_normalizesPathBeforeListing() = runTest {
        val service = FakeSmbMountService()
        val viewModel = SmbBrowserViewModel(service)
        viewModel.updateHost("nas")
        viewModel.updateShare("media")
        viewModel.connect()
        advanceUntilIdle()

        viewModel.openDirectory("//TV///Season1//")
        advanceUntilIdle()

        assertEquals("/TV/Season1", viewModel.uiState.value.currentPath)
        assertEquals("/TV/Season1", service.listCalls.last())
    }

    @Test
    fun playbackUrl_forMediaFile_returnsSmbUrl() = runTest {
        val service = FakeSmbMountService()
        val viewModel = SmbBrowserViewModel(service)
        viewModel.updateHost("nas")
        viewModel.updateShare("media")

        val entry = SmbEntry(
            name = "Episode-1.mkv",
            path = "/TV/Episode-1.mkv",
            isDirectory = false,
        )

        val url = viewModel.playbackUrlFor(entry)
        assertEquals("smb://nas/media/TV/Episode-1.mkv", url)
    }

    @Test
    fun playbackUrl_forDirectory_returnsNull() = runTest {
        val service = FakeSmbMountService()
        val viewModel = SmbBrowserViewModel(service)
        viewModel.updateHost("nas")
        viewModel.updateShare("media")

        val entry = SmbEntry(
            name = "TV",
            path = "/TV",
            isDirectory = true,
        )

        val url = viewModel.playbackUrlFor(entry)
        assertEquals(null, url)
    }
}

private class FakeSmbMountService : SmbMountService {
    var listResult: List<SmbEntry> = emptyList()
    var listError: Throwable? = null
    val listCalls = mutableListOf<String>()

    override suspend fun connect(host: String, share: String, username: String, password: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun list(path: String): Result<List<SmbEntry>> {
        listCalls += path
        val error = listError
        return if (error != null) {
            Result.failure(error)
        } else {
            Result.success(listResult)
        }
    }
}
