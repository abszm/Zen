package com.zen.embytv.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zen.embytv.data.repository.EmbyRepository
import com.zen.embytv.domain.emby.EmbyAuthService
import com.zen.embytv.domain.emby.EmbyLibraryService
import com.zen.embytv.domain.media.MediaItem
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LibraryUiState(
    val serverUrl: String = "http://192.168.1.100:8096",
    val username: String = "",
    val password: String = "",
    val searchQuery: String = "",
    val page: Int = 1,
    val pageSize: Int = 30,
    val totalCount: Int = 0,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class LibraryViewModel(
    private val authService: EmbyAuthService,
    private val libraryService: EmbyLibraryService,
    private val repository: EmbyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    val session = repository.session.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val mediaItems = repository.mediaItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun updateServerUrl(value: String) {
        _uiState.value = _uiState.value.copy(serverUrl = value)
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun updateSearchQuery(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
    }

    fun login() {
        val current = _uiState.value
        if (current.serverUrl.isBlank() || current.username.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Please fill server, username and password.")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(loading = true, errorMessage = null, successMessage = null)
            authService.login(
                serverUrl = current.serverUrl.trim(),
                username = current.username.trim(),
                password = current.password,
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    page = 1,
                    successMessage = "Connected as ${it.userName}",
                )
                syncLibrary()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = "Login failed: ${toReadableMessage(it)}",
                )
            }
        }
    }

    fun syncLibrary(forcePage: Int? = null) {
        viewModelScope.launch {
            val current = _uiState.value
            val targetPage = forcePage ?: current.page
            _uiState.value = current.copy(loading = true, errorMessage = null, successMessage = null)

            libraryService.syncLibrary(
                page = targetPage,
                pageSize = current.pageSize,
                query = current.searchQuery.trim(),
            ).onSuccess { pageResult ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    page = pageResult.page,
                    totalCount = pageResult.totalCount,
                    successMessage = "Fetched ${pageResult.items.size} items",
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = "Library sync failed: ${toReadableMessage(it)}",
                )
            }
        }
    }

    fun applySearch() {
        syncLibrary(forcePage = 1)
    }

    fun loadNextPage() {
        val current = _uiState.value
        val totalPages = totalPages()
        if (current.page >= totalPages) return
        syncLibrary(forcePage = current.page + 1)
    }

    fun loadPreviousPage() {
        val current = _uiState.value
        if (current.page <= 1) return
        syncLibrary(forcePage = current.page - 1)
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
            _uiState.value = _uiState.value.copy(successMessage = "Session cleared")
        }
    }

    fun playbackUrlFor(item: MediaItem): String? {
        return repository.buildPlaybackUrl(item.id)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun totalPages(): Int {
        val state = _uiState.value
        if (state.totalCount <= 0) return 1
        return ((state.totalCount - 1) / state.pageSize) + 1
    }

    private fun toReadableMessage(error: Throwable): String {
        return when (error) {
            is IOException -> "Cannot reach Emby server"
            is HttpException -> when (error.code()) {
                401 -> "Invalid username or password"
                403 -> "Access denied by server"
                404 -> "Emby endpoint not found, check server URL"
                else -> "HTTP ${error.code()}"
            }

            else -> error.message ?: "Unknown error"
        }
    }
}
