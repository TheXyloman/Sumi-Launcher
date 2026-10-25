package com.sumilauncher

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.sumilauncher.platform.LauncherSetupState
import com.sumilauncher.ui.LauncherUiState
import com.sumilauncher.ui.OnboardingScreen
import com.sumilauncher.ui.theme.SumiTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test fun continueNeedsDefaultHomeButNotAccessibility() {
        var completed = false
        composeRule.setContent { SumiTheme {
            OnboardingScreen(LauncherUiState(isLoaded = true, setupState = LauncherSetupState(true, false)),
                onRefresh = {}, onContinue = { completed = true })
        } }
        composeRule.onNodeWithTag("check_default_launcher").assertExists()
        composeRule.onNodeWithTag("check_accessibility").assertExists()
        composeRule.onNodeWithTag("continue_setup").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertTrue(completed) }
    }

    @Test fun defaultHomeStillNeedsToBeChosen() {
        composeRule.setContent { SumiTheme {
            OnboardingScreen(LauncherUiState(setupState = LauncherSetupState(false, false)), onRefresh = {}, onContinue = {})
        } }
        composeRule.onNodeWithTag("continue_setup").assertIsNotEnabled()
    }
}
