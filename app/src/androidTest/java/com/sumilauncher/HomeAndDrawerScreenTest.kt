package com.sumilauncher

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.*
import com.sumilauncher.ui.theme.SumiTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class HomeAndDrawerScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val apps = (1..30).map { LauncherApp("app$it", "Main", "App ${it.toString().padStart(2, '0')}") }

    @Test fun homeLongPressAndHorizontalSwipeNeverLaunchAnAppAndSwipeFiresOnce() {
        var launches = 0
        var opens = 0
        var actions = 0
        composeRule.setContent { SumiTheme {
            HomeScreen(apps, onLaunchApp = { launches++ }, onOpenDrawer = { opens++ },
                onOpenNotifications = {}, onEdit = {}, onAppActions = { actions++ })
        } }
        composeRule.onNodeWithTag("pinned_app_${apps.first().componentKey}").performTouchInput { longClick() }
        composeRule.onNodeWithTag("paged_apps").performTouchInput { swipeLeft() }
        composeRule.runOnIdle { assertEquals(0, launches); assertEquals(1, opens); assertEquals(1, actions) }
    }

    @Test fun pageSwipesAdvanceOnePageAndDontOpenNotificationsOrLaunchApps() {
        var launches = 0
        var notifications = 0
        composeRule.setContent { SumiTheme {
            HomeScreen(apps, onLaunchApp = { launches++ }, onOpenDrawer = {}, onOpenNotifications = { notifications++ }, onEdit = {})
        } }
        composeRule.onNodeWithTag("page_indicator").assertTextContains("1 /", substring = true)
        composeRule.onNodeWithTag("paged_apps").performTouchInput { swipeUp() }
        composeRule.onNodeWithTag("page_indicator").assertTextContains("2 /", substring = true)
        composeRule.onNodeWithTag("paged_apps").performTouchInput { swipeDown() }
        composeRule.onNodeWithTag("page_indicator").assertTextContains("1 /", substring = true)
        composeRule.runOnIdle { assertEquals(0, launches); assertEquals(0, notifications) }
        composeRule.onNodeWithTag("date_time_header").performTouchInput { swipeDown() }
        composeRule.runOnIdle { assertEquals(1, notifications) }
    }

    @Test fun emptyHomeRemainsUsableWithVisibleNavigation() {
        var edits = 0
        var library = 0
        composeRule.setContent { SumiTheme {
            HomeScreen(emptyList(), onLaunchApp = {}, onOpenDrawer = { library++ }, onOpenNotifications = {}, onEdit = { edits++ })
        } }
        composeRule.onNodeWithTag("add_apps").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("open_drawer").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("open_customize").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(1, edits); assertEquals(1, library) }
    }

    @Test fun enlargedTextKeepsHomeControlsVisibleAndRowsWithinList() {
        composeRule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) { SumiTheme {
                Box(Modifier.size(320.dp, 700.dp)) {
                    HomeScreen(apps, onLaunchApp = {}, onOpenDrawer = {}, onOpenNotifications = {}, onEdit = {})
                }
            } }
        }
        composeRule.onNodeWithTag("open_drawer").assertIsDisplayed()
        composeRule.onNodeWithTag("open_customize").assertIsDisplayed()
        val list = composeRule.onNodeWithTag("paged_apps").fetchSemanticsNode().boundsInRoot
        val row = composeRule.onNodeWithTag("pinned_app_${apps.first().componentKey}").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(row.top >= list.top && row.bottom <= list.bottom)
        composeRule.onNodeWithTag("next_page").performClick()
        composeRule.onNodeWithTag("page_indicator").assertTextContains("2 /", substring = true)
    }

    @Test fun drawerSearchStartsUnfocusedMatchesBothNamesAndResetsPage() {
        val renamed = apps.first().copy(label = "Reading", originalLabel = "Books")
        composeRule.setContent { SumiTheme {
            DrawerScreen(listOf(renamed) + apps.drop(1), onLaunchApp = {}, onClose = {})
        } }
        composeRule.onNodeWithTag("search_apps").assertIsNotFocused()
        composeRule.onNodeWithTag("next_page").performClick()
        composeRule.onNodeWithTag("page_indicator").assertTextContains("2 /", substring = true)
        composeRule.onNodeWithTag("search_apps").performTextInput("BOOKS")
        composeRule.onNodeWithTag("drawer_app_${renamed.componentKey}").assertIsDisplayed()
        composeRule.onNodeWithTag("page_indicator").assertTextEquals("1 / 1")
        composeRule.onNodeWithTag("search_apps").performTextReplacement("reading")
        composeRule.onNodeWithTag("drawer_app_${renamed.componentKey}").assertIsDisplayed()
        composeRule.onNodeWithTag("search_apps").performTextReplacement("no matches here")
        composeRule.onNodeWithText("No matches.").assertIsDisplayed()
    }

    @Test fun drawerSwipeRightRequestsCloseOnceAndDoesNotLaunch() {
        var closed = 0
        var launched = 0
        composeRule.setContent { SumiTheme {
            DrawerScreen(apps, onLaunchApp = { launched++ }, onClose = { closed++ })
        } }
        composeRule.onNodeWithTag("paged_apps").performTouchInput { swipeRight() }
        composeRule.runOnIdle { assertEquals(1, closed); assertEquals(0, launched) }
    }
}
