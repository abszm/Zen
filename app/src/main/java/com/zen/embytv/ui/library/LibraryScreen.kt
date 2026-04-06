package com.zen.embytv.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zen.embytv.domain.media.MediaItem
import com.zen.embytv.ui.theme.AppColors

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onPlayRequest: (mediaItem: MediaItem, streamUrl: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .background(AppColors.Surface, RoundedCornerShape(24.dp))
                .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Emby Login",
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary,
            )
            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = viewModel::updateServerUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Server URL") },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::updateUsername,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = viewModel::login) { Text("Connect") }
                Button(onClick = viewModel::syncLibrary) { Text("Sync") }
                Button(onClick = viewModel::logout) { Text("Logout") }
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search") },
                singleLine = true,
            )
            Button(onClick = viewModel::applySearch) {
                Text("Apply Filter")
            }

            Text(
                text = if (state.loading) "Loading..." else session?.let { "Connected: ${it.userName}" } ?: "Not connected",
                color = AppColors.TextSecondary,
            )
            state.successMessage?.let { msg ->
                Text(text = msg, color = AppColors.Focus)
            }
            state.errorMessage?.let { msg ->
                Text(text = msg, color = androidx.compose.ui.graphics.Color(0xFFFFA6A6))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .background(AppColors.Surface, RoundedCornerShape(24.dp))
                .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Media Library (${mediaItems.size})",
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = viewModel::loadPreviousPage,
                    enabled = state.page > 1,
                ) {
                    Text("Prev")
                }
                Button(
                    onClick = viewModel::loadNextPage,
                    enabled = state.page < viewModel.totalPages(),
                ) {
                    Text("Next")
                }
                Text(
                    text = "Page ${state.page}/${viewModel.totalPages()} · Total ${state.totalCount}",
                    color = AppColors.TextSecondary,
                )
            }

            if (mediaItems.isEmpty()) {
                Text(
                    text = "No media loaded. Connect and Sync first.",
                    color = AppColors.TextSecondary,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(mediaItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.Background, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, color = AppColors.TextPrimary)
                                Text(text = item.type, color = AppColors.TextSecondary)
                            }
                            Button(onClick = {
                                val streamUrl = viewModel.playbackUrlFor(item)
                                if (!streamUrl.isNullOrBlank()) {
                                    onPlayRequest(item, streamUrl)
                                } else {
                                    viewModel.showError("Cannot build playback URL, please reconnect Emby")
                                }
                            }) {
                                Text("Play")
                            }
                        }
                    }
                }
            }
        }
    }
}
