package com.sumilauncher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sumilauncher.model.LauncherApp
import kotlin.math.max

@Composable
fun PagedAppList(
    apps: List<LauncherApp>,
    modifier: Modifier = Modifier,
    showIcons: Boolean = false,
    iconRevision: Int = 0,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    resetKey: Any = Unit,
    tagPrefix: String = "app",
    emptyTitle: String = "No apps here yet",
    emptyDescription: String = "Choose apps to make this space your own.",
    emptyAction: (@Composable () -> Unit)? = null,
    onClick: (LauncherApp) -> Unit,
    onLongClick: ((LauncherApp) -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    trailingWidth: Dp = 0.dp,
    trailingContent: (@Composable RowScope.(LauncherApp) -> Unit)? = null,
    bottomContent: (@Composable (LauncherApp, Int) -> Unit)? = null,
) {
    var savedPage by rememberSaveable(resetKey) { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier.fillMaxWidth().testTag("paged_apps")) {
        val footerHeight = 64.dp
        val availablePx = with(density) { (maxHeight - footerHeight).roundToPx() }.coerceAtLeast(0)
        val widthPx = with(density) {
            (maxWidth - 8.dp - (if (showIcons) 44.dp else 0.dp) - trailingWidth - (if (trailingWidth > 0.dp) 12.dp else 0.dp)).roundToPx()
        }.coerceAtLeast(1)
        val bottomPx = with(density) { (if (bottomContent != null) 48.dp else 0.dp).roundToPx() }
        val minRowPx = with(density) { 64.dp.roundToPx() }
        val paddingPx = with(density) { 20.dp.roundToPx() }
        val heights = remember(apps, widthPx, labelStyle, density.density, density.fontScale, bottomPx) {
            apps.map { app ->
                max(minRowPx, measurer.measure(app.label, style = labelStyle, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, constraints = Constraints(maxWidth = widthPx)).size.height + paddingPx) + bottomPx
            }
        }
        val fits = heights.all { it <= availablePx }
        val pages = remember(heights, availablePx) { if (fits) pageRanges(heights, availablePx) else emptyList() }
        val page = savedPage.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
        LaunchedEffect(pages.size) { if (savedPage != page) savedPage = page }
        val previous: () -> Unit = { if (page > 0) savedPage = page - 1 }
        val next: () -> Unit = { if (page < pages.lastIndex) savedPage = page + 1 }
        Column(Modifier.fillMaxSize().einkSwipes(onLeft = onSwipeLeft, onRight = onSwipeRight, onUp = next, onDown = previous)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    apps.isEmpty() -> Column(Modifier.align(Alignment.CenterStart).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(emptyTitle, style = MaterialTheme.typography.headlineSmall)
                        Text(emptyDescription, style = MaterialTheme.typography.bodyMedium)
                        emptyAction?.invoke()
                    }
                    !fits -> {
                        val keyboard = LocalSoftwareKeyboardController.current
                        Column(Modifier.align(Alignment.CenterStart)) {
                            Text("Make a little room", style = MaterialTheme.typography.headlineSmall)
                            Text("Close the keyboard or reduce the display size to see complete app rows.", style = MaterialTheme.typography.bodyMedium)
                            TextAction("Close keyboard", onClick = { keyboard?.hide() })
                        }
                    }
                    else -> Column {
                        for (index in pages[page]) {
                            val app = apps[index]
                            val rowHeight = with(density) { (heights[index] - bottomPx).toDp() }
                            AppRow(app, onClick = { onClick(app) }, onLongClick = onLongClick?.let { { it(app) } },
                                showIcons = showIcons, iconRevision = iconRevision, labelTextStyle = labelStyle,
                                modifier = Modifier.height(rowHeight).testTag("${tagPrefix}_${app.componentKey}"),
                                trailingContent = trailingContent?.let { content -> { content(app) } })
                            bottomContent?.invoke(app, index)
                        }
                    }
                }
            }
            Column(Modifier.height(footerHeight)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconActionButton(Icons.AutoMirrored.Filled.ArrowBack, "Previous page", enabled = page > 0, onClick = previous, modifier = Modifier.testTag("previous_page"))
                    Text(if (pages.isEmpty()) "—" else "${page + 1} / ${pages.size}", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.testTag("page_indicator").semantics { liveRegion = LiveRegionMode.Polite })
                    IconActionButton(Icons.AutoMirrored.Filled.ArrowForward, "Next page", enabled = page < pages.lastIndex, onClick = next, modifier = Modifier.testTag("next_page"))
                }
            }
        }
    }
}
