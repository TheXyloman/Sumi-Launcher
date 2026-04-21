package com.sumilauncher.model

data class LauncherApp(
    val packageName: String,
    val activityName: String,
    val label: String,
) {
    val componentKey: String
        get() = buildComponentKey(packageName, activityName)
}

fun buildComponentKey(packageName: String, activityName: String): String {
    return "$packageName/$activityName"
}
