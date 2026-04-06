package com.zen.embytv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zen.embytv.di.AppContainer
import com.zen.embytv.ui.library.LibraryScreen
import com.zen.embytv.ui.library.LibraryViewModel
import com.zen.embytv.ui.navigation.Screen
import com.zen.embytv.ui.player.PlayerScreen
import com.zen.embytv.ui.player.PlayerViewModel
import com.zen.embytv.ui.smb.SmbBrowserScreen
import com.zen.embytv.ui.smb.SmbBrowserViewModel
import com.zen.embytv.ui.theme.AppColors

@Composable
fun EmbyTvApp() {
    val navController = rememberNavController()
    val sidebarFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }
    val libraryViewModel: LibraryViewModel = viewModel(factory = AppContainer.libraryViewModelFactory)
    val playerViewModel: PlayerViewModel = viewModel(factory = AppContainer.playerViewModelFactory)
    val smbBrowserViewModel: SmbBrowserViewModel = viewModel(factory = AppContainer.smbBrowserViewModelFactory)

    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var sidebarVisible by remember { mutableStateOf(true) }

    LaunchedEffect(sidebarVisible) {
        if (sidebarVisible) {
            sidebarFocusRequester.requestFocus()
        } else {
            contentFocusRequester.requestFocus()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                when (event.key) {
                    Key.DirectionLeft -> {
                        if (!sidebarVisible) {
                            sidebarVisible = true
                            true
                        } else {
                            false
                        }
                    }

                    Key.DirectionRight -> {
                        if (sidebarVisible) {
                            sidebarVisible = false
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AppColors.Background, AppColors.BackgroundAccent),
                    ),
                ),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = sidebarVisible,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                ) {
                    Sidebar(
                        modifier = Modifier.fillMaxHeight(),
                        selectedScreen = currentScreen,
                        focusRequester = sidebarFocusRequester,
                        onSelected = { screen ->
                            currentScreen = screen
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            sidebarVisible = false
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .focusRequester(contentFocusRequester),
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                    ) {
                        composable(Screen.Home.route) {
                            PlaceholderScreen(
                                title = "Home",
                                description = "Compose for TV shell is ready.",
                            )
                        }
                        composable(Screen.Library.route) {
                            LibraryScreen(
                                viewModel = libraryViewModel,
                                onPlayRequest = { item, streamUrl ->
                                    playerViewModel.open(
                                        mediaItemId = item.id,
                                        title = item.name,
                                        streamUrl = streamUrl,
                                    )
                                    currentScreen = Screen.Player
                                    navController.navigate(Screen.Player.route) {
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                        composable(Screen.Player.route) {
                            PlayerScreen(viewModel = playerViewModel)
                        }
                        composable(Screen.Settings.route) {
                            SmbBrowserScreen(
                                viewModel = smbBrowserViewModel,
                                onPlayRequest = { title, streamUrl ->
                                    playerViewModel.open(
                                        mediaItemId = "smb:$streamUrl",
                                        title = title,
                                        streamUrl = streamUrl,
                                        reportPlaybackProgress = false,
                                    )
                                    currentScreen = Screen.Player
                                    navController.navigate(Screen.Player.route) {
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    focusRequester: FocusRequester,
    onSelected: (Screen) -> Unit,
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .padding(20.dp)
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .background(AppColors.Surface, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Emby TV",
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Screen.entries.forEachIndexed { index, item ->
            Button(
                onClick = { onSelected(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index == 0) Modifier.focusRequester(focusRequester) else Modifier),
            ) {
                Text(
                    text = if (item == selectedScreen) "${item.title} *" else item.title,
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface, RoundedCornerShape(24.dp))
            .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = AppColors.TextPrimary,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
        )
        Text(
            text = "D-Pad Left: open sidebar, D-Pad Right: focus content.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Focus,
        )
    }
}
