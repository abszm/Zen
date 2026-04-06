package com.zen.embytv.ui.smb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zen.embytv.domain.local.SmbEntry
import com.zen.embytv.domain.local.SmbMountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SmbBrowserUiState(
    val host: String = "",
    val share: String = "",
    val username: String = "",
    val password: String = "",
    val connected: Boolean = false,
    val currentPath: String = "/",
    val loading: Boolean = false,
    val entries: List<SmbEntry> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class SmbBrowserViewModel(
    private val smbMountService: SmbMountService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SmbBrowserUiState())
    val uiState: StateFlow<SmbBrowserUiState> = _uiState.asStateFlow()

    fun updateHost(value: String) {
        _uiState.value = _uiState.value.copy(host = value)
    }

    fun updateShare(value: String) {
        _uiState.value = _uiState.value.copy(share = value)
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun connect() {
        val state = _uiState.value
        if (state.host.isBlank() || state.share.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please fill host and share.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, errorMessage = null, successMessage = null)
            smbMountService.connect(
                host = state.host.trim(),
                share = state.share.trim(),
                username = state.username.trim(),
                password = state.password,
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    connected = true,
                    loading = false,
                    currentPath = "/",
                    successMessage = "SMB connected",
                )
                openDirectory("/")
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    connected = false,
                    errorMessage = "SMB connect failed: ${it.message ?: "Unknown error"}",
                )
            }
        }
    }

    fun openDirectory(path: String) {
        if (!_uiState.value.connected) {
            return
        }
        val normalized = normalizePath(path)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, errorMessage = null, successMessage = null)
            smbMountService.list(normalized).onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    currentPath = normalized,
                    entries = list,
                    successMessage = "Loaded ${list.size} entries",
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = "SMB list failed: ${it.message ?: "Unknown error"}",
                )
            }
        }
    }

    fun goParent() {
        val current = _uiState.value.currentPath
        if (current == "/") {
            return
        }
        val segments = current.trim('/').split('/').filter { it.isNotBlank() }
        val parent = if (segments.size <= 1) "/" else "/" + segments.dropLast(1).joinToString("/")
        openDirectory(parent)
    }

    fun playbackUrlFor(entry: SmbEntry): String? {
        if (entry.isDirectory) {
            return null
        }
        if (!isPlayableMedia(entry.name)) {
            return null
        }

        val state = _uiState.value
        if (state.host.isBlank() || state.share.isBlank()) {
            return null
        }

        val relPath = entry.path.trim().trim('/').replace(Regex("/+"), "/")
        return if (relPath.isBlank()) {
            null
        } else {
            "smb://${state.host.trim()}/${state.share.trim().trim('/')}/$relPath"
        }
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    private fun isPlayableMedia(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".mkv") ||
            lower.endsWith(".mp4") ||
            lower.endsWith(".avi") ||
            lower.endsWith(".mov") ||
            lower.endsWith(".ts") ||
            lower.endsWith(".m2ts") ||
            lower.endsWith(".webm")
    }

    private fun normalizePath(path: String): String {
        if (path.isBlank()) return "/"
        val compact = "/" + path.trim('/').replace(Regex("/+"), "/")
        return if (compact == "/") compact else compact.trimEnd('/')
    }
}
