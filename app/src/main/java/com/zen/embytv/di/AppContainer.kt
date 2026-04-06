package com.zen.embytv.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.zen.embytv.data.local.JcifsSmbMountService
import com.zen.embytv.data.repository.EmbyRepository
import com.zen.embytv.data.repository.EmbyRepositoryImpl
import com.zen.embytv.domain.emby.EmbyAuthService
import com.zen.embytv.domain.emby.EmbyAuthServiceImpl
import com.zen.embytv.domain.emby.EmbyLibraryService
import com.zen.embytv.domain.emby.EmbyLibraryServiceImpl
import com.zen.embytv.domain.emby.EmbyPlaybackService
import com.zen.embytv.domain.emby.EmbyPlaybackServiceImpl
import com.zen.embytv.domain.local.SmbMountService
import com.zen.embytv.ui.library.LibraryViewModel
import com.zen.embytv.ui.player.PlayerViewModel
import com.zen.embytv.ui.smb.SmbBrowserViewModel

object AppContainer {
    val embyRepository: EmbyRepository by lazy { EmbyRepositoryImpl() }

    val embyAuthService: EmbyAuthService by lazy { EmbyAuthServiceImpl(embyRepository) }
    val embyLibraryService: EmbyLibraryService by lazy { EmbyLibraryServiceImpl(embyRepository) }
    val embyPlaybackService: EmbyPlaybackService by lazy { EmbyPlaybackServiceImpl(embyRepository) }
    val smbMountService: SmbMountService by lazy { JcifsSmbMountService() }

    val libraryViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return LibraryViewModel(
                        authService = embyAuthService,
                        libraryService = embyLibraryService,
                        repository = embyRepository,
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    val playerViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PlayerViewModel(
                        playbackService = embyPlaybackService,
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    val smbBrowserViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(SmbBrowserViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SmbBrowserViewModel(
                        smbMountService = smbMountService,
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
}
