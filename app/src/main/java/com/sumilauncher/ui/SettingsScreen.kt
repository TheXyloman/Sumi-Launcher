package com.sumilauncher.ui

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.platform.startActivitySafely
import com.sumilauncher.ui.components.*

@Composable
fun SettingsScreen(state: LauncherUiState, onBack: () -> Unit, onShowIcons: (Boolean) -> Unit, onHiddenApps: () -> Unit,
    onRefresh: () -> Unit, onRetryApps: () -> Unit, onError: (String) -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val requestHome = rememberHomeRoleRequest(onRefresh, onError)
    fun openSettings(action: String) { if (!context.startActivitySafely(Intent(action))) onError("These settings could not be opened on this device.") }
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 12.dp).testTag("settings_screen")) {
        ScreenHeader("Settings", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionLabel("Appearance")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("App icons", style = MaterialTheme.typography.bodyLarge)
                    Text("Grayscale. Quiet by design.", style = MaterialTheme.typography.bodyMedium)
                }
                ActionPill(if (state.showIcons) "On" else "Off", Modifier.testTag("show_icons"), selected = state.showIcons, onClick = { onShowIcons(!state.showIcons) })
            }
            TextAction("Hidden apps · ${state.hiddenApps.size}", Modifier.fillMaxWidth().testTag("open_hidden"), onClick = onHiddenApps)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SectionLabel("Your device")
            Text("Default home", style = MaterialTheme.typography.headlineSmall)
            Text(if (state.setupState.isDefaultLauncher) "Sumi is your default home." else "Another app is currently your home.", style = MaterialTheme.typography.bodyMedium)
            TextAction("Choose home app", onClick = requestHome)
            Text("Notification shortcut", style = MaterialTheme.typography.headlineSmall)
            Text(if (state.setupState.isAccessibilityEnabled) "Enabled. Swipe down on the clock to open notifications."
                else "Optional. Allow accessibility access to open notifications with a downward swipe on the clock.", style = MaterialTheme.typography.bodyMedium)
            TextAction(if (state.setupState.isAccessibilityEnabled) "Manage access" else "Set up shortcut", Modifier.testTag("notification_setup"), onClick = { openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS) })
            TextAction("Android settings ↗", onClick = { openSettings(Settings.ACTION_SETTINGS) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SectionLabel("Made for e‑ink")
            Text("Keep it crisp", style = MaterialTheme.typography.headlineSmall)
            Text("On BOOX, open the E Ink Center while using Sumi, then App Optimization. Adjust the per-app refresh and text settings to suit your screen. Available options vary by firmware.", style = MaterialTheme.typography.bodyMedium)
            Text("Sumi uses still pages and a clock that updates once a minute while home is visible. Your device controls the display refresh.", style = MaterialTheme.typography.bodyMedium)
            if (state.loadError != null) Text(state.loadError, style = MaterialTheme.typography.bodyMedium)
            TextAction("Reload app list", onClick = onRetryApps)
            SectionLabel("Sumi · 2.0")
        }
    }
}

@Composable
fun HiddenAppsScreen(apps: List<LauncherApp>, showIcons: Boolean, iconRevision: Int, onRestore: (LauncherApp) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 12.dp, bottom = 8.dp).testTag("hidden_screen")) {
        ScreenHeader("Hidden apps", onBack)
        Text("Out of sight, still yours. Restore an app to show it in the library again.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 20.dp))
        PagedAppList(apps, Modifier.weight(1f), showIcons, iconRevision, tagPrefix = "hidden_app",
            emptyTitle = "Nothing hidden.", emptyDescription = "Apps you hide can always be restored here.",
            onClick = onRestore, trailingWidth = 48.dp, trailingContent = { app ->
                IconActionButton(Icons.Default.Add, "Restore ${app.label}", onClick = { onRestore(app) }, modifier = Modifier.testTag("restore_${app.componentKey}"))
            })
    }
}
