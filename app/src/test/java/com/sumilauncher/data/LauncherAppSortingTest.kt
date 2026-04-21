package com.sumilauncher.data

import com.sumilauncher.model.LauncherApp
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherAppSortingTest {
    @Test
    fun sortAlphabetically_ordersByLabelIgnoringCase() {
        val apps = listOf(
            LauncherApp("com.example.zebra", "ZebraActivity", "zebra"),
            LauncherApp("com.example.alpha", "AlphaActivity", "Alpha"),
            LauncherApp("com.example.beta", "BetaActivity", "beta"),
        )

        assertEquals(
            listOf("Alpha", "beta", "zebra"),
            apps.sortAlphabetically().map { it.label },
        )
    }
}
