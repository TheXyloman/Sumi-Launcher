package com.sumilauncher

import android.app.Application
import com.sumilauncher.data.InstalledAppsRepository
import com.sumilauncher.data.LauncherPrefsRepository

class SumiApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(
            prefsRepository = LauncherPrefsRepository(this),
            installedAppsRepository = InstalledAppsRepository(this),
        )
    }
}

data class AppContainer(
    val prefsRepository: LauncherPrefsRepository,
    val installedAppsRepository: InstalledAppsRepository,
)
