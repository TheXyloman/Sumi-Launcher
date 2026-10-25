package com.sumilauncher.ui.components

/** Greedy packing keeps complete rows on each page, including at exact boundaries. */
fun pageRanges(rowHeights: List<Int>, availableHeight: Int): List<IntRange> {
    if (rowHeights.isEmpty() || availableHeight <= 0) return emptyList()
    val pages = mutableListOf<IntRange>()
    var start = 0
    var used = 0
    rowHeights.forEachIndexed { index, height ->
        require(height > 0 && height <= availableHeight) { "A row must fit on a page" }
        if (used + height > availableHeight) {
            pages += start until index
            start = index
            used = 0
        }
        used += height
    }
    pages += start..rowHeights.lastIndex
    return pages
}
