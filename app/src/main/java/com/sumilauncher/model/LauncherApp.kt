package com.sumilauncher.model

data class LauncherApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val originalLabel: String = label,
    val available: Boolean = true,
) {
    val componentKey: String get() = buildComponentKey(packageName, activityName)
}

fun buildComponentKey(packageName: String, activityName: String): String = "$packageName/$activityName"

fun List<LauncherApp>.matching(query: String): List<LauncherApp> {
    val term = query.trim()
    return if (term.isEmpty()) this else filter {
        it.label.contains(term, ignoreCase = true) || it.originalLabel.contains(term, ignoreCase = true)
    }
}
