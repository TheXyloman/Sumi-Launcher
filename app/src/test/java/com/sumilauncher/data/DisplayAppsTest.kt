package com.sumilauncher.data

import com.sumilauncher.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class DisplayAppsTest {
    private val reader = LauncherApp("reader", "Main", "Books")
    private val notes = LauncherApp("notes", "Main", "Notes")
    private fun pin(app: LauncherApp, order: Int) = PinnedApp(app.packageName, app.activityName, app.label, order)

    @Test fun displayNamesApplyEverywhereAndLibrarySortsByDisplayName() {
        val result = buildDisplayApps(listOf(reader, notes), LauncherPreferences(
            pinnedApps = listOf(pin(reader, 0), pin(notes, 1)), customNames = mapOf(reader.componentKey to "Reading")))
        assertEquals(listOf("Notes", "Reading"), result.all.map { it.label })
        assertEquals(listOf("Reading", "Notes"), result.pinned.map { it.label })
        assertEquals("Books", result.pinned.first().originalLabel)
    }

    @Test fun temporarilyMissingAppKeepsItsPinAndName() {
        val result = buildDisplayApps(listOf(notes), LauncherPreferences(
            pinnedApps = listOf(pin(reader, 0), pin(notes, 1)), customNames = mapOf(reader.componentKey to "Read")))
        assertEquals(listOf("Read", "Notes"), result.pinned.map { it.label })
        assertFalse(result.pinned.first().available)
        assertTrue(result.pinned.last().available)
        assertEquals(1, result.all.size)
    }

    @Test fun hiddenAppsNeverAppearInOrdinaryListsOrSearch() {
        val result = buildDisplayApps(listOf(reader, notes), LauncherPreferences(
            pinnedApps = listOf(pin(reader, 0)), hiddenComponents = setOf(reader.componentKey)))
        assertTrue(result.pinned.isEmpty())
        assertTrue(result.all.matching("Books").isEmpty())
        assertEquals(listOf(reader), result.hidden)
    }

    @Test fun hiddenMissingAppsCanStillBeRestored() {
        val result = buildDisplayApps(emptyList(), LauncherPreferences(hiddenComponents = setOf(reader.componentKey),
            customNames = mapOf(reader.componentKey to "Read"), hiddenLabels = mapOf(reader.componentKey to "Books")))
        assertEquals(reader.componentKey, result.hidden.single().componentKey)
        assertEquals("Read", result.hidden.single().label)
        assertFalse(result.hidden.single().available)
        assertEquals("Books", result.hidden.single().originalLabel)
    }

    @Test fun searchMatchesOriginalAndAliasIgnoringCaseAndWhitespace() {
        val app = reader.copy(label = "Read")
        assertEquals(listOf(app), listOf(app).matching("  BOOKS  "))
        assertEquals(listOf(app), listOf(app).matching("read"))
        assertEquals(listOf(app), listOf(app).matching("   "))
        assertTrue(listOf(app).matching("writing").isEmpty())
    }

    @Test fun caseInsensitiveSearchDoesNotDependOnTurkishLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val files = LauncherApp("files", "Main", "Files")
            assertEquals(listOf(files), listOf(files).matching("FILES"))
        } finally { Locale.setDefault(previous) }
    }

    @Test fun malformedAliasEntryDoesNotDiscardOtherAliases() {
        val encoded = AppNamesCodec.encode(mapOf(reader.componentKey to "Read"))
        assertEquals(mapOf(reader.componentKey to "Read"), AppNamesCodec.decode("%%%\t%%%\n$encoded"))
    }
}
