package com.sumilauncher.data

import androidx.datastore.preferences.core.*
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.model.PinnedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class LauncherPrefsRepositoryTest {
    private val reader = LauncherApp("com.example.reader", "ReaderActivity", "Reader")
    private val notes = LauncherApp("com.example.notes", "NotesActivity", "Notes")

    @Test fun legacyPreferencesKeepPinsOrderAndOnboardingWithTextOnlyDefaults() = runTest {
        val store = createStore(backgroundScope)
        val pins = listOf(PinnedApp(notes.packageName, notes.activityName, notes.label, 0),
            PinnedApp(reader.packageName, reader.activityName, reader.label, 1))
        store.edit {
            it[stringPreferencesKey("pinned_apps")] = PinnedAppsCodec.encode(pins)
            it[booleanPreferencesKey("onboarding_complete")] = true
        }
        val saved = LauncherPrefsRepository(store).preferences.first()
        assertEquals(pins, saved.pinnedApps)
        assertTrue(saved.onboardingComplete)
        assertFalse(saved.showIcons)
        assertTrue(saved.customNames.isEmpty())
        assertTrue(saved.hiddenComponents.isEmpty())
    }

    @Test fun pinsCanBeAddedMovedRemovedAndLeftEmpty() = runTest {
        val repo = LauncherPrefsRepository(createStore(backgroundScope))
        repo.setPinned(reader, true)
        repo.setPinned(notes, true)
        repo.setPinned(reader, true)
        assertEquals(listOf(reader.componentKey, notes.componentKey), repo.pinnedApps.first().map { it.componentKey })
        repo.movePinned(notes.componentKey, LauncherPrefsRepository.MoveDirection.UP)
        assertEquals(listOf(notes.componentKey, reader.componentKey), repo.pinnedApps.first().map { it.componentKey })
        repo.movePinned(notes.componentKey, LauncherPrefsRepository.MoveDirection.UP)
        repo.movePinned("missing", LauncherPrefsRepository.MoveDirection.DOWN)
        assertEquals(listOf(0, 1), repo.pinnedApps.first().map { it.sortOrder })
        repo.setPinned(notes, false)
        repo.setPinned(reader, false)
        assertTrue(repo.pinnedApps.first().isEmpty())
    }

    @Test fun aliasesTrimPersistAndBlankRestoresOriginal() = runTest {
        val store = createStore(backgroundScope)
        val repo = LauncherPrefsRepository(store)
        repo.renameApp(reader.componentKey, "  Read\t静かに\n  ")
        assertEquals("Read\t静かに", LauncherPrefsRepository(store).preferences.first().customNames[reader.componentKey])
        repo.renameApp(reader.componentKey, " \t\n ")
        assertTrue(repo.preferences.first().customNames.isEmpty())
    }

    @Test fun hidingAtomicallyUnpinsAndRestoringDoesNotRepin() = runTest {
        val repo = LauncherPrefsRepository(createStore(backgroundScope))
        repo.setPinned(reader, true)
        repo.setPinned(notes, true)
        repo.renameApp(reader.componentKey, "Read")
        repo.setHidden(reader, true)
        val hidden = repo.preferences.first()
        assertEquals(setOf(reader.componentKey), hidden.hiddenComponents)
        assertEquals("Reader", hidden.hiddenLabels[reader.componentKey])
        assertEquals(listOf(notes.componentKey), hidden.pinnedApps.map { it.componentKey })
        assertEquals(listOf(0), hidden.pinnedApps.map { it.sortOrder })
        repo.setPinned(reader, true)
        assertEquals(1, repo.pinnedApps.first().size)
        repo.setHidden(reader, false)
        assertTrue(repo.preferences.first().hiddenComponents.isEmpty())
        assertEquals(listOf(notes.componentKey), repo.pinnedApps.first().map { it.componentKey })
        assertEquals("Read", repo.preferences.first().customNames[reader.componentKey])
    }

    @Test fun confirmedUninstallRemovesOnlyThatPackagesConfiguration() = runTest {
        val repo = LauncherPrefsRepository(createStore(backgroundScope))
        repo.setPinned(reader, true)
        repo.setPinned(notes, true)
        repo.renameApp(reader.componentKey, "Read")
        repo.renameApp(notes.componentKey, "Write")
        repo.setHidden(reader, true)
        repo.removeUninstalledPackage(reader.packageName)
        val saved = repo.preferences.first()
        assertTrue(saved.hiddenComponents.isEmpty())
        assertEquals(mapOf(notes.componentKey to "Write"), saved.customNames)
        assertEquals(listOf(notes.componentKey), saved.pinnedApps.map { it.componentKey })
    }

    @Test fun onboardingAndIconPreferencePersist() = runTest {
        val store = createStore(backgroundScope)
        val repo = LauncherPrefsRepository(store)
        repo.setOnboardingComplete(true)
        repo.setShowIcons(true)
        val saved = LauncherPrefsRepository(store).preferences.first()
        assertTrue(saved.onboardingComplete)
        assertTrue(saved.showIcons)
    }

    private fun createStore(scope: CoroutineScope) = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { File.createTempFile("launcher-prefs", ".preferences_pb").apply { deleteOnExit() } },
    )
}
