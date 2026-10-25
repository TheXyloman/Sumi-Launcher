package com.sumilauncher

import android.app.Application
import com.sumilauncher.data.InstalledAppsRepository
import com.sumilauncher.data.LauncherPrefsRepository
import com.sumilauncher.platform.AppIconCache

class SumiApplication : Application() {
    val container: AppContainer by lazy {
        val icons = AppIconCache(this)
        AppContainer(LauncherPrefsRepository(this), InstalledAppsRepository(this, icons), icons)
    }
}

data class AppContainer(
    val prefsRepository: LauncherPrefsRepository,
    val installedAppsRepository: InstalledAppsRepository,
    val iconCache: AppIconCache,
)
