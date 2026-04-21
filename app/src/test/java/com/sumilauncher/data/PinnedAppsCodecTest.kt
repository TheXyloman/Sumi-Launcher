package com.sumilauncher.data

import com.sumilauncher.model.PinnedApp
import org.junit.Assert.assertEquals
import org.junit.Test

class PinnedAppsCodecTest {
    @Test
    fun encodeAndDecode_roundTripsPinnedApps() {
        val apps = listOf(
            PinnedApp(
                packageName = "com.example.reader",
                activityName = "com.example.reader.ReaderActivity",
                label = "Reader\tOne",
                sortOrder = 1,
            ),
            PinnedApp(
                packageName = "com.example.notes",
                activityName = "com.example.notes.NotesActivity",
                label = "Notes\nTwo",
                sortOrder = 0,
            ),
        )

        val encoded = PinnedAppsCodec.encode(apps)

        assertEquals(
            listOf(apps[1], apps[0]),
            PinnedAppsCodec.decode(encoded),
        )
    }
}
