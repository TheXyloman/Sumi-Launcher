package com.sumilauncher

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.EditScreen
import com.sumilauncher.ui.HiddenAppsScreen
import com.sumilauncher.ui.RenameAppDialog
import com.sumilauncher.ui.theme.SumiTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EditScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val reader = LauncherApp("reader", "Main", "Reader")
    private val notes = LauncherApp("notes", "Main", "Notes")

    @Test fun emptyPinnedListAllowsDoneAndOffersAppSelection() {
        var done = false
        composeRule.setContent { SumiTheme {
            EditScreen(listOf(reader), emptyList(), onTogglePinned = { _, _ -> }, onMoveUp = {}, onMoveDown = {}, onDone = { done = true })
        } }
        composeRule.onNodeWithTag("edit_empty_state").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_done").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertTrue(done) }
        composeRule.onNodeWithTag("edit_add_tab").performClick()
        composeRule.onNodeWithTag("edit_all_${reader.componentKey}").assertIsDisplayed()
    }

    @Test fun reorderAndRemoveButtonsInvokeTheCorrectApp() {
        var up: LauncherApp? = null
        var down: LauncherApp? = null
        var removed: LauncherApp? = null
        composeRule.setContent { SumiTheme {
            EditScreen(listOf(reader, notes), listOf(reader, notes),
                onTogglePinned = { app, enabled -> if (!enabled) removed = app }, onMoveUp = { up = it }, onMoveDown = { down = it }, onDone = {})
        } }
        composeRule.onNodeWithTag("move_up_${reader.componentKey}").assertIsNotEnabled()
        composeRule.onNodeWithTag("move_down_${notes.componentKey}").assertIsNotEnabled()
        composeRule.onNodeWithTag("move_down_${reader.componentKey}").performClick()
        composeRule.onNodeWithTag("move_up_${notes.componentKey}").performClick()
        composeRule.onNodeWithTag("remove_${reader.componentKey}").performClick()
        composeRule.runOnIdle { assertEquals(reader, down); assertEquals(notes, up); assertEquals(reader, removed) }
    }

    @Test fun hiddenAppHasAnExplicitRestoreAction() {
        var restored: LauncherApp? = null
        composeRule.setContent { SumiTheme {
            HiddenAppsScreen(listOf(reader), false, 0, { restored = it }, {})
        } }
        composeRule.onNodeWithTag("restore_${reader.componentKey}").performClick()
        composeRule.runOnIdle { assertEquals(reader, restored) }
    }

    @Test fun blankRenameCanRestoreOriginalName() {
        var saved: String? = null
        composeRule.setContent { SumiTheme {
            RenameAppDialog(reader.copy(label = "Read"), "Read", onDismiss = {}, onSave = { saved = it })
        } }
        composeRule.onNodeWithTag("rename_input").performTextClearance()
        composeRule.onNodeWithTag("save_name").performClick()
        composeRule.runOnIdle { assertEquals("", saved) }
    }
}
