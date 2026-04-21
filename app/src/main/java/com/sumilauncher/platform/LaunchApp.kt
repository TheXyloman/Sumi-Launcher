package com.sumilauncher.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.sumilauncher.model.LauncherApp

fun Context.launchApp(app: LauncherApp) {
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(ComponentName(app.packageName, app.activityName))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivity(intent)
}
