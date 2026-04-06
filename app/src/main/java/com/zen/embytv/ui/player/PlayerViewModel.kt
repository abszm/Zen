package com.zen.embytv.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zen.embytv.domain.emby.EmbyPlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val mediaTitle: String = "",
    val mediaItemId: String = "",
    val streamUrl: String = "",
    val reportPlaybackProgress: Boolean = true,
    val playbackErrorMessage: String? = null,
)

class PlayerViewModel(
    private val playbackService: EmbyPlaybackService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    fun open(
        mediaItemId: String,
        title: String,
        streamUrl: String,
        reportPlaybackProgress: Boolean = true,
    ) {
        _uiState.value = PlayerUiState(
            mediaTitle = title,
            mediaItemId = mediaItemId,
            streamUrl = streamUrl,
            reportPlaybackProgress = reportPlaybackProgress,
            playbackErrorMessage = null,
        )
    }

    fun startProgressReporting(positionProvider: () -> Long) {
        progressJob?.cancel()
        val current = _uiState.value
        if (current.mediaItemId.isBlank() || !current.reportPlaybackProgress) {
            return
        }
        progressJob = viewModelScope.launch {
            while (true) {
                delay(10_000)
                val pos = positionProvider()
                playbackService.reportProgress(
                    itemId = current.mediaItemId,
                    positionMs = pos,
                )
            }
        }
    }

    fun onPlaybackError(message: String) {
        _uiState.value = _uiState.value.copy(playbackErrorMessage = message)
    }

    fun clearPlaybackError() {
        _uiState.value = _uiState.value.copy(playbackErrorMessage = null)
    }

    fun stopProgressReporting() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressReporting()
    }
}
