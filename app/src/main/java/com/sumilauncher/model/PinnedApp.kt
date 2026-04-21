package com.sumilauncher.model

data class PinnedApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val sortOrder: Int,
) {
    val componentKey: String
        get() = buildComponentKey(packageName, activityName)
}
