package com.sumilauncher

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.EditScreen
import com.sumilauncher.ui.theme.SumiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EditScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyPinnedListShowsEmptyState() {
        composeRule.setContent {
            SumiTheme {
                EditScreen(
                    allApps = emptyList(),
                    pinnedApps = emptyList(),
                    onTogglePinned = { _, _ -> },
                    onMoveUp = {},
                    onMoveDown = {},
                    onDone = {},
                )
            }
        }

        composeRule.onNodeWithTag("edit_empty_state").assertIsDisplayed()
    }

    @Test
    fun reorderButtonsInvokeCallbacks() {
        val reader = LauncherApp("com.example.reader", "ReaderActivity", "Reader")
        val notes = LauncherApp("com.example.notes", "NotesActivity", "Notes")
        var movedUp: LauncherApp? = null
        var movedDown: LauncherApp? = null

        composeRule.setContent {
            SumiTheme {
                EditScreen(
                    allApps = listOf(reader, notes),
                    pinnedApps = listOf(reader, notes),
                    onTogglePinned = { _, _ -> },
                    onMoveUp = { movedUp = it },
                    onMoveDown = { movedDown = it },
                    onDone = {},
                )
            }
        }

        composeRule.onNodeWithTag("move_down_${reader.componentKey}").performClick()
        composeRule.onNodeWithTag("move_up_${notes.componentKey}").performClick()

        assertEquals(reader, movedDown)
        assertEquals(notes, movedUp)
    }
}
