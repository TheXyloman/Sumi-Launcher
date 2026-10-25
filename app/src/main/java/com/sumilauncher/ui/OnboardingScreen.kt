package com.sumilauncher.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sumilauncher.platform.startActivitySafely
import com.sumilauncher.ui.components.*

@Composable
fun OnboardingScreen(uiState: LauncherUiState, modifier: Modifier = Modifier, onRefresh: () -> Unit, onContinue: () -> Unit, onError: (String) -> Unit = {}) {
    val context = LocalContext.current
    val requestHome = rememberHomeRoleRequest(onRefresh, onError)
    Column(modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 28.dp, bottom = 16.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            SectionLabel("SUMI / A quieter home")
            Text("Less noise.\nMore space.", style = MaterialTheme.typography.headlineLarge)
            Text("Your everyday apps, thoughtfully within reach. Made for the calm of an e‑ink screen.", style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Text("A simple rhythm", style = MaterialTheme.typography.headlineSmall)
            Text("Tap an app to open it. Swipe left for all apps, and up or down to turn pages. Long-press an app to make it yours. Buttons are always there, too.", style = MaterialTheme.typography.bodyMedium)
            Column(Modifier.testTag("check_default_launcher"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Make Sumi your home", style = MaterialTheme.typography.headlineSmall)
                Text(if (uiState.setupState.isDefaultLauncher) "Sumi is your default launcher." else "Choose Sumi when Android asks for your home app.", style = MaterialTheme.typography.bodyMedium)
                if (!uiState.setupState.isDefaultLauncher) ActionPill("Set as home", onClick = requestHome)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Column(Modifier.testTag("check_accessibility"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notifications, if you like", style = MaterialTheme.typography.headlineSmall)
                Text("Optional: swipe down on the clock to open notifications. Accessibility access is used only for this shortcut. You can set it up later.", style = MaterialTheme.typography.bodyMedium)
                TextAction(if (uiState.setupState.isAccessibilityEnabled) "Shortcut enabled" else "Set up shortcut", onClick = {
                    if (!context.startActivitySafely(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) onError("Accessibility settings could not be opened.")
                })
            }
            Spacer(Modifier.height(12.dp))
        }
        ActionPill("Enter Sumi", Modifier.fillMaxWidth().testTag("continue_setup"), enabled = uiState.setupState.isDefaultLauncher, onClick = onContinue)
    }
}
