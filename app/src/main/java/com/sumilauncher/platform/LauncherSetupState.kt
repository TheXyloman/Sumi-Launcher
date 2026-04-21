package com.sumilauncher.platform

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class LauncherSetupState(
    val isDefaultLauncher: Boolean,
    val isAccessibilityEnabled: Boolean,
)

object LauncherSetupStateReader {
    fun read(context: Context): LauncherSetupState {
        return LauncherSetupState(
            isDefaultLauncher = isDefaultLauncher(context),
            isAccessibilityEnabled = NotificationShadeController.isEnabled(context),
        )
    }

    private fun isDefaultLauncher(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return resolved?.activityInfo?.packageName == context.packageName
    }
}
