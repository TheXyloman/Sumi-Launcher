package com.sumilauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
fun DrawerScreen(
    apps: List<LauncherApp>, modifier: Modifier = Modifier,
    onLaunchApp: (LauncherApp) -> Unit, onClose: () -> Unit,
    onAppActions: ((LauncherApp) -> Unit)? = null,
    showIcons: Boolean = false, iconRevision: Int = 0,
) {
    BackHandler(onBack = onClose)
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(apps, query) { apps.matching(query) }
    Column(modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 12.dp, bottom = 8.dp).testTag("drawer_screen")) {
        ScreenHeader("All apps", onClose)
        SearchField(query, { query = it }, Modifier.padding(top = 20.dp, bottom = 12.dp))
        SectionLabel(if (query.isEmpty()) "${apps.size} apps · A to Z" else "${results.size} results")
        PagedAppList(results, Modifier.weight(1f), showIcons, iconRevision, MaterialTheme.typography.bodyLarge,
            resetKey = query, tagPrefix = "drawer_app", emptyTitle = if (query.isEmpty()) "Your library is quiet." else "No matches.",
            emptyDescription = if (query.isEmpty()) "Installed apps appear here. Hidden apps can be restored in Settings." else "Try an original app name or a name you’ve given it.",
            onClick = onLaunchApp, onLongClick = onAppActions, onSwipeRight = onClose)
    }
}
