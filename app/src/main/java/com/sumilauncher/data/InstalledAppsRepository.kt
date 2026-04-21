package com.sumilauncher.data

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import com.sumilauncher.model.LauncherApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import java.util.Locale

class InstalledAppsRepository(
    context: Context,
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java),
    private val selfPackageName: String = context.packageName,
) {
    fun observeApps(): Flow<List<LauncherApp>> = callbackFlow {
        fun publish() {
            trySend(loadApps())
        }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: android.os.UserHandle) = publish()

            override fun onPackageRemoved(packageName: String, user: android.os.UserHandle) = publish()

            override fun onPackageChanged(packageName: String, user: android.os.UserHandle) = publish()

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
                replacing: Boolean,
            ) = publish()

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
                replacing: Boolean,
            ) = publish()
        }

        publish()
        launcherApps.registerCallback(callback)

        awaitClose {
            launcherApps.unregisterCallback(callback)
        }
    }.conflate()

    fun loadApps(): List<LauncherApp> {
        return launcherApps
            .getActivityList(null, Process.myUserHandle())
            .asSequence()
            .map { info ->
                LauncherApp(
                    packageName = info.applicationInfo.packageName,
                    activityName = info.name,
                    label = info.label?.toString().orEmpty().ifBlank { info.name.substringAfterLast('.') },
                )
            }
            .filterNot { it.packageName == selfPackageName }
            .toList()
            .sortAlphabetically()
    }
}

fun List<LauncherApp>.sortAlphabetically(): List<LauncherApp> {
    return sortedWith(
        compareBy<LauncherApp> { app ->
            app.label.lowercase(Locale.getDefault())
        }.thenBy { app ->
            app.packageName
        },
    )
}
