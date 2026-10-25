package com.sumilauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.model.matching
import com.sumilauncher.ui.components.*

@Composable
fun EditScreen(
    allApps: List<LauncherApp>, pinnedApps: List<LauncherApp>, modifier: Modifier = Modifier,
    onTogglePinned: (LauncherApp, Boolean) -> Unit, onMoveUp: (LauncherApp) -> Unit,
    onMoveDown: (LauncherApp) -> Unit, onDone: () -> Unit,
    onSettings: () -> Unit = {}, onAppActions: ((LauncherApp) -> Unit)? = null,
    showIcons: Boolean = false, iconRevision: Int = 0,
) {
    BackHandler(onBack = onDone)
    var adding by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val pinnedKeys = remember(pinnedApps) { pinnedApps.mapTo(mutableSetOf()) { it.componentKey } }
    val available = remember(allApps, query) { allApps.matching(query) }
    Column(modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 12.dp, bottom = 8.dp).testTag("edit_screen")) {
        ScreenHeader("Customize", onDone, trailing = { TextAction("Settings", Modifier.testTag("open_settings"), onClick = onSettings) })
        Row(Modifier.padding(top = 20.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionPill("Home", Modifier.weight(1f).testTag("edit_home_tab"), selected = !adding, onClick = { adding = false })
            ActionPill("Add apps", Modifier.weight(1f).testTag("edit_add_tab"), selected = adding, onClick = { adding = true })
        }
        if (adding) {
            SearchField(query, { query = it }, Modifier.padding(bottom = 12.dp))
            PagedAppList(available, Modifier.weight(1f), showIcons, iconRevision, resetKey = "add:$query", tagPrefix = "edit_all",
                emptyTitle = "No matches.", emptyDescription = "Try another app name.",
                onClick = { onTogglePinned(it, it.componentKey !in pinnedKeys) }, onLongClick = onAppActions,
                trailingWidth = 48.dp, trailingContent = { app ->
                    IconActionButton(if (app.componentKey in pinnedKeys) Icons.Default.Check else Icons.Default.Add,
                        if (app.componentKey in pinnedKeys) "Remove from home" else "Add to home",
                        modifier = Modifier.testTag("toggle_${app.componentKey}"),
                        onClick = { onTogglePinned(app, app.componentKey !in pinnedKeys) })
                })
        } else {
            PagedAppList(pinnedApps, Modifier.weight(1f), showIcons, iconRevision, resetKey = "home", tagPrefix = "edit_pinned",
                emptyTitle = "Make it yours.", emptyDescription = "Add a few everyday apps. You can change their order here.",
                emptyAction = { ActionPill("Choose apps", Modifier.testTag("edit_empty_state"), onClick = { adding = true }) },
                onClick = { onAppActions?.invoke(it) }, onLongClick = onAppActions,
                bottomContent = { app, index ->
                    Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.End) {
                        IconActionButton(Icons.Default.KeyboardArrowUp, "Move ${app.label} up", enabled = index > 0,
                            modifier = Modifier.testTag("move_up_${app.componentKey}"), onClick = { onMoveUp(app) })
                        IconActionButton(Icons.Default.KeyboardArrowDown, "Move ${app.label} down", enabled = index < pinnedApps.lastIndex,
                            modifier = Modifier.testTag("move_down_${app.componentKey}"), onClick = { onMoveDown(app) })
                        IconActionButton(Icons.Default.Close, "Remove ${app.label} from home",
                            modifier = Modifier.testTag("remove_${app.componentKey}"), onClick = { onTogglePinned(app, false) })
                    }
                })
        }
        ActionPill("Done", Modifier.fillMaxWidth().testTag("edit_done"), onClick = onDone)
    }
}
