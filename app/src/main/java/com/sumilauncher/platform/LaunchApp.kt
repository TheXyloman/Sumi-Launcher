package com.sumilauncher.platform

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.sumilauncher.model.LauncherApp

fun Context.launchApp(app: LauncherApp): Boolean {
    if (!app.available) return false
    return startActivitySafely(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(ComponentName(app.packageName, app.activityName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
fun Context.openAppInfo(app: LauncherApp): Boolean = startActivitySafely(
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}")))
fun Context.startActivitySafely(intent: Intent): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
    true
} catch (e: android.content.ActivityNotFoundException) { false }
catch (e: SecurityException) { false }
