package com.sumilauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.model.PinnedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.launcherPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "launcher_prefs",
)

class LauncherPrefsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.launcherPrefsDataStore)

    val pinnedApps: Flow<List<PinnedApp>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            PinnedAppsCodec.decode(preferences[PINNED_APPS_KEY].orEmpty())
        }
        .distinctUntilChanged()

    val onboardingComplete: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[ONBOARDING_COMPLETE_KEY] ?: false
        }
        .distinctUntilChanged()

    suspend fun setPinned(app: LauncherApp, enabled: Boolean) {
        dataStore.edit { preferences ->
            val current = PinnedAppsCodec.decode(preferences[PINNED_APPS_KEY].orEmpty())
            val updated = if (enabled) {
                val nextOrder = (current.maxOfOrNull { it.sortOrder } ?: -1) + 1
                (current + PinnedApp(app.packageName, app.activityName, app.label, nextOrder))
                    .distinctBy { it.componentKey }
            } else {
                current.filterNot { it.componentKey == app.componentKey }
            }.normalizeSortOrder()

            preferences[PINNED_APPS_KEY] = PinnedAppsCodec.encode(updated)
        }
    }

    suspend fun movePinned(componentKey: String, direction: MoveDirection) {
        dataStore.edit { preferences ->
            val current = PinnedAppsCodec.decode(preferences[PINNED_APPS_KEY].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.componentKey == componentKey }
            if (index == -1) {
                return@edit
            }

            val targetIndex = when (direction) {
                MoveDirection.UP -> (index - 1).coerceAtLeast(0)
                MoveDirection.DOWN -> (index + 1).coerceAtMost(current.lastIndex)
            }

            if (targetIndex == index) {
                return@edit
            }

            val item = current.removeAt(index)
            current.add(targetIndex, item)
            preferences[PINNED_APPS_KEY] = PinnedAppsCodec.encode(current.normalizeSortOrder())
        }
    }

    suspend fun prunePinnedApps(validComponentKeys: Set<String>) {
        dataStore.edit { preferences ->
            val current = PinnedAppsCodec.decode(preferences[PINNED_APPS_KEY].orEmpty())
            val updated = current
                .filter { it.componentKey in validComponentKeys }
                .normalizeSortOrder()

            if (updated == current) {
                return@edit
            }

            preferences[PINNED_APPS_KEY] = PinnedAppsCodec.encode(updated)
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETE_KEY] = complete
        }
    }

    private fun List<PinnedApp>.normalizeSortOrder(): List<PinnedApp> {
        return mapIndexed { index, app ->
            app.copy(sortOrder = index)
        }
    }

    enum class MoveDirection {
        UP,
        DOWN,
    }

    private companion object {
        val PINNED_APPS_KEY = stringPreferencesKey("pinned_apps")
        val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
    }
}
