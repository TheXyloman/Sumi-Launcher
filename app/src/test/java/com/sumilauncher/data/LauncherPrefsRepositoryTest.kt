package com.sumilauncher.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.sumilauncher.model.LauncherApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LauncherPrefsRepositoryTest {
    @Test
    fun pinnedApps_canAddMoveRemoveAndPrune() = runTest {
        val repository = createRepository(backgroundScope)
        val reader = LauncherApp(
            packageName = "com.example.reader",
            activityName = "ReaderActivity",
            label = "Reader",
        )
        val notes = LauncherApp(
            packageName = "com.example.notes",
            activityName = "NotesActivity",
            label = "Notes",
        )

        repository.setPinned(reader, true)
        repository.setPinned(notes, true)

        assertEquals(
            listOf(reader.componentKey, notes.componentKey),
            repository.pinnedApps.first().map { it.componentKey },
        )

        repository.movePinned(notes.componentKey, LauncherPrefsRepository.MoveDirection.UP)

        assertEquals(
            listOf(notes.componentKey, reader.componentKey),
            repository.pinnedApps.first().map { it.componentKey },
        )

        repository.setPinned(notes, false)

        assertEquals(
            listOf(reader.componentKey),
            repository.pinnedApps.first().map { it.componentKey },
        )

        repository.prunePinnedApps(setOf(notes.componentKey))

        assertTrue(repository.pinnedApps.first().isEmpty())
    }

    @Test
    fun onboardingFlag_persists() = runTest {
        val repository = createRepository(backgroundScope)

        assertFalse(repository.onboardingComplete.first())

        repository.setOnboardingComplete(true)

        assertTrue(repository.onboardingComplete.first())
    }

    private fun createRepository(scope: CoroutineScope): LauncherPrefsRepository {
        val file = File.createTempFile("launcher-prefs", ".preferences_pb").apply {
            deleteOnExit()
        }
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
        return LauncherPrefsRepository(dataStore)
    }
}
