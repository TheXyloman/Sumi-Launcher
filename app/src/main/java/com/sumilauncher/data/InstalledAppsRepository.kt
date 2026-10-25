package com.sumilauncher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import androidx.core.content.ContextCompat
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.platform.AppIconCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.Locale

class InstalledAppsRepository(
    private val context: Context,
    private val iconCache: AppIconCache,
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java),
) {
    private var refresh: (() -> Unit)? = null
    fun refresh() { refresh?.invoke() }

    fun observeApps(onConfirmedUninstall: suspend (String) -> Unit): Flow<AppCatalog> = callbackFlow {
        data class Change(val packages: List<String> = emptyList(), val removed: String? = null)
        val changes = Channel<Change>(Channel.UNLIMITED)
        val callback = object : LauncherApps.Callback() {
            fun changed(packages: List<String>, user: UserHandle) {
                if (user == Process.myUserHandle()) changes.trySend(Change(packages))
            }
            override fun onPackageAdded(packageName: String, user: UserHandle) = changed(listOf(packageName), user)
            override fun onPackageRemoved(packageName: String, user: UserHandle) = changed(listOf(packageName), user)
            override fun onPackageChanged(packageName: String, user: UserHandle) = changed(listOf(packageName), user)
            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = changed(packageNames.toList(), user)
            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = changed(packageNames.toList(), user)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_PACKAGE_REMOVED && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    intent.data?.schemeSpecificPart?.let { changes.trySend(Change(listOf(it), removed = it)) }
                }
            }
        }
        launcherApps.registerCallback(callback)
        ContextCompat.registerReceiver(context, receiver,
            IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply { addDataScheme("package") }, ContextCompat.RECEIVER_EXPORTED)
        refresh = { changes.trySend(Change()) }
        changes.trySend(Change())
        val worker = launch(Dispatchers.IO) {
            var last = AppCatalog()
            for (change in changes) {
                change.packages.forEach(iconCache::invalidate)
                var failure: String? = null
                if (change.removed != null) {
                    try { onConfirmedUninstall(change.removed) }
                    catch (e: java.io.IOException) { failure = "Could not save app changes. Please try again." }
                }
                try {
                    last = AppCatalog(loadApps(), loaded = true, revision = last.revision + 1, error = failure)
                } catch (e: RuntimeException) {
                    last = last.copy(loaded = true, revision = last.revision + 1, error = "Could not load your apps. Try again in Settings.")
                }
                send(last)
            }
        }
        awaitClose {
            refresh = null
            launcherApps.unregisterCallback(callback)
            context.unregisterReceiver(receiver)
            changes.close()
            worker.cancel()
        }
    }

    fun loadApps(): List<LauncherApp> = launcherApps.getActivityList(null, Process.myUserHandle())
        .map { info -> LauncherApp(info.applicationInfo.packageName, info.name,
            info.label?.toString().orEmpty().ifBlank { info.name.substringAfterLast('.') }) }
        .filterNot { it.packageName == context.packageName }
        .distinctBy { it.componentKey }.sortAlphabetically()
}

fun List<LauncherApp>.sortAlphabetically(): List<LauncherApp> = sortedWith(
    compareBy<LauncherApp> { it.label.lowercase(Locale.getDefault()) }.thenBy { it.componentKey },
)
