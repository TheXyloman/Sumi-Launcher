package com.sumilauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.model.PinnedApp
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.Base64

private val Context.launcherPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

data class LauncherPreferences(
    val pinnedApps: List<PinnedApp> = emptyList(),
    val onboardingComplete: Boolean = false,
    val customNames: Map<String, String> = emptyMap(),
    val hiddenComponents: Set<String> = emptySet(),
    val hiddenLabels: Map<String, String> = emptyMap(),
    val showIcons: Boolean = false,
)

class LauncherPrefsRepository(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(context.launcherPrefsDataStore)

    val preferences: Flow<LauncherPreferences> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            LauncherPreferences(
                pinnedApps = PinnedAppsCodec.decode(prefs[PINNED_APPS_KEY].orEmpty()),
                onboardingComplete = prefs[ONBOARDING_COMPLETE_KEY] ?: false,
                customNames = AppNamesCodec.decode(prefs[CUSTOM_NAMES_KEY].orEmpty()),
                hiddenComponents = prefs[HIDDEN_COMPONENTS_KEY].orEmpty(),
                hiddenLabels = AppNamesCodec.decode(prefs[HIDDEN_LABELS_KEY].orEmpty()),
                showIcons = prefs[SHOW_ICONS_KEY] ?: false,
            )
        }.distinctUntilChanged()
    val pinnedApps = preferences.map { it.pinnedApps }.distinctUntilChanged()
    val onboardingComplete = preferences.map { it.onboardingComplete }.distinctUntilChanged()

    suspend fun setPinned(app: LauncherApp, enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = PinnedAppsCodec.decode(prefs[PINNED_APPS_KEY].orEmpty())
            val updated = if (enabled && app.componentKey !in prefs[HIDDEN_COMPONENTS_KEY].orEmpty()) {
                (current + PinnedApp(app.packageName, app.activityName, app.originalLabel, current.size))
                    .distinctBy { it.componentKey }
            } else current.filterNot { it.componentKey == app.componentKey }
            prefs[PINNED_APPS_KEY] = PinnedAppsCodec.encode(updated.normalized())
        }
    }

    suspend fun movePinned(componentKey: String, direction: MoveDirection) {
        dataStore.edit { prefs ->
            val current = PinnedAppsCodec.decode(prefs[PINNED_APPS_KEY].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.componentKey == componentKey }
            if (index < 0) return@edit
            val target = (index + if (direction == MoveDirection.UP) -1 else 1).coerceIn(current.indices)
            if (target == index) return@edit
            current.add(target, current.removeAt(index))
            prefs[PINNED_APPS_KEY] = PinnedAppsCodec.encode(current.normalized())
        }
    }

    suspend fun renameApp(componentKey: String, name: String) {
        dataStore.edit { prefs ->
            val names = AppNamesCodec.decode(prefs[CUSTOM_NAMES_KEY].orEmpty()).toMutableMap()
            val cleaned = name.trim()
            if (cleaned.isEmpty()) names.remove(componentKey) else names[componentKey] = cleaned
            prefs[CUSTOM_NAMES_KEY] = AppNamesCodec.encode(names)
        }
    }

    suspend fun setHidden(app: LauncherApp, hidden: Boolean) {
        val componentKey = app.componentKey
        dataStore.edit { prefs ->
            val keys = prefs[HIDDEN_COMPONENTS_KEY].orEmpty()
            prefs[HIDDEN_COMPONENTS_KEY] = if (hidden) keys + componentKey else keys - componentKey
            // Keep recovery labels even while a hidden package is temporarily unavailable.
            val labels = AppNamesCodec.decode(prefs[HIDDEN_LABELS_KEY].orEmpty()).toMutableMap()
            if (hidden) labels[componentKey] = app.originalLabel else labels.remove(componentKey)
            prefs[HIDDEN_LABELS_KEY] = AppNamesCodec.encode(labels)
            if (hidden) {
                val pins = PinnedAppsCodec.decode(prefs[PINNED_APPS_KEY].orEmpty())
                prefs[PINNED_APPS_KEY] = PinnedAppsCodec.encode(pins.filterNot { it.componentKey == componentKey }.normalized())
            }
        }
    }

    // Only called for a PACKAGE_REMOVED broadcast with EXTRA_REPLACING=false.
    // Missing launcher activities and temporarily unavailable packages are not uninstalls.
    suspend fun removeUninstalledPackage(packageName: String) {
        dataStore.edit { prefs ->
            val pins = PinnedAppsCodec.decode(prefs[PINNED_APPS_KEY].orEmpty())
            prefs[PINNED_APPS_KEY] = PinnedAppsCodec.encode(pins.filterNot { it.packageName == packageName }.normalized())
            val prefix = "$packageName/"
            prefs[HIDDEN_COMPONENTS_KEY] = prefs[HIDDEN_COMPONENTS_KEY].orEmpty().filterNot { it.startsWith(prefix) }.toSet()
            prefs[HIDDEN_LABELS_KEY] = AppNamesCodec.encode(AppNamesCodec.decode(prefs[HIDDEN_LABELS_KEY].orEmpty()).filterKeys { !it.startsWith(prefix) })
            prefs[CUSTOM_NAMES_KEY] = AppNamesCodec.encode(AppNamesCodec.decode(prefs[CUSTOM_NAMES_KEY].orEmpty()).filterKeys { !it.startsWith(prefix) })
        }
    }

    suspend fun setShowIcons(value: Boolean) { dataStore.edit { it[SHOW_ICONS_KEY] = value } }
    suspend fun setOnboardingComplete(complete: Boolean) { dataStore.edit { it[ONBOARDING_COMPLETE_KEY] = complete } }
    private fun List<PinnedApp>.normalized() = mapIndexed { index, app -> app.copy(sortOrder = index) }
    enum class MoveDirection { UP, DOWN }

    private companion object {
        val PINNED_APPS_KEY = stringPreferencesKey("pinned_apps")
        val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
        val CUSTOM_NAMES_KEY = stringPreferencesKey("custom_names")
        val HIDDEN_COMPONENTS_KEY = stringSetPreferencesKey("hidden_components")
        val HIDDEN_LABELS_KEY = stringPreferencesKey("hidden_labels")
        val SHOW_ICONS_KEY = booleanPreferencesKey("show_icons")
    }
}

internal object AppNamesCodec {
    private fun encodeField(value: String) = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))
    private fun decodeField(value: String) = Base64.getUrlDecoder().decode(value).toString(Charsets.UTF_8)
    fun encode(names: Map<String, String>): String = names.entries.sortedBy { it.key }
        .joinToString("\n") { "${encodeField(it.key)}\t${encodeField(it.value)}" }
    fun decode(raw: String): Map<String, String> = raw.lineSequence().mapNotNull { line ->
        val parts = line.split('\t')
        if (parts.size != 2) null else runCatching { decodeField(parts[0]) to decodeField(parts[1]) }.getOrNull()
    }.toMap()
}
