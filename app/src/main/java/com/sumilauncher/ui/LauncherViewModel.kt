package com.sumilauncher.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumilauncher.AppContainer
import com.sumilauncher.data.*
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.platform.LauncherSetupState
import com.sumilauncher.platform.LauncherSetupStateReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Display lists are derived once per catalog/preferences change, never by the clock.
data class LauncherUiState(
    val isLoaded: Boolean = false,
    val onboardingComplete: Boolean = false,
    val setupState: LauncherSetupState = LauncherSetupState(false, false),
    val allApps: List<LauncherApp> = emptyList(),
    val pinnedApps: List<LauncherApp> = emptyList(),
    val hiddenApps: List<LauncherApp> = emptyList(),
    val customNames: Map<String, String> = emptyMap(),
    val showIcons: Boolean = false,
    val iconRevision: Int = 0,
    val loadError: String? = null,
) {
    val pinnedKeys: Set<String> get() = pinnedApps.mapTo(mutableSetOf()) { it.componentKey }
}

class LauncherViewModel(private val application: Application, private val container: AppContainer) : ViewModel() {
    private val setup = MutableStateFlow(LauncherSetupStateReader.read(application))
    private val messages = Channel<String>(Channel.BUFFERED)
    val errors = messages.receiveAsFlow()
    private val catalog = container.installedAppsRepository.observeApps(container.prefsRepository::removeUninstalledPackage)
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppCatalog())
    private val display = combine(catalog, container.prefsRepository.preferences) { apps, prefs ->
        val displayApps = buildDisplayApps(apps.apps, prefs)
        LauncherUiState(
            isLoaded = apps.loaded, onboardingComplete = prefs.onboardingComplete,
            allApps = displayApps.all, pinnedApps = displayApps.pinned, hiddenApps = displayApps.hidden,
            customNames = prefs.customNames, showIcons = prefs.showIcons,
            iconRevision = apps.revision, loadError = apps.error,
        )
    }.flowOn(Dispatchers.Default)
    val uiState = combine(display, setup) { state, currentSetup -> state.copy(setupState = currentSetup) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherUiState())

    fun refreshSetupState() { setup.value = LauncherSetupStateReader.read(application) }
    fun retryApps() = container.installedAppsRepository.refresh()
    private fun save(block: suspend LauncherPrefsRepository.() -> Unit) {
        viewModelScope.launch {
            try { container.prefsRepository.block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { messages.send("Your change could not be saved. Please try again.") }
        }
    }
    fun setPinned(app: LauncherApp, enabled: Boolean) = save { setPinned(app, enabled) }
    fun movePinnedUp(app: LauncherApp) = save { movePinned(app.componentKey, LauncherPrefsRepository.MoveDirection.UP) }
    fun movePinnedDown(app: LauncherApp) = save { movePinned(app.componentKey, LauncherPrefsRepository.MoveDirection.DOWN) }
    fun renameApp(app: LauncherApp, name: String) = save { renameApp(app.componentKey, name) }
    fun setHidden(app: LauncherApp, hidden: Boolean) = save { setHidden(app, hidden) }
    fun setShowIcons(value: Boolean) = save { setShowIcons(value) }
    fun completeOnboarding() = save { setOnboardingComplete(true) }

    class Factory(private val application: Application, private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LauncherViewModel::class.java))
            return LauncherViewModel(application, container) as T
        }
    }
}
