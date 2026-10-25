package com.sumilauncher.data

import com.sumilauncher.model.LauncherApp

data class AppCatalog(
    val apps: List<LauncherApp> = emptyList(),
    val loaded: Boolean = false,
    val revision: Int = 0,
    val error: String? = null,
)

data class DisplayApps(val all: List<LauncherApp>, val pinned: List<LauncherApp>, val hidden: List<LauncherApp>)

fun buildDisplayApps(apps: List<LauncherApp>, preferences: LauncherPreferences): DisplayApps {
    val byKey = apps.associateBy { it.componentKey }
    fun named(app: LauncherApp) = app.copy(label = preferences.customNames[app.componentKey] ?: app.originalLabel)
    val all = apps.filterNot { it.componentKey in preferences.hiddenComponents }.map(::named).sortAlphabetically()
    val pinned = preferences.pinnedApps.sortedBy { it.sortOrder }.filterNot { it.componentKey in preferences.hiddenComponents }.map { pin ->
        named(byKey[pin.componentKey] ?: LauncherApp(pin.packageName, pin.activityName, pin.label, available = false))
    }
    val hidden = preferences.hiddenComponents.map { key ->
        named(byKey[key] ?: LauncherApp(key.substringBefore('/'), key.substringAfter('/'),
            preferences.hiddenLabels[key] ?: key.substringBefore('/'), available = false))
    }.sortAlphabetically()
    return DisplayApps(all, pinned, hidden)
}
