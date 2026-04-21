package com.sumilauncher

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.DrawerScreen
import com.sumilauncher.ui.HomeScreen
import com.sumilauncher.ui.theme.SumiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeAndDrawerScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeRendersPinnedApps_andSupportsLongPressAndSwipeLeft() {
        val apps = listOf(
            LauncherApp("com.example.reader", "ReaderActivity", "Reader"),
            LauncherApp("com.example.notes", "NotesActivity", "Notes"),
        )
        var launchedLabel: String? = null
        var openedDrawer = false
        var editOpened = false

        composeRule.setContent {
            SumiTheme {
                HomeScreen(
                    pinnedApps = apps,
                    onLaunchApp = { launchedLabel = it.label },
                    onOpenDrawer = { openedDrawer = true },
                    onOpenNotifications = {},
                    onEdit = { editOpened = true },
                )
            }
        }

        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("pinned_app_${apps.first().componentKey}").assertIsDisplayed()
        composeRule.onNodeWithTag("pinned_app_${apps.first().componentKey}").performTouchInput {
            longClick()
        }
        composeRule.onNodeWithTag("home_screen").performTouchInput {
            swipeLeft()
        }

        assertTrue(editOpened)
        assertTrue(openedDrawer)
        assertEquals(null, launchedLabel)
    }

    @Test
    fun drawerSwipeRightRequestsClose() {
        val apps = listOf(
            LauncherApp("com.example.reader", "ReaderActivity", "Reader"),
        )
        var closed = false

        composeRule.setContent {
            SumiTheme {
                DrawerScreen(
                    apps = apps,
                    onLaunchApp = {},
                    onClose = { closed = true },
                )
            }
        }

        composeRule.onNodeWithTag("drawer_screen").performTouchInput {
            swipeRight()
        }

        assertTrue(closed)
    }
}
