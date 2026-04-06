package com.zen.embytv.ui.smb

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zen.embytv.ui.theme.AppColors

@Composable
fun SmbBrowserScreen(
    viewModel: SmbBrowserViewModel,
    onPlayRequest: (title: String, streamUrl: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                text = "SMB Browser",
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary,
            )
            OutlinedTextField(
                value = state.host,
                onValueChange = viewModel::updateHost,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Host") },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.share,
                onValueChange = viewModel::updateShare,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Share") },
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
                Button(onClick = viewModel::connect) {
                    Text("Connect SMB")
                }
                Button(onClick = viewModel::goParent, enabled = state.connected && state.currentPath != "/") {
                    Text("Up")
                }
                Button(onClick = { viewModel.openDirectory(state.currentPath) }, enabled = state.connected) {
                    Text("Refresh")
                }
            }

            Text(
                text = if (state.loading) "Loading..." else if (state.connected) "Connected" else "Not connected",
                color = AppColors.TextSecondary,
            )
            state.successMessage?.let { Text(text = it, color = AppColors.Focus) }
            state.errorMessage?.let { Text(text = it, color = Color(0xFFFFA6A6)) }
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
                text = "Current Path: ${state.currentPath}",
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
            )

            if (!state.connected) {
                Text(
                    text = "Connect SMB first to browse files.",
                    color = AppColors.TextSecondary,
                )
            } else if (state.entries.isEmpty()) {
                Text(
                    text = "This folder is empty.",
                    color = AppColors.TextSecondary,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.entries) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.Background, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = entry.name, color = AppColors.TextPrimary)
                                Text(
                                    text = if (entry.isDirectory) "Directory" else "File",
                                    color = AppColors.TextSecondary,
                                )
                            }
                            if (entry.isDirectory) {
                                Button(onClick = { viewModel.openDirectory(entry.path) }) {
                                    Text("Open")
                                }
                            } else {
                                Button(onClick = {
                                    val streamUrl = viewModel.playbackUrlFor(entry)
                                    if (!streamUrl.isNullOrBlank()) {
                                        onPlayRequest(entry.name, streamUrl)
                                    } else {
                                        viewModel.showError("This file type is not supported for SMB playback")
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
}
