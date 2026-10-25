package com.sumilauncher.ui

import com.sumilauncher.ui.components.pageRanges
import org.junit.Assert.*
import org.junit.Test

class PaginationTest {
    @Test fun emptyListAndZeroHeightHaveNoPages() {
        assertTrue(pageRanges(emptyList(), 200).isEmpty())
        assertTrue(pageRanges(listOf(64), 0).isEmpty())
    }
    @Test fun exactBoundaryDoesNotAddAnEmptyPage() {
        assertEquals(listOf(0..2), pageRanges(listOf(64, 64, 64), 192))
    }
    @Test fun variableHeightRowsStayWholeAndInOrder() {
        val pages = pageRanges(listOf(64, 90, 64, 120, 64), 160)
        assertEquals(listOf(0..1, 2..2, 3..3, 4..4), pages)
        assertEquals((0..4).toList(), pages.flatMap { it.toList() })
    }
    @Test fun oneRowPerPageAtLargeTextSize() {
        assertEquals(listOf(0..0, 1..1, 2..2), pageRanges(listOf(120, 120, 120), 160))
    }
    @Test(expected = IllegalArgumentException::class) fun oversizedRowsAreRejectedRatherThanClipped() {
        pageRanges(listOf(180), 160)
    }
}
