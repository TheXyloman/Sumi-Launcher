package com.sumilauncher.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumilauncher.AppContainer
import com.sumilauncher.data.InstalledAppsRepository
import com.sumilauncher.data.LauncherPrefsRepository
import com.sumilauncher.data.LauncherPrefsRepository.MoveDirection
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.model.PinnedApp
import com.sumilauncher.platform.LauncherSetupState
import com.sumilauncher.platform.LauncherSetupStateReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val isLoaded: Boolean = false,
    val onboardingComplete: Boolean = false,
    val setupState: LauncherSetupState = LauncherSetupState(
        isDefaultLauncher = false,
        isAccessibilityEnabled = false,
    ),
    val allApps: List<LauncherApp> = emptyList(),
    val pinnedApps: List<PinnedApp> = emptyList(),
) {
    val pinnedKeys: Set<String>
        get() = pinnedApps.mapTo(mutableSetOf()) { it.componentKey }
}

class LauncherViewModel(
    private val application: Application,
    private val prefsRepository: LauncherPrefsRepository,
    installedAppsRepository: InstalledAppsRepository,
) : ViewModel() {
    private val appsLoaded = MutableStateFlow(false)
    private val setupState = MutableStateFlow(LauncherSetupStateReader.read(application))

    private val installedApps = installedAppsRepository.observeApps()
        .onEach { apps ->
            appsLoaded.value = true
            prefsRepository.prunePinnedApps(apps.mapTo(mutableSetOf()) { it.componentKey })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    val uiState: StateFlow<LauncherUiState> = combine(
        appsLoaded,
        installedApps,
        prefsRepository.pinnedApps,
        prefsRepository.onboardingComplete,
        setupState,
    ) { isLoaded, apps, pinnedApps, onboardingComplete, currentSetupState ->
        val appLabelsByKey = apps.associateBy { it.componentKey }
        LauncherUiState(
            isLoaded = isLoaded,
            onboardingComplete = onboardingComplete,
            setupState = currentSetupState,
            allApps = apps,
            pinnedApps = pinnedApps
                .filter { it.componentKey in appLabelsByKey }
                .sortedBy { it.sortOrder }
                .map { pinnedApp ->
                    val currentApp = appLabelsByKey[pinnedApp.componentKey]
                    if (currentApp == null || currentApp.label == pinnedApp.label) {
                        pinnedApp
                    } else {
                        pinnedApp.copy(label = currentApp.label)
                    }
                },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = LauncherUiState(),
    )

    fun refreshSetupState() {
        setupState.value = LauncherSetupStateReader.read(application)
    }

    fun setPinned(app: LauncherApp, enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setPinned(app, enabled)
        }
    }

    fun movePinnedUp(componentKey: String) {
        movePinned(componentKey, MoveDirection.UP)
    }

    fun movePinnedDown(componentKey: String) {
        movePinned(componentKey, MoveDirection.DOWN)
    }

    fun setOnboardingComplete(complete: Boolean) {
        viewModelScope.launch {
            prefsRepository.setOnboardingComplete(complete)
        }
    }

    private fun movePinned(componentKey: String, direction: MoveDirection) {
        viewModelScope.launch {
            prefsRepository.movePinned(componentKey, direction)
        }
    }

    class Factory(
        private val application: Application,
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
                return LauncherViewModel(
                    application = application,
                    prefsRepository = container.prefsRepository,
                    installedAppsRepository = container.installedAppsRepository,
                ) as T
            }
            error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
