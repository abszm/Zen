package com.zen.embytv.ui.smb

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zen.embytv.domain.local.SmbEntry
import com.zen.embytv.domain.local.SmbMountService
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmbBrowserScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun smbBrowserScreen_showsBasicSections() {
        val viewModel = SmbBrowserViewModel(
            smbMountService = object : SmbMountService {
                override suspend fun connect(
                    host: String,
                    share: String,
                    username: String,
                    password: String,
                ): Result<Unit> = Result.success(Unit)

                override suspend fun list(path: String): Result<List<SmbEntry>> = Result.success(emptyList())
            },
        )

        composeRule.setContent {
            SmbBrowserScreen(
                viewModel = viewModel,
                onPlayRequest = { _, _ -> },
            )
        }

        composeRule.onNodeWithText("SMB Browser").assertExists()
        composeRule.onNodeWithText("Host").assertExists()
        composeRule.onNodeWithText("Connect SMB").assertExists()
        composeRule.onNodeWithText("Current Path: /").assertExists()
    }
}
