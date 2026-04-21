package com.sumilauncher

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.sumilauncher.platform.LauncherSetupState
import com.sumilauncher.ui.LauncherUiState
import com.sumilauncher.ui.OnboardingScreen
import com.sumilauncher.ui.theme.SumiTheme
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onboardingShowsChecklistAndDisabledContinueUntilSetupIsComplete() {
        composeRule.setContent {
            SumiTheme {
                OnboardingScreen(
                    uiState = LauncherUiState(
                        isLoaded = true,
                        onboardingComplete = false,
                        setupState = LauncherSetupState(
                            isDefaultLauncher = false,
                            isAccessibilityEnabled = false,
                        ),
                    ),
                    onRefresh = {},
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithTag("check_default_launcher").assertIsDisplayed()
        composeRule.onNodeWithTag("check_accessibility").assertIsDisplayed()
        composeRule.onNodeWithTag("continue_setup").assertIsNotEnabled()
    }
}
